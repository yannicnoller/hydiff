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
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.DiffExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.PathCondition.Diff;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.ShadowPCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

public class DDIV extends gov.nasa.jpf.jvm.bytecode.DDIV {

    @Override
    public Instruction execute(ThreadInfo ti) {
        StackFrame sf = ti.getModifiableTopFrame();
        Object op_v1 = sf.getOperandAttr(1);
        Object op_v2 = sf.getOperandAttr(3);

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
            } else if (!pc.isDiffPC() && ti.getExecutionMode() == Execute.BOTH) {
                nextCg = new PCChoiceGenerator(4);
            } else {
                // If we only execute one version, we only have to check for == 0 and != 0
                nextCg = new PCChoiceGenerator(2);
            }

            nextCg.setOffset(this.position);
            nextCg.setMethodName(this.getMethodInfo().getFullName());
            nextCg.setExecutionMode(ti.getExecutionMode());
            ti.getVM().setNextChoiceGenerator(nextCg);
            return this;
        } else { // this is what really returns results
            double v1 = sf.popDouble();
            double v2 = sf.popDouble();

            // Symbolic and shadow expressions of the operands and the result
            RealExpression oldSym_v1 = ShadowBytecodeUtils.getOldSymbolicExpr(op_v1, v1);
            RealExpression oldSym_v2 = ShadowBytecodeUtils.getOldSymbolicExpr(op_v2, v2);
            RealExpression oldCon_v1 = ShadowBytecodeUtils.getOldConcreteExpr(op_v1, v1);
            RealExpression oldCon_v2 = ShadowBytecodeUtils.getOldConcreteExpr(op_v2, v2);

            RealExpression newSym_v1 = ShadowBytecodeUtils.getNewSymbolicExpr(op_v1, v1);
            RealExpression newSym_v2 = ShadowBytecodeUtils.getNewSymbolicExpr(op_v2, v2);
            RealExpression newCon_v1 = ShadowBytecodeUtils.getNewConcreteExpr(op_v1, v1);
            RealExpression newCon_v2 = ShadowBytecodeUtils.getNewConcreteExpr(op_v2, v2);

            RealExpression oldSym_result = null;
            RealExpression newSym_result = null;
            RealExpression oldCon_result = null;
            RealExpression newCon_result = null;

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
                boolean oldConDivByZero = oldCon_v1.solution() == 0.0;
                boolean newConDivByZero = newCon_v1.solution() == 0.0;

                if (pc.isDiffPC()) {
                    // Only execute new version as soon as we are in a diff path
                    if (!newConDivByZero) {
                        curCg.select(0);
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                        oldSym_result = new RealConstant(0);
                        oldCon_result = new RealConstant(0);
                        sf.pushDouble(newCon_result.solution());
                        if (op_v1 instanceof DiffExpression || op_v2 instanceof DiffExpression
                                || ti.getExecutionMode() != Execute.BOTH) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                        } else {
                            // oldSym_result and newSym_result are equal, so we just store one of them
                            RealExpression result = (RealExpression) newSym_result;
                            sf.setLongOperandAttr(result);
                        }
                        return this.getNext(ti);
                    } else {
                        curCg.select(1);
                        sf.pushLong(0);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else if (!pc.containsDiffExpr()) {
                    if (!oldConDivByZero) {
                        // Both versions no division by zero
                        curCg.select(0);
                        sf.pushDouble(v2 / v1);
                        if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, newSym_v1, 0);
                        }
                        if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, oldSym_v1, 0);
                        }
                        curCg.setCurrentPC(pc);
                        oldSym_result = oldSym_v2._div(oldSym_v1);
                        sf.setLongOperandAttr(oldSym_result);
                        return this.getNext(ti);
                    } else {
                        // Both versions do division by zero
                        curCg.select(1);
                        sf.pushLong(0);
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

                        sf.pushDouble(v2 / v1);

                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                        if (oldSym_v1 instanceof RealConstant && oldSym_v2 instanceof RealConstant) {
                            oldSym_result = new RealConstant(oldSym_v2.solution() / oldSym_v1.solution());
                        } else {
                            oldSym_result = oldSym_v2._div(oldSym_v1);
                        }
                        oldCon_result = new RealConstant(oldCon_v2.solution() / oldCon_v1.solution());

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                        } else {
                            RealExpression result = newSym_result;
                            sf.setLongOperandAttr(result);
                        }

                        return this.getNext(ti);
                    } else if (oldConDivByZero && newConDivByZero) {
                        // Both versions follow div by zero
                        curCg.select(1);
                        sf.pushLong(0);
                        oldSym_result = new RealConstant(0);
                        sf.setLongOperandAttr(oldSym_result);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    } else if (oldConDivByZero && !newConDivByZero) {
                        // New version no div by zero, but old version.
                        curCg.select(2);
                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                        sf.pushDouble(newCon_result.solution());
                        oldSym_result = new RealConstant(0);
                        oldCon_result = new RealConstant(0);
                        DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                newCon_result);
                        sf.setLongOperandAttr(result);
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return this.getNext(ti);
                    } else {
                        // New version div by zero, but not old version.
                        curCg.select(3);
                        sf.pushLong(0);
                        newSym_result = new RealConstant(0);
                        sf.setLongOperandAttr(newSym_result);
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
                            newSym_result = new RealConstant(0);
                            newCon_result = new RealConstant(0);
                            if (oldSym_v1 instanceof RealConstant && oldSym_v2 instanceof RealConstant) {
                                oldSym_result = new RealConstant(oldSym_v2.solution() / oldSym_v1.solution());
                            } else {
                                oldSym_result = oldSym_v2._div(oldSym_v1);
                            }
                            oldCon_result = new RealConstant(oldCon_v2.solution() / oldCon_v1.solution());
                            sf.pushDouble(v2 / v1);
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                            return this.getNext(ti);
                        } else {
                            curCg.select(1);
                            sf.pushLong(0);
                            pc._addDet(Comparator.EQ, oldSym_v1, 0);
                            pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                            curCg.setCurrentPC(pc);
                            ShadowPCChoiceGenerator shadowCg = curCg
                                    .getPreviousChoiceGeneratorOfType(ShadowPCChoiceGenerator.class);
                            if (shadowCg != null) {
                                Instruction next = shadowCg.getEndInstruction();
                                sf.popLong();
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
                            if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                                newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                            } else {
                                newSym_result = newSym_v2._div(newSym_v1);
                            }
                            newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                            oldSym_result = new RealConstant(0);
                            oldCon_result = new RealConstant(0);
                            sf.pushDouble(newCon_result.solution());
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                            return this.getNext(ti);
                        } else {
                            curCg.select(1);
                            sf.pushLong(0);
                            pc._addDet(Comparator.EQ, newSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                        }
                    }
                }
            } else {
                /* Full four-way symbolic forking. */
                sf.pushLong(0);
                int choice = curCg.getNextChoice();
                switch (choice) {
                case 0: // Denominator is not zero --> set result and continue normally
                    if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                    }
                    if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.NE, oldSym_v1, 0);
                    }

                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                    } else {
                        /*
                         * We might want to determine results based on the execution mode. However, the StackFrame
                         * already considers the execution mode when propagating values.
                         */
                        curCg.setCurrentPC(pc);

                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                        if (oldSym_v1 instanceof RealConstant && oldSym_v2 instanceof RealConstant) {
                            oldSym_result = new RealConstant(oldSym_v2.solution() / oldSym_v1.solution());
                        } else {
                            oldSym_result = oldSym_v2._div(oldSym_v1);
                        }
                        oldCon_result = new RealConstant(oldCon_v2.solution() / oldCon_v1.solution());

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                        } else {
                            RealExpression result = newSym_result;
                            sf.setLongOperandAttr(result);
                        }
                    }
                    return this.getNext(ti);

                case 1: // Denominator is zero --> throw arithmetic exception
                    if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                    }
                    if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                    }
                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                        return this.getNext(ti);
                    } else {
                        // TODO: Handle division in change(int,int)

                        if (ti.getExecutionMode() == Execute.OLD) {
                            pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                            curCg.setCurrentPC(pc);

                            if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                                newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                            } else {
                                newSym_result = newSym_v2._div(newSym_v1);
                            }
                            newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                            oldSym_result = new RealConstant(0);
                            oldCon_result = new RealConstant(0);

                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                            return this.getNext();
                        }

                        return ti.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }

                case 2:
                    // "True" diff, new is not zero but old is --> bug fix
                    pc._addDet(Comparator.NE, newSym_v1, 0);
                    pc._addDet(Comparator.EQ, oldSym_v1, 0);

                    if (!pc.simplify()) {
                        ti.getVM().getSystemState().setIgnored(true);
                    } else {
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);

                        oldSym_result = new RealConstant(0); // old is NaN
                        oldCon_result = new RealConstant(0);
                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setLongOperandAttr(result);
                        } else {
                            RealExpression result = newSym_result;
                            sf.setLongOperandAttr(result);
                        }
                    }
                    return this.getNext(ti);

                case 3:
                    // "False" diff, new denominator is zero while old is not --> regression
                    pc._addDet(Comparator.EQ, newSym_v1, 0);
                    pc._addDet(Comparator.NE, oldSym_v1, 0);

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