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
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

/*
 * Implementation of the shadow symbolic IINC bytecode instruction
 */

public class IINC extends gov.nasa.jpf.jvm.bytecode.IINC {
    public IINC(int localVarIndex, int incConstant) {
        super(localVarIndex, incConstant);
    }

    @Override
    public Instruction execute(ThreadInfo ti) {

        StackFrame sf = ti.getModifiableTopFrame();
        Object op_v = sf.getLocalAttr(index);

        if (op_v == null && ti.getExecutionMode() == Execute.BOTH) {
            // we'll do the concrete execution
            return super.execute(ti);
        } else {
            // either the operand is symbolic (i.e. op_v != null) or the execution mode is not BOTH

            int v = sf.getLocalVariable(index);
            sf.setLocalVariable(index, v + increment, false);

            // Symbolic and shadow expressions of the operand and result
            IntegerExpression oldSym_v = ShadowBytecodeUtils.getOldSymbolicExpr(op_v, v);
            IntegerExpression oldCon_v = ShadowBytecodeUtils.getOldConcreteExpr(op_v, v);

            IntegerExpression newSym_v = ShadowBytecodeUtils.getNewSymbolicExpr(op_v, v);
            IntegerExpression newCon_v = ShadowBytecodeUtils.getNewConcreteExpr(op_v, v);

            IntegerExpression oldSym_result = null;
            IntegerExpression newSym_result = null;
            IntegerExpression oldCon_result = null;
            IntegerExpression newCon_result = null;

            if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                if (ti.getExecutionMode() == Execute.OLD) {
                    newSym_result = newSym_v;
                    newCon_result = newCon_v;
                }
                if (oldSym_v instanceof IntegerConstant) {
                    oldSym_result = new IntegerConstant(oldSym_v.solutionInt() + increment);
                } else {
                    oldSym_result = oldSym_v._plus(increment);
                }
                oldCon_result = new IntegerConstant(oldCon_v.solutionInt() + increment);
            }

            if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                if (ti.getExecutionMode() == Execute.NEW) {
                    oldSym_result = oldSym_v;
                    oldCon_result = oldCon_v;
                }
                if (newSym_v instanceof IntegerConstant) {
                    newSym_result = new IntegerConstant(newSym_v.solutionInt() + increment);
                } else {
                    newSym_result = newSym_v._plus(increment);
                }
                newCon_result = new IntegerConstant(newCon_v.solutionInt() + increment);
            }

            /*
             * If the operand is a DiffExpression, the result will be a DiffExpression as well, executing only the old
             * or the new version will cause a divergence as well
             */
            if (op_v instanceof DiffExpression || ti.getExecutionMode() != Execute.BOTH) {
                DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result, newCon_result);
                sf.setOperandAttr(result);
            } else {
                // oldSym_result and newSym_result are equal, so we just store one of them
                IntegerExpression result = (IntegerExpression) newSym_result;
                sf.setOperandAttr(result);
            }

        }

        return getNext(ti);
    }

}
