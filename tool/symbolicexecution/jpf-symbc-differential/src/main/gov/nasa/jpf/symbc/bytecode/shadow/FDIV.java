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

public class FDIV extends gov.nasa.jpf.jvm.bytecode.FDIV {

    @Override
    public Instruction execute(ThreadInfo th) {
        StackFrame sf = th.getModifiableTopFrame();
        Object op_v1 = sf.getOperandAttr(0);
        Object op_v2 = sf.getOperandAttr(1);

        if (op_v1 == null && op_v2 == null && th.getExecutionMode() == Execute.BOTH) {
            return super.execute(th); // we'll still do the concrete execution
        }

        /*
         * Either we have at least one symbolic operand and/or we execute only the old or only the new version (which
         * results in divergent results). In both cases, we should check whether the denominator can be zero, which
         * affects the path condition.
         */

        if (!th.isFirstStepInsn()) { // first time around
            PCChoiceGenerator curPcCg;
            ChoiceGenerator<?> curCg = th.getVM().getSystemState().getChoiceGenerator();
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
            } else if (!pc.isDiffPC() && th.getExecutionMode() == Execute.BOTH) {
                nextCg = new PCChoiceGenerator(4);
            } else {
                // If we only execute one version, we only have to check for == 0 and != 0
                nextCg = new PCChoiceGenerator(2);
            }

            nextCg.setOffset(this.position);
            nextCg.setMethodName(this.getMethodInfo().getFullName());
            nextCg.setExecutionMode(th.getExecutionMode());
            th.getVM().setNextChoiceGenerator(nextCg);
            return this;
        } else { // this is what really returns results
            float v1 = sf.popFloat();
            float v2 = sf.popFloat();

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

            PCChoiceGenerator curCg = (PCChoiceGenerator) th.getVM().getChoiceGenerator();
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
            th.setExecutionMode(curCg.getExecutionMode());

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
                        sf.pushFloat((float) newCon_result.solution());
                        if (op_v1 instanceof DiffExpression || op_v2 instanceof DiffExpression
                                || th.getExecutionMode() != Execute.BOTH) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                        } else {
                            // oldSym_result and newSym_result are equal, so we just store one of them
                            RealExpression result = (RealExpression) newSym_result;
                            sf.setOperandAttr(result);
                        }
                        return this.getNext(th);
                    } else {
                        curCg.select(1);
                        sf.push(0);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else if (!pc.containsDiffExpr()) {
                    if (!oldConDivByZero) {
                        // Both versions no division by zero
                        curCg.select(0);
                        sf.pushFloat(v2 / v1);
                        if (th.getExecutionMode() == Execute.NEW || th.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, newSym_v1, 0);
                        }
                        if (th.getExecutionMode() == Execute.OLD || th.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, oldSym_v1, 0);
                        }
                        curCg.setCurrentPC(pc);
                        oldSym_result = oldSym_v2._div(oldSym_v1);
                        sf.setOperandAttr(oldSym_result);
                        return this.getNext(th);
                    } else {
                        // Both versions do division by zero
                        curCg.select(1);
                        sf.push(0);
                        if (th.getExecutionMode() == Execute.NEW || th.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.EQ, newSym_v1, 0);
                        }
                        if (th.getExecutionMode() == Execute.OLD || th.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        }
                        curCg.setCurrentPC(pc);
                        return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else if (th.getExecutionMode() == Execute.BOTH) {
                    if (!oldConDivByZero && !newConDivByZero) {
                        // Both versions follow no div by zero
                        curCg.select(0);

                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        pc._addDet(Comparator.NE, oldSym_v1, 0);
                        curCg.setCurrentPC(pc);

                        sf.pushFloat(v2 / v1);

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
                            sf.setOperandAttr(result);
                        } else {
                            RealExpression result = newSym_result;
                            sf.setOperandAttr(result);
                        }

                        return this.getNext(th);
                    } else if (oldConDivByZero && newConDivByZero) {
                        // Both versions follow div by zero
                        curCg.select(1);
                        sf.push(0);
                        oldSym_result = new RealConstant(0);
                        sf.setOperandAttr(oldSym_result);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        curCg.setCurrentPC(pc);
                        return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    } else if (oldConDivByZero && !newConDivByZero) {
                        // New version no div by zero, but old version.
                        curCg.select(2);
                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                        sf.pushFloat((float) newCon_result.solution());
                        oldSym_result = new RealConstant(0);
                        oldCon_result = new RealConstant(0);
                        DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                newCon_result);
                        sf.setOperandAttr(result);
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return this.getNext(th);
                    } else {
                        // New version div by zero, but not old version.
                        curCg.select(3);
                        sf.push(0);
                        newSym_result = new RealConstant(0);
                        sf.setOperandAttr(newSym_result);
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                        pc._addDet(Comparator.NE, oldSym_v1, 0);
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }
                } else {
                    // Evaluating the old or new expression in an if(change(boolean,boolean)) stmt
                    if (th.getExecutionMode() == Execute.OLD) {
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
                            sf.pushFloat(v2 / v1);
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                            return this.getNext(th);
                        } else {
                            curCg.select(1);
                            sf.push(0);
                            pc._addDet(Comparator.EQ, oldSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                            ShadowPCChoiceGenerator shadowCg = curCg
                                    .getPreviousChoiceGeneratorOfType(ShadowPCChoiceGenerator.class);
                            if (shadowCg != null) {
                                Instruction next = shadowCg.getEndInstruction();
                                sf.pop();
                                th.skipInstruction(next);
                                return next;
                            } else {
                                // if shadowCG cannot be found, then throw the exception.
                                return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                            }
                        }
                    } else {
                        if (!newConDivByZero) {
                            curCg.select(0);
                            pc._addDet(Comparator.NE, newSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                            if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                                newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                            } else {
                                newSym_result = newSym_v2._div(newSym_v1);
                            }
                            newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                            oldSym_result = new RealConstant(0);
                            oldCon_result = new RealConstant(0);
                            sf.pushFloat((float) newCon_result.solution());
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                            return this.getNext(th);
                        } else {
                            curCg.select(1);
                            sf.push(0);
                            pc._addDet(Comparator.EQ, newSym_v1, 0);
                            curCg.setCurrentPC(pc);
                            return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                        }
                    }
                }
            } else {
                /* Full four-way symbolic forking. */
                sf.push(0);
                int choice = curCg.getNextChoice();
                switch (choice) {
                case 0: // Denominator is not zero --> set result and continue normally
                    if (th.getExecutionMode() == Execute.NEW || th.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.NE, newSym_v1, 0);
                    }
                    if (th.getExecutionMode() == Execute.OLD || th.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.NE, oldSym_v1, 0);
                    }

                    if (!pc.simplify()) {
                        th.getVM().getSystemState().setIgnored(true);
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
                            sf.setOperandAttr(result);
                        } else {
                            RealExpression result = newSym_result;
                            sf.setOperandAttr(result);
                        }
                    }
                    return this.getNext(th);

                case 1: // Denominator is zero --> throw arithmetic exception
                    if (th.getExecutionMode() == Execute.NEW || th.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.EQ, newSym_v1, 0);
                    }
                    if (th.getExecutionMode() == Execute.OLD || th.getExecutionMode() == Execute.BOTH) {
                        pc._addDet(Comparator.EQ, oldSym_v1, 0);
                    }
                    if (!pc.simplify()) {
                        th.getVM().getSystemState().setIgnored(true);
                        return this.getNext(th);
                    } else {
                        if (th.getExecutionMode() == Execute.OLD) {
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
                            sf.setOperandAttr(result);
                            return this.getNext();
                        }

                        return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }

                case 2:
                    // "True" diff, new is not zero but old is --> bug fix
                    pc._addDet(Comparator.NE, newSym_v1, 0);
                    pc._addDet(Comparator.EQ, oldSym_v1, 0);

                    if (!pc.simplify()) {
                        th.getVM().getSystemState().setIgnored(true);
                    } else {
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);

                        if (newSym_v2 instanceof RealConstant && newSym_v1 instanceof RealConstant) {
                            newSym_result = new RealConstant(newSym_v2.solution() / newSym_v1.solution());
                        } else {
                            newSym_result = newSym_v2._div(newSym_v1);
                        }
                        newCon_result = new RealConstant(newCon_v2.solution() / newCon_v1.solution());
                        if (oldSym_v1 instanceof RealConstant && oldSym_v2 instanceof RealConstant) {
                            oldSym_result = oldCon_result = new RealConstant(0);
                        } else {
                            oldSym_result = oldSym_v2._div(oldSym_v1);
                        }
                        oldCon_result = new RealConstant(0);

                        if ((op_v1 instanceof DiffExpression) || (op_v2 instanceof DiffExpression)) {
                            DiffExpression result = new DiffExpression(oldSym_result, newSym_result, oldCon_result,
                                    newCon_result);
                            sf.setOperandAttr(result);
                        } else {
                            RealExpression result = newSym_result;
                            sf.setOperandAttr(result);
                        }
                    }
                    return this.getNext(th);

                case 3:
                    // "False" diff, new denominator is zero while old is not --> regression
                    pc._addDet(Comparator.EQ, newSym_v1, 0);
                    pc._addDet(Comparator.NE, oldSym_v1, 0);

                    if (!pc.simplify()) {
                        th.getVM().getSystemState().setIgnored(true);
                        return this.getNext(th);
                    } else {
                        pc.markAsDiffPC(this.getLineNumber(), Diff.divByZero);
                        curCg.setCurrentPC(pc);
                        return th.createAndThrowException("java.lang.ArithmeticException", "div by 0");
                    }

                default:
                    assert (false);
                    return this;
                }
            }
        }
    }
}