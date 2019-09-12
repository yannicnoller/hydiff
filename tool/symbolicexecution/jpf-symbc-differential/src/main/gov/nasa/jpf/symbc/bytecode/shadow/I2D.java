/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.symbc.bytecode.shadow;

import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

/**
 * Convert int to double ..., value => ..., result
 */
public class I2D extends gov.nasa.jpf.jvm.bytecode.I2D {

    @Override
    public Instruction execute(ThreadInfo ti) {
        Object op_v = ti.getModifiableTopFrame().getOperandAttr();

        if (op_v == null && ti.getExecutionMode() == Execute.BOTH) {
            return super.execute(ti);
        } else {

            // here we get a hold of the current path condition and
            // add an extra mixed constraint sym_dval==sym_ival

            ChoiceGenerator<?> cg;
            if (!ti.isFirstStepInsn()) { // first time around
                cg = new PCChoiceGenerator(1); // only one choice
                ti.getVM().getSystemState().setNextChoiceGenerator(cg);
                return this;
            } else { // this is what really returns results
                cg = ti.getVM().getSystemState().getChoiceGenerator();
                assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
            }

            // get the path condition from the
            // previous choice generator of the same type

            PathCondition pc;
            ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

            if (prev_cg == null)
                pc = new PathCondition(); // TODO: handling of preconditions needs to be changed
            else
                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();
            assert pc != null;

            StackFrame sf = ti.getModifiableTopFrame();
            int v = sf.pop();
            sf.pushDouble(v);

            IntegerExpression oldSym_v = ShadowBytecodeUtils.getOldSymbolicExpr(op_v, v);
            IntegerExpression oldCon_v = ShadowBytecodeUtils.getOldConcreteExpr(op_v, v);
            IntegerExpression newSym_v = ShadowBytecodeUtils.getNewSymbolicExpr(op_v, v);
            IntegerExpression newCon_v = ShadowBytecodeUtils.getNewConcreteExpr(op_v, v);

            SymbolicReal newSym_doubleValue = new SymbolicReal();
            pc._addDet(Comparator.EQ, newSym_doubleValue, newSym_v);
            RealExpression newCon_doubleValue = new RealConstant((double) newCon_v.solutionInt());

            if (op_v instanceof DiffExpression) {
                SymbolicReal oldSym_doubleValue = new SymbolicReal();
                pc._addDet(Comparator.EQ, oldSym_doubleValue, oldSym_v);
                RealExpression oldCon_doubleValue = new RealConstant((double) oldCon_v.solutionInt());
                DiffExpression result_dval = new DiffExpression(oldSym_doubleValue, newSym_doubleValue, oldCon_doubleValue, newCon_doubleValue);
                sf.setLongOperandAttr(result_dval);
            } else {
                sf.setLongOperandAttr(newSym_doubleValue);
            }

            if (!pc.simplify()) { // not satisfiable
                ti.getVM().getSystemState().setIgnored(true);
            } else {
                ((PCChoiceGenerator) cg).setCurrentPC(pc);
            }

            return getNext(ti);
        }

    }

}
