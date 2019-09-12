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
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.ShadowPCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition.Diff;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;
import gov.nasa.jpf.symbc.numeric.ExecExpression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;

public class IFEQ extends gov.nasa.jpf.jvm.bytecode.IFEQ {
    public IFEQ(int targetPosition) {
        super(targetPosition);
    }

    @Override
    public Instruction execute(ThreadInfo ti) {
        StackFrame sf = ti.getModifiableTopFrame();
        Object op_v = sf.getOperandAttr();

        if (op_v == null) { // the condition is concrete
            return super.execute(ti);
        } else { // The condition is symbolic

            if (op_v instanceof ExecExpression) {

                if (((ExecExpression) op_v).getExecutionMode() == 1) {
                    // if(execute(OLD))

                    if (!ti.isFirstStepInsn()) {
                        // First we have to check whether we are currently exploring a diffpath
                        // in this case, we will skip the whole execute(OLD) block
                        ChoiceGenerator<?> curCg = ti.getVM().getSystemState().getChoiceGenerator();

                        PCChoiceGenerator curPcCg;
                        if (curCg instanceof PCChoiceGenerator) {
                            curPcCg = (PCChoiceGenerator) curCg;
                        } else {
                            curPcCg = curCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
                        }
                        if (curPcCg != null) {
                            PathCondition pc = curPcCg.getCurrentPC();
                            if (pc.isDiffPC()) {
                                ti.getModifiableTopFrame().pop();
                                return this.getTarget();
                            }
                        }

                        ShadowPCChoiceGenerator shadowCg = new ShadowPCChoiceGenerator(1, this.getTarget(),
                                this.getMethodInfo());
                        shadowCg.setOffset(this.getPosition());
                        shadowCg.setMethodName(this.getMethodInfo().getFullName());
                        shadowCg.setExecutionMode(Execute.OLD);
                        ti.getVM().getSystemState().setNextChoiceGenerator(shadowCg);
                        return this;

                    } else {

                        ti.getModifiableTopFrame().pop();
                        ShadowPCChoiceGenerator shadowCg = ((ShadowPCChoiceGenerator) ti.getVM().getSystemState()
                                .getChoiceGenerator());
                        PCChoiceGenerator prevCg = shadowCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

                        PathCondition pc;
                        if (prevCg == null) {
                            pc = new PathCondition();
                        } else {
                            pc = prevCg.getCurrentPC();
                        }

                        assert (pc != null);
                        ti.setExecutionMode(shadowCg.getExecutionMode());
                        assert (ti.getExecutionMode() == Execute.OLD);
                        shadowCg.setCurrentPC(pc);

                        Instruction next = this.getNext();
                        return next; // start executing the block of old version

                    }
                } else {
                    // execute(NEW)

                    if (!ti.isFirstStepInsn()) {

                        ShadowPCChoiceGenerator shadowCg = new ShadowPCChoiceGenerator(1, this.getTarget(),
                                this.getMethodInfo());
                        shadowCg.setOffset(this.getPosition());
                        shadowCg.setMethodName(this.getMethodInfo().getFullName());
                        shadowCg.setExecutionMode(Execute.NEW);
                        ti.getVM().getSystemState().setNextChoiceGenerator(shadowCg);
                        return this;

                    } else {

                        ti.getModifiableTopFrame().pop();
                        ShadowPCChoiceGenerator shadowCg = ((ShadowPCChoiceGenerator) ti.getVM().getSystemState()
                                .getChoiceGenerator());
                        PCChoiceGenerator prevCg = shadowCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

                        PathCondition pc;
                        if (prevCg == null) {
                            pc = new PathCondition();
                        } else {
                            pc = prevCg.getCurrentPC();
                        }

                        assert (pc != null);
                        ti.setExecutionMode(shadowCg.getExecutionMode());
                        assert (ti.getExecutionMode() == Execute.NEW);
                        shadowCg.setCurrentPC(pc);

                        Instruction next = this.getNext();
                        return next; // start executing the block of new version
                    }
                }
            }

            // no if(execute(old)) or if(execute(new)) branch, proceed as usual

            if (!ti.isFirstStepInsn()) {
                ChoiceGenerator<?> curCg = ti.getVM().getSystemState().getChoiceGenerator();
                PCChoiceGenerator curPcCg;
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
                    boolean diffPossible = ShadowBytecodeUtils.checkOperandsForPotentialDiff(op_v);
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
                ti.getVM().getSystemState().setNextChoiceGenerator(nextCg);
                return this;

            } else {
                // "Lower part" of cg method, process choice now
                PCChoiceGenerator curCg = (PCChoiceGenerator) ti.getVM().getSystemState().getChoiceGenerator();

                int v = ti.getModifiableTopFrame().pop();

                // Get current pc from previous cg
                PCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
                PathCondition pc;
                if (prevCg == null) {
                    pc = new PathCondition();
                } else {
                    pc = prevCg.getCurrentPC();
                }

                assert (pc != null);

                // Get symbolic and shadow expressions
                IntegerExpression oldSym_v = ShadowBytecodeUtils.getOldSymbolicExpr(op_v, v);
                IntegerExpression oldCon_v = ShadowBytecodeUtils.getOldConcreteExpr(op_v, v);

                IntegerExpression newSym_v = ShadowBytecodeUtils.getNewSymbolicExpr(op_v, v);
                IntegerExpression newCon_v = ShadowBytecodeUtils.getNewConcreteExpr(op_v, v);

                ti.setExecutionMode(curCg.getExecutionMode());

                /* Symcrete execution, determine concrete choice. */
                // choice = 0 means NE 0 -> ret next
                // choice = 1 means EQ 0 -> ret target
                if (SymbolicInstructionFactory.collect_constraints) {

                    boolean oldConcrete_result = oldCon_v.solutionInt() == 0;
                    boolean newConcrete_result = newCon_v.solutionInt() == 0;

                    if (pc.isDiffPC()) {
                        // Only execute new version as soon as we are in a diff path
                        if (!(newSym_v instanceof IntegerConstant)) {
                            pc._addDet(newConcrete_result ? Comparator.EQ : Comparator.NE, newSym_v, 0);
                        }
                        curCg.select(newConcrete_result ? 1 : 0);
                        curCg.setCurrentPC(pc);
                        return newConcrete_result ? this.getTarget() : this.getNext(ti);
                    } else if (!pc.containsDiffExpr()) {
                        if (!oldConcrete_result) {
                            // Both versions follow the true path in src
                            curCg.select(0);
                            if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                                if (!(newSym_v instanceof IntegerConstant)) {
                                    pc._addDet(Comparator.NE, newSym_v, 0);
                                }
                            }
                            if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                                if (!(oldSym_v instanceof IntegerConstant)) {
                                    pc._addDet(Comparator.NE, oldSym_v, 0);
                                }
                            }
                            curCg.setCurrentPC(pc);
                            return this.getNext(ti);
                        } else {
                            // Both versions follow the false path in src
                            curCg.select(1);
                            if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                                if (!(newSym_v instanceof IntegerConstant)) {
                                    pc._addDet(Comparator.EQ, newSym_v, 0);
                                }
                            }
                            if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                                if (!(oldSym_v instanceof IntegerConstant)) {
                                    pc._addDet(Comparator.EQ, oldSym_v, 0);
                                }
                            }
                            curCg.setCurrentPC(pc);
                            return this.getTarget();
                        }
                    } else if (ti.getExecutionMode() == Execute.BOTH) {
                        if (!oldConcrete_result && !newConcrete_result) {
                            // Both versions follow the true path in src
                            curCg.select(0);
                            if (!(newSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.NE, newSym_v, 0);
                            }
                            if (!(oldSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.NE, oldSym_v, 0);
                            }
                            curCg.setCurrentPC(pc);
                            return this.getNext(ti);
                        } else if (oldConcrete_result && newConcrete_result) {
                            // Both versions follow the false path in src
                            curCg.select(1);
                            if (!(newSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.EQ, newSym_v, 0);
                            }
                            if (!(oldSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.EQ, oldSym_v, 0);
                            }
                            curCg.setCurrentPC(pc);
                            return this.getTarget();
                        } else if (oldConcrete_result && !newConcrete_result) {
                            // Diff true path in src (new true, old false)
                            curCg.select(2);
                            if (!(newSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.NE, newSym_v, 0);
                            }
                            if (!(oldSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.EQ, oldSym_v, 0);
                            }
                            pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                            curCg.setCurrentPC(pc);
                            return this.getNext(ti); // continue with new version
                        } else {
                            // Diff false path in src (new false, old true)
                            curCg.select(3);
                            if (!(newSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.EQ, newSym_v, 0);
                            }
                            if (!(oldSym_v instanceof IntegerConstant)) {
                                pc._addDet(Comparator.NE, oldSym_v, 0);
                            }
                            pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                            curCg.setCurrentPC(pc);
                            return this.getTarget(); // continue with new version
                        }
                    } else {
                        // Evaluating the old or new expression in an if(change(boolean,boolean)) stmt
                        if (ti.getExecutionMode() == Execute.OLD) {
                            if (!(oldSym_v instanceof IntegerConstant)) {
                                pc._addDet(oldConcrete_result ? Comparator.EQ : Comparator.NE, oldSym_v, 0);
                            }
                            curCg.select(oldConcrete_result ? 1 : 0);
                            curCg.setCurrentPC(pc);
                            return oldConcrete_result ? this.getTarget() : this.getNext(ti);
                        } else {
                            if (!(newSym_v instanceof IntegerConstant)) {
                                pc._addDet(newConcrete_result ? Comparator.EQ : Comparator.NE, newSym_v, 0);
                            }
                            curCg.select(newConcrete_result ? 1 : 0);
                            curCg.setCurrentPC(pc);
                            return newConcrete_result ? this.getTarget() : this.getNext(ti);
                        }
                    }

                } else {
                    /*
                     * Full four-way symbolic forking.
                     */
                    int choice = curCg.getNextChoice();
                    switch (choice) {
                    case 0: // True path in src
                        if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.NE, newSym_v, 0);
                        }
                        if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                            if (!pc.isDiffPC()) { // ignore old for diff path
                                pc._addDet(Comparator.NE, oldSym_v, 0);
                            }
                        }
                        if (!pc.simplify()) {
                            ti.getVM().getSystemState().setIgnored(true);
                        } else {
                            curCg.setCurrentPC(pc);
                        }
                        return this.getNext(ti);
                    case 1: // False path in src
                        if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                            pc._addDet(Comparator.EQ, newSym_v, 0);
                        }
                        if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                            if (!pc.isDiffPC()) { // ignore old for diff path
                                pc._addDet(Comparator.EQ, oldSym_v, 0);
                            }
                        }
                        if (!pc.simplify()) {
                            // Path is not feasible
                            ti.getVM().getSystemState().setIgnored(true);
                        } else {
                            curCg.setCurrentPC(pc);
                        }
                        return this.getTarget();
                    case 2: // Diff true path in src (new true, old false)
                        pc._addDet(Comparator.NE, newSym_v, 0);
                        pc._addDet(Comparator.EQ, oldSym_v, 0);
                        if (!pc.simplify()) {
                            ti.getVM().getSystemState().setIgnored(true);
                        } else {
                            pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                            curCg.setCurrentPC(pc);
                        }
                        return this.getNext(ti); // continue with new version

                    case 3: // Diff false path in src (new false, old true)
                        pc._addDet(Comparator.EQ, newSym_v, 0);
                        pc._addDet(Comparator.NE, oldSym_v, 0);
                        if (!pc.simplify()) {
                            ti.getVM().getSystemState().setIgnored(true);
                        } else {
                            pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                            curCg.setCurrentPC(pc);
                        }
                        return this.getTarget(); // continue with new version
                    default:
                        throw new RuntimeException("Unhandled choice in IFEQ: " + choice);
                    }
                }
            }
        }
    }
}
