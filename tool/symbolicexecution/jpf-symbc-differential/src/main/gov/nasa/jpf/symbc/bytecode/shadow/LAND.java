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

import gov.nasa.jpf.symbc.numeric.DiffExpression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

/**
 * Boolean AND long ..., value1, value2 => ..., result
 */
public class LAND extends gov.nasa.jpf.jvm.bytecode.LAND {

    @Override
    public Instruction execute(ThreadInfo ti) {
        StackFrame sf = ti.getModifiableTopFrame();
        Object op_v1 = sf.getOperandAttr(1);
        Object op_v2 = sf.getOperandAttr(3);

        if (op_v1 == null && op_v2 == null && ti.getExecutionMode() == Execute.BOTH) {
            return super.execute(ti);// we'll still do the concrete execution
        } else {
            // Pop (concrete) operands from operand stack and push result
            long v1 = sf.popLong();
            long v2 = sf.popLong();
            sf.pushLong(v1 & v2);

            // Get symbolic and shadow expressions from the operands
            IntegerExpression oldSym_v1 = ShadowBytecodeUtils.getOldSymbolicExpr(op_v1, v1);
            IntegerExpression oldSym_v2 = ShadowBytecodeUtils.getOldSymbolicExpr(op_v2, v2);
            IntegerExpression oldCon_v1 = ShadowBytecodeUtils.getOldConcreteExpr(op_v1, v1);
            IntegerExpression oldCon_v2 = ShadowBytecodeUtils.getOldConcreteExpr(op_v2, v2);

            IntegerExpression newSym_v1 = ShadowBytecodeUtils.getNewSymbolicExpr(op_v1, v1);
            IntegerExpression newSym_v2 = ShadowBytecodeUtils.getNewSymbolicExpr(op_v2, v2);
            IntegerExpression newCon_v1 = ShadowBytecodeUtils.getNewConcreteExpr(op_v1, v1);
            IntegerExpression newCon_v2 = ShadowBytecodeUtils.getNewConcreteExpr(op_v2, v2);

            IntegerExpression oldSym_result = null;
            IntegerExpression newSym_result = null;
            IntegerExpression oldCon_result = null;
            IntegerExpression newCon_result = null;

            // Calculate resulting expressions depending on the execution mode
            if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                if (ti.getExecutionMode() == Execute.OLD) {
                    // i.e. only old version is executed, then add a dummy value for the new result to avoid null values
                    newSym_result = new IntegerConstant(0);
                    newCon_result = new IntegerConstant(0);
                }
                if (oldSym_v1 instanceof IntegerConstant && oldSym_v2 instanceof IntegerConstant) {
                    oldSym_result = new IntegerConstant(oldSym_v1.solution() & oldSym_v2.solution());
                } else {
                    oldSym_result = oldSym_v1._and(oldSym_v2);
                }
                oldCon_result = new IntegerConstant(oldCon_v1.solution() & oldCon_v2.solution());
            }

            if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                if (ti.getExecutionMode() == Execute.NEW) {
                    // i.e. only new version is executed, then add a dummy value for the old result to avoid null values
                    oldSym_result = new IntegerConstant(0);
                    oldCon_result = new IntegerConstant(0);
                }
                if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                    newSym_result = new IntegerConstant(newSym_v1.solution() & newSym_v2.solution());
                } else {
                    newSym_result = newSym_v1._and(newSym_v2);
                }
                newCon_result = new IntegerConstant(newCon_v1.solution() & newCon_v2.solution());
            }

            /*
             * Set result:
             * 
             * If at least one of the operands is a DiffExpression, the result will also be a DiffExpression also,
             * executing only the old or the new version will cause a divergence as well
             */
            if (op_v1 instanceof DiffExpression || op_v2 instanceof DiffExpression
                    || ti.getExecutionMode() != Execute.BOTH) {
                DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result, newCon_result);
                sf.setLongOperandAttr(result);
            } else {
                // oldSym_result and newSym_result are equal, so we just store one of them
                IntegerExpression result = (IntegerExpression) newSym_result;
                sf.setLongOperandAttr(result);
            }

            return getNext(ti);
        }
    }
}
