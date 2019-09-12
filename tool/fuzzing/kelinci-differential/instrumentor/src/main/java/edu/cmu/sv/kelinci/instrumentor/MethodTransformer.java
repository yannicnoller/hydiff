package edu.cmu.sv.kelinci.instrumentor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.hub.se.cfg.CFGAnalysis;
import de.hub.se.cfg.CFGNode;
import de.hub.se.cfg.CFGTarget;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Label;

import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.instrumentor.Options.InstrumentationMode;

import java.util.HashSet;
import java.util.Random;

/**
 * @author rodykers
 *
 *         Adds AFL-like instrumentation to branches.
 * 
 *         Uses the ASM MethodVisitor to instrument the start of methods, the location immediately after a branch (else
 *         case), as well as all labels.
 * 
 *         There are also methods in MethodVisitor that we could override to instrument tableswitch, lookupswitch and
 *         try-catch. But as those jump to labels in any case (including default), instrumenting the labels only is
 *         enough.
 */
public class MethodTransformer extends MethodVisitor {

    private HashSet<Integer> ids;
    Random r;
    private String className;
    private String methodName;

    /**
     * @param mv
     *            - MethodVisitor
     * @param methodName
     *            - expects the full qualified method name inclusive the signature, e.g. "Foo.foo(I)I"
     */
    public MethodTransformer(MethodVisitor mv, String className, String methodName) {
        super(ASM5, mv);

        ids = new HashSet<>();
        r = new Random();
        this.className = className;
        this.methodName = methodName;
    }

    /**
     * Best effort to generate a random id that is not already in use.
     */
    private int getNewLocationId() {
        int id;
        int tries = 0;
        do {
            id = r.nextInt(Mem.BUFFER_SIZE);
            tries++;
        } while (tries <= 10 && ids.contains(id));
        ids.add(id);
        return id;
    }

    /**
     * Instrument a program location, AFL style. Each location gets a compile time random ID, hopefully unique, but
     * maybe not.
     * 
     * Instrumentation is the bytecode translation of this:
     * 
     * Mem.mem[id^Mem.prev_location]++; Mem.prev_location = id >> 1;
     *
     */
    private void instrumentLocation() {
        Integer id = getNewLocationId();
        mv.visitFieldInsn(GETSTATIC, "edu/cmu/sv/kelinci/Mem", "mem", "[B");
        mv.visitLdcInsn(id);
        mv.visitFieldInsn(GETSTATIC, "edu/cmu/sv/kelinci/Mem", "prev_location", "I");
        mv.visitInsn(IXOR);
        mv.visitInsn(DUP2);
        mv.visitInsn(BALOAD);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitInsn(I2B);
        mv.visitInsn(BASTORE);
        mv.visitIntInsn(SIPUSH, (id >> 1));
        mv.visitFieldInsn(PUTSTATIC, "edu/cmu/sv/kelinci/Mem", "prev_location", "I");
    }

    private void instrumentForCounting() {
        // RK: experimental
        mv.visitFieldInsn(GETSTATIC, "edu/cmu/sv/kelinci/Mem", "instrCost", "J");
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LADD);
        mv.visitFieldInsn(PUTSTATIC, "edu/cmu/sv/kelinci/Mem", "instrCost", "J");
    }

    private void instrumentDecision(Label targetLabel) {
        int labelHashCode = targetLabel.hashCode();
        mv.visitLdcInsn(labelHashCode);
        mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/sv/kelinci/regression/DecisionHistory", "addDecision", "(I)V", false);
    }

    private void instrumentUpdateLastDecision(Label label) {
        int labelHashCode = label.hashCode();
        mv.visitLdcInsn(labelHashCode);
        mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/sv/kelinci/regression/DecisionHistory", "flipLastDecision", "(I)V",
                false);
    }

    /**
     * Update CFGSummary, i.e. the distance information.
     */
    private void instrumentCFGDistanceUpdate(int line, Label start) {
        int sourceLineNumber = line;

        /* Update minimum distance for every target. */
        CFGAnalysis cfga = Instrumentor.cfga;

        /* Check whether CFG was calculated for current class (e.g., interfaces get skipped). */
        if (cfga.wasClassSkippedInCFGBuilding(className)) {
            return;
        }

        for (CFGTarget target : cfga.getProcessedTargets()) {
            CFGNode currentNode = cfga.getNodeByMethodAndSourceLine(methodName, sourceLineNumber);
            if (currentNode == null) {
                continue; // sourceline does not exist in CFG
            }
            String targetMethod = target.getMethod();
            int targetSourceLineNumber = target.getSourceLineNumber();
            CFGNode targetNode = cfga.getNodeByMethodAndSourceLine(targetMethod, targetSourceLineNumber);
            Integer currentDistance = currentNode.getDistance(targetNode.getId());

            /* Only instrument if target is reachable and not after the target. */
            if (currentDistance != null && currentDistance >= 0) {
                /*
                 * Bytecode to instrument to call the updateDistance method with the targetus information and the
                 * distance.
                 */
                mv.visitLdcInsn(targetMethod);
                mv.visitIntInsn(SIPUSH, targetSourceLineNumber);
                mv.visitIntInsn(SIPUSH, currentDistance.intValue());
                mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/sv/kelinci/regression/CFGSummary", "updateDistance",
                        "(Ljava/lang/String;II)V", false);
            }
        }

    }

    @Override
    public void visitCode() {
        mv.visitCode();

        /**
         * Add instrumentation at start of method.
         */
        instrumentLocation();
        instrumentForCounting();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {

        /*
         * If enabled, then instrument the decision before jumping to false, a later update can override this decision
         * to true if jumped. Skip for GOTO or JSR
         */
        if ((Options.v().getInstrumentationMode().equals(InstrumentationMode.REGRESSION)
                || Options.v().getInstrumentationMode().equals(InstrumentationMode.REGRESSION_NO_CFG))
                && opcode != Opcodes.GOTO && opcode != Opcodes.JSR) {
            instrumentDecision(label);
        }

        mv.visitJumpInsn(opcode, label);

        /**
         * Add instrumentation after the jump. Instrumentation for the if-branch is handled by visitLabel().
         */
        if (Options.v().getInstrumentationMode().equals(InstrumentationMode.JUMPS)) {
            instrumentLocation();
            instrumentForCounting();
        }
    }

    @Override
    public void visitLabel(Label label) {
        mv.visitLabel(label);

        /**
         * Since there is a label, we most probably (surely?) jump to this location. Instrument.
         */
        instrumentLocation();
        instrumentForCounting();

        if (Options.v().getInstrumentationMode().equals(InstrumentationMode.REGRESSION)
                || Options.v().getInstrumentationMode().equals(InstrumentationMode.REGRESSION_NO_CFG)) {
            instrumentUpdateLastDecision(label);
        }
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);

        /* Don't instrument init methods. */
        if ((Options.v().getInstrumentationMode().equals(InstrumentationMode.REGRESSION)
                || Options.v().getInstrumentationMode().equals(InstrumentationMode.REGRESSION_NO_DEC))
                && !methodName.contains(".<init>(") && !methodName.contains(".<clinit>()")) {
            instrumentCFGDistanceUpdate(line, start);
        }

    }
}
