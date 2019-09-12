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

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.symbc.numeric.PathCondition.Diff;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

public class IDIV extends gov.nasa.jpf.jvm.bytecode.IDIV {

    @Override
    public Instruction execute(ThreadInfo ti) {
        StackFrame sf = ti.getModifiableTopFrame();
        Object op_v1 = sf.getOperandAttr(0);
        Object op_v2 = sf.getOperandAttr(1);

        if (op_v1 == null && op_v2 == null && ti.getExecutionMode() == Execute.BOTH) {
            return super.execute(ti); // we'll still do the concrete execution
        }

        /*
         * Either we have at least one symbolic operand and/or we execute only the old or only the new version (which
         * results in divergent results). In both cases, we should check whether the denominator can be zero, which
         * affects the path condition.
         */

        if (!ti.isFirstStepInsn()) { // first time around

            PCChoiceGenerator curPcCg;
            ChoiceGenerator<?> curCg = ti.getVM().getSystemState().getChoiceGenerator();
            if (curCg instanceof PCChoiceGenerator) {
                curPcCg = (PCChoiceGenerator) curCg;
            } else {
                curPcCg = curCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
            }

            PathCondition pc;
            if (curPcCg != null) {
                pc = curPcCg.getCurrentPC();
            } else {
                pc = new PathCondition();
            }

            PCChoiceGenerator nextCg;
            if (SymbolicInstructionFactory.collect_constraints) {
                nextCg = new PCChoiceGenerator(1);
            } else {
                boolean diffPossible = ShadowBytecodeUtils.checkOperandsForPotentialDiff(op_v1, op_v2);
                if (diffPossible && ti.getExecutionMode() == Execute.BOTH && !pc.isDiffPC()
                        && !ShadowBytecodeUtils.isChangeBoolean(this, ti)) {
                    nextCg = new PCChoiceGenerator(4);
                } else {
                    nextCg = new PCChoiceGenerator(2);
                }
            }

            nextCg.setOffset(this.position);
            nextCg.setMethodName(this.getMethodInfo().getFullName());
            nextCg.setExecutionMode(ti.getExecutionMode());
            ti.getVM().setNextChoiceGenerator(nextCg);
            return this;
        } else { // this is what really returns results
            int v1 = sf.pop();
            int v2 = sf.pop();

            // Symbolic and shadow expressions of the operands and the result
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

            PCChoiceGenerator curCg = (PCChoiceGenerator) ti.getVM().getChoiceGenerator();
            PCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

            // get current path condition
            PathCondition pc;
            if (prevCg == null) {
                pc = new PathCondition();
            } else {
                pc = prevCg.getCurrentPC();
            }

            assert (pc != null);

            // Restore execution mode
            ti.setExecutionMode(curCg.getExecutionMode());

            if (SymbolicInstructionFactory.collect_constraints) {
                boolean oldConDivByZero = oldCon_v1.solutionInt() == 0;
                boolean newConDivByZero = newCon_v1.solutionInt() == 0;

                if (pc.isDiffPC()) {
                    // Only execute new version as soon as we are in a diff path
                    if (!newConDivByZero) {
                        curCg.select(0);
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                            newSym_result = new IntegerConstant(newSym_v2.solutionInt() / newSym_v1.solutionInt());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new IntegerConstant(newCon_v2.solutionInt() / newCon_v1.solutionInt());
                        oldSym_result = new IntegerConstant(0);
                        oldCon_result = new IntegerConstant(0);
                        sf.push(newCon_result.solutionInt(), false);
                        if (op_v1 instanceof DiffExpression || op_v2 instanceof DiffExpression
                                || ti.getExecutionMode() != Execute.BOTH) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                        } else {
                            // oldSym_result and newSym_result are equal, so we just store one of them
                            IntegerExpression result = (IntegerExpression) newSym_result;
                            sf.setOperandAttr(result);
                        }

                        return this.getNext(ti);
                    } else {
                        curCg.select(1);
                        sf.push(0, false);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else if (!pc.containsDiffExpr()) {
                    if (!oldConDivByZero) {
                        // Both versions no division by zero
                        curCg.select(0);
                        sf.push(v2 / v1, false);
                        if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, newSym_v1, 0);
                        }
                        if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, oldSym_v1, 0);
                        }
                        curCg.setCurrentPC(pc);
                        oldSym_result = oldSym_v2._div(oldSym_v1);
                        sf.setOperandAttr(oldSym_result);
                        return this.getNext(ti);
                    } else {
                        // Both versions do division by zero
                        curCg.select(1);
                        sf.push(0, false);
                        if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.EQ, newSym_v1, 0);
                        }
                        if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        }
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else if (ti.getExecutionMode() == Execute.BOTH) {
                    if (!oldConDivByZero && !newConDivByZero) {
                        // Both versions follow no div by zero
                        curCg.select(0);

                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        pc._addDet(Comparator.NE, oldSym_v1, 0);
                        curCg.setCurrentPC(pc);

                        sf.push(v2 / v1, false);

                        if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                            newSym_result = new IntegerConstant(newSym_v2.solutionInt() / newSym_v1.solutionInt());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new IntegerConstant(newCon_v2.solutionInt() / newCon_v1.solutionInt());
                        if (oldSym_v1 instanceof IntegerConstant && oldSym_v2 instanceof IntegerConstant) {
                            oldSym_result = new IntegerConstant(oldSym_v2.solutionInt() / oldSym_v1.solutionInt());
                        } else {
                            oldSym_result = oldSym_v2._div(oldSym_v1);
                        }
                        oldCon_result = new IntegerConstant(oldCon_v2.solutionInt() / oldCon_v1.solutionInt());

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                        } else {
                            IntegerExpression result = newSym_result;
                            sf.setOperandAttr(result);
                        }

                        return this.getNext(ti);
                    } else if (oldConDivByZero && newConDivByZero) {
                        // Both versions follow div by zero
                        curCg.select(1);
                        sf.push(0, false);
                        oldSym_result = new IntegerConstant(0);
                        sf.setOperandAttr(oldSym_result);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    } else if (oldConDivByZero && !newConDivByZero) {
                        // New version no div by zero, but old version.
                        curCg.select(2);
                        if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                            newSym_result = new IntegerConstant(newSym_v2.solutionInt() / newSym_v1.solutionInt());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new IntegerConstant(newCon_v2.solutionInt() / newCon_v1.solutionInt());
                        sf.push(newCon_result.solutionInt(), false);
                        oldSym_result = new IntegerConstant(0);
                        oldCon_result = new IntegerConstant(0);
                        DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                newCon_result);
                        sf.setOperandAttr(result);
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return this.getNext(ti);
                    } else {
                        // New version div by zero, but not old version.
                        curCg.select(3);
                        sf.push(0, false);
                        newSym_result = new IntegerConstant(0);
                        sf.setOperandAttr(newSym_result);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        pc._addDet(Comparator.NE, oldSym_v1, 0);
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else {
                    // Evaluating the old or new expression in an if(change(boolean,boolean)) stmt
                    if (ti.getExecutionMode() == Execute.OLD) {
                        if (!oldConDivByZero) {
                            curCg.select(0);
                            pc._addDet(Comparator.NE, oldSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            newSym_result = new IntegerConstant(0);
                            newCon_result = new IntegerConstant(0);
                            if (oldSym_v1 instanceof IntegerConstant && oldSym_v2 instanceof IntegerConstant) {
                                oldSym_result = new IntegerConstant(oldSym_v2.solutionInt() / oldSym_v1.solutionInt());
                            } else {
                                oldSym_result = oldSym_v2._div(oldSym_v1);
                            }
                            oldCon_result = new IntegerConstant(oldCon_v2.solutionInt() / oldCon_v1.solutionInt());
                            sf.push(v2 / v1, false);
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                            return this.getNext(ti);
                        } else {
                            curCg.select(1);
                            sf.push(0, false);
                            pc._addDet(Comparator.EQ, oldSym_v1, 0);
                            pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                            curCg.setCurrentPC(pc);
                            ShadowPCChoiceGenerator shadowCg = curCg
                                    .getPreviousChoiceGeneratorOfType(ShadowPCChoiceGenerator.class);
                            if (shadowCg != null) {
                                Instruction next = shadowCg.getEndInstruction();
                                sf.pop();
                                ti.skipInstruction(next);
                                return next;
                            } else {
                                // if shadowCG cannot be found, then throw the exception.
                                return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                            }

                        }
                    } else {
                        if (!newConDivByZero) {
                            curCg.select(0);
                            pc._addDet(Comparator.NE, newSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                                newSym_result = new IntegerConstant(newSym_v2.solutionInt() / newSym_v1.solutionInt());
                            } else {
                                newSym_result = newSym_v2._div(newSym_v1);
                            }
                            newCon_result = new IntegerConstant(newCon_v2.solutionInt() / newCon_v1.solutionInt());
                            oldSym_result = new IntegerConstant(0);
                            oldCon_result = new IntegerConstant(0);
                            sf.push(newCon_result.solutionInt(), false);
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                            return this.getNext(ti);
                        } else {
                            curCg.select(1);
                            sf.push(0, false);
                            pc._addDet(Comparator.EQ, newSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                        }
                    }
                }
            } else {
                /* Full four-way symbolic forking. */
                sf.push(0, false);
                int choice = curCg.getNextChoice();
                switch (choice) {
                case 0: // Denominator is not zero --> set result and continue normally
                    if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                        if (newSym_v1 instanceof IntegerConstant && newSym_v1.solutionInt() == 0) {
                            ti.getVM().getSystemState().setIgnored(true);
                            return this.getNext(ti);
                        } else {
                            pc._addDet(Comparator.NE, newSym_v1, 0);    
                        }
                    }
                    if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                        if (oldSym_v1 instanceof IntegerConstant && oldSym_v1.solutionInt() == 0) {
                            ti.getVM().getSystemState().setIgnored(true);
                            return this.getNext(ti);
                        } else {
                            pc._addDet(Comparator.NE, oldSym_v1, 0);    
                        }
                    }

                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                    } else {
                        /*
                         * We might want to determine results based on the execution mode. However, the StackFrame
                         * already considers the execution mode when propagating values.
                         */
                        curCg.setCurrentPC(pc);

                        if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                            newSym_result = new IntegerConstant(newSym_v2.solutionInt() / newSym_v1.solutionInt());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new IntegerConstant(newCon_v2.solutionInt() / newCon_v1.solutionInt());
                        if (oldSym_v1 instanceof IntegerConstant && oldSym_v2 instanceof IntegerConstant) {
                            oldSym_result = new IntegerConstant(oldSym_v2.solutionInt() / oldSym_v1.solutionInt());
                        } else {
                            oldSym_result = oldSym_v2._div(oldSym_v1);
                        }
                        oldCon_result = new IntegerConstant(oldCon_v2.solutionInt() / oldCon_v1.solutionInt());

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                        } else {
                            IntegerExpression result = newSym_result;
                            sf.setOperandAttr(result);
                        }
                    }
                    return this.getNext(ti);

                case 1: // Denominator is zero --> throw arithmetic exception
                    if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                        if (newSym_v1 instanceof IntegerConstant && newSym_v1.solutionInt() != 0) {
                            ti.getVM().getSystemState().setIgnored(true);
                            return this.getNext(ti);
                        } else {
                            pc._addDet(Comparator.EQ, newSym_v1, 0);    
                        }
                    }
                    if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                        if (oldSym_v1 instanceof IntegerConstant && oldSym_v1.solutionInt() != 0) {
                            ti.getVM().getSystemState().setIgnored(true);
                            return this.getNext(ti);
                        } else {
                            pc._addDet(Comparator.EQ, oldSym_v1, 0);    
                        }
                    }
                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        if (ti.getExecutionMode() == Execute.OLD) {
                            pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                            curCg.setCurrentPC(pc);

                            newSym_result = new IntegerConstant(0);
                            newCon_result = new IntegerConstant(0);
                            if (oldSym_v1 instanceof IntegerConstant && oldSym_v2 instanceof IntegerConstant) {
                                oldSym_result = new IntegerConstant(0);
                            } else {
                                oldSym_result = oldSym_v2._div(oldSym_v1);
                            }
                            oldCon_result = new IntegerConstant(0);

                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                            return this.getNext();
                        }

                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }

                case 2:
                    // "True" diff, new is not zero but old is --> bug fix
                    if (newSym_v1 instanceof IntegerConstant && newSym_v1.solutionInt() == 0) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        pc._addDet(Comparator.NE, newSym_v1, 0);    
                    }
                    if (oldSym_v1 instanceof IntegerConstant && oldSym_v1.solutionInt() != 0) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);    
                    }

                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                    } else {
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);

                        if (newSym_v1 instanceof IntegerConstant && newSym_v2 instanceof IntegerConstant) {
                            newSym_result = new IntegerConstant(newSym_v2.solutionInt() / newSym_v1.solutionInt());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new IntegerConstant(newCon_v2.solutionInt() / newCon_v1.solutionInt());
                        oldSym_result = new IntegerConstant(0); // old is NaN
                        oldCon_result = new IntegerConstant(0);

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                        } else {
                            IntegerExpression result = newSym_result;
                            sf.setOperandAttr(result);
                        }
                    }
                    return this.getNext(ti);

                case 3:
                    // "False" diff, new denominator is zero while old is not --> regression
                    if (newSym_v1 instanceof IntegerConstant && newSym_v1.solutionInt() != 0) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        pc._addDet(Comparator.EQ, newSym_v1, 0);    
                    }
                    if (oldSym_v1 instanceof IntegerConstant && oldSym_v1.solutionInt() == 0) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        pc._addDet(Comparator.NE, oldSym_v1, 0);    
                    }

                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }

                default:
                    assert (false);
                    return this;
                }
            }
        }
    }
}