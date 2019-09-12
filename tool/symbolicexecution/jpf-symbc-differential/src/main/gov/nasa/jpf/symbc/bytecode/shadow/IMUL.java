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

//Copyright (C) 2007 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.

//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.

//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
package gov.nasa.jpf.symbc.bytecode.shadow;

import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

/*
 * Implementation of the shadow symbolic IMUL bytecode instruction
 */
public class IMUL extends gov.nasa.jpf.jvm.bytecode.IMUL {
    @Override
    public Instruction execute(ThreadInfo ti) {

        StackFrame sf = ti.getModifiableTopFrame();
        Object op_v1 = sf.getOperandAttr(0);
        Object op_v2 = sf.getOperandAttr(1);

        if (op_v1 == null && op_v2 == null && ti.getExecutionMode() == Execute.BOTH) {
            return super.execute(ti); // both operands are concrete
        } else {
            // Pop (concrete) operands from operand stack and push result
            int v1 = sf.pop();
            int v2 = sf.pop();
            sf.push(v1 * v2, false); // for symbolic expressions, the concrete value actually does not matter

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
                    oldSym_result = new IntegerConstant(oldSym_v1.solutionInt() * oldSym_v2.solutionInt());
                } else {
                    oldSym_result = oldSym_v1._mul(oldSym_v2);
                }
                oldCon_result = new IntegerConstant(oldCon_v1.solutionInt() * oldCon_v2.solutionInt());

            }

            if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                if (ti.getExecutionMode() == Execute.OLD) {
                    // i.e. only old version is executed, then add a dummy value for the new result to avoid null values
                    newSym_result = new IntegerConstant(0);
                    newCon_result = new IntegerConstant(0);
                }
                if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                    newSym_result = new IntegerConstant(newSym_v1.solutionInt() * newSym_v2.solutionInt());
                } else {
                    newSym_result = newSym_v1._mul(newSym_v2);
                }
                newCon_result = new IntegerConstant(newCon_v1.solutionInt() * newCon_v2.solutionInt());
            }

            // Set result

            /*
             * If at least one of the operands is a DiffExpression, the result will be a DiffExpression as well,
             * executing only the old or the new version will cause a divergence as well
             */
            if (op_v1 instanceof DiffExpression || op_v2 instanceof DiffExpression
                    || ti.getExecutionMode() != Execute.BOTH) {
                DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result, newCon_result);
                sf.setOperandAttr(result);
            } else {
                // Shadow_result and sym_result are equal, so we just store one of them
                IntegerExpression result = (IntegerExpression) newSym_result;
                sf.setOperandAttr(result);
            }
        }

        return getNext(ti);
    }
}
