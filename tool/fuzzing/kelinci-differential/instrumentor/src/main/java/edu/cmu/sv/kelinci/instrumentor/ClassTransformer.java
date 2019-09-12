package edu.cmu.sv.kelinci.instrumentor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author rodykers
 *
 * Does nothing more than call the MethodTransformer.
 */
public class ClassTransformer extends ClassVisitor {
	public ClassTransformer(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}
	
	private String className;
	
	@Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
        className = name.replace("/", ".");
    }

	@Override
	public MethodVisitor visitMethod(int access, String name,
			String desc, String signature, String[] exceptions) {
		MethodVisitor mv;
		mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv != null && !(Options.v().skipMain() && name.equals("main"))) {
			String fullQualifiedMethodName = className + "." + name + desc;
			mv = new MethodTransformer(mv, className, fullQualifiedMethodName);
		}
		return mv;
	}
}
