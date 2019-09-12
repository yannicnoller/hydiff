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

import static gov.nasa.jpf.symbc.bytecode.shadow.ShadowBytecodeUtils.addDet;
import static gov.nasa.jpf.symbc.bytecode.shadow.ShadowBytecodeUtils.getChoiceForConditional;
import static gov.nasa.jpf.symbc.bytecode.shadow.ShadowBytecodeUtils.getChoiceForConditionals;
import static gov.nasa.jpf.symbc.bytecode.shadow.ShadowBytecodeUtils.newConditionValues;
import static gov.nasa.jpf.symbc.bytecode.shadow.ShadowBytecodeUtils.oldConditionValues;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.PathCondition.Diff;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;

/**
 * Compare long ..., value1, value2 => ..., result
 */
public class LCMP extends gov.nasa.jpf.jvm.bytecode.LCMP {

    @Override
    public Instruction execute(ThreadInfo ti) {
        StackFrame sf = ti.getModifiableTopFrame();

        Object op_v1 = sf.getOperandAttr(1);
        Object op_v2 = sf.getOperandAttr(3);

        if (op_v1 == null && op_v2 == null) { // both conditions are concrete
            return super.execute(ti);
        } else { // at least one condition is symbolic
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
                    boolean diffPossible = ShadowBytecodeUtils.checkOperandsForPotentialDiff(op_v1, op_v2);
                    if (diffPossible && ti.getExecutionMode() == Execute.BOTH && !pc.isDiffPC()
                            && !ShadowBytecodeUtils.isChangeBoolean(this, ti)) {
                        nextCg = new PCChoiceGenerator(9);
                    } else {
                        nextCg = new PCChoiceGenerator(0, 6, 3);
                    }
                }
                nextCg.setOffset(this.position);
                nextCg.setMethodName(this.getMethodInfo().getFullName());
                nextCg.setExecutionMode(ti.getExecutionMode());
                ti.getVM().getSystemState().setNextChoiceGenerator(nextCg);
                return this;
            } else {

                // This actually returns the next instruction
                PCChoiceGenerator curCg = (PCChoiceGenerator) ti.getVM().getSystemState().getChoiceGenerator();
                long v1 = sf.popLong();
                long v2 = sf.popLong();

                PathCondition pc;
                PCChoiceGenerator prevCg = curCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

                if (prevCg == null) {
                    pc = new PathCondition();
                } else {
                    pc = prevCg.getCurrentPC();
                }

                assert (pc != null);

                IntegerExpression oldSym_v1 = ShadowBytecodeUtils.getOldSymbolicExpr(op_v1, v1);
                IntegerExpression oldSym_v2 = ShadowBytecodeUtils.getOldSymbolicExpr(op_v2, v2);
                IntegerExpression oldCon_v1 = ShadowBytecodeUtils.getOldConcreteExpr(op_v1, v1);
                IntegerExpression oldCon_v2 = ShadowBytecodeUtils.getOldConcreteExpr(op_v2, v2);

                IntegerExpression newSym_v1 = ShadowBytecodeUtils.getNewSymbolicExpr(op_v1, v1);
                IntegerExpression newSym_v2 = ShadowBytecodeUtils.getNewSymbolicExpr(op_v2, v2);
                IntegerExpression newCon_v1 = ShadowBytecodeUtils.getNewConcreteExpr(op_v1, v1);
                IntegerExpression newCon_v2 = ShadowBytecodeUtils.getNewConcreteExpr(op_v2, v2);

                ti.setExecutionMode(curCg.getExecutionMode());

                if (SymbolicInstructionFactory.collect_constraints) {

                    int oldCon_conditVal = super.conditionValue(oldCon_v1.solution(), oldCon_v2.solution());
                    int newCon_conditVal = super.conditionValue(newCon_v1.solution(), newCon_v2.solution());

                    if (pc.isDiffPC()) {
                        // Only execute new version as soon as we are in a diff path
                        addDet(newCon_conditVal, pc, newSym_v2, newSym_v1);
                        curCg.select(getChoiceForConditional(newCon_conditVal));
                        curCg.setCurrentPC(pc);
                        sf.push(newCon_conditVal, false);
                        return this.getNext(ti);
                    } else if (!pc.containsDiffExpr()) {
                        // Both follow the same concrete path.
                        if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                            addDet(newCon_conditVal, pc, newSym_v2, newSym_v1);
                        }
                        if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                            addDet(oldCon_conditVal, pc, oldSym_v2, oldSym_v1);
                        }
                        curCg.select(getChoiceForConditional(oldCon_conditVal));
                        sf.push(oldCon_conditVal, false);
                        curCg.setCurrentPC(pc);
                        return this.getNext(ti);
                    } else if (ti.getExecutionMode() == Execute.BOTH) {
                        addDet(newCon_conditVal, pc, newSym_v2, newSym_v1);
                        addDet(oldCon_conditVal, pc, oldSym_v2, oldSym_v1);
                        curCg.select(getChoiceForConditionals(oldCon_conditVal, newCon_conditVal));
                        Instruction nextInstr = this.getNext(ti);
                        /*
                         * Based on the code pattern (what is the following instruction type?) we can select the diff
                         * path.
                         */
                        // TODO
                        if (oldCon_conditVal != newCon_conditVal) {
                            if (nextInstr instanceof IFLE) {
                                /* ">" in src */
                                if (newCon_conditVal == 1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else if (oldCon_conditVal == 1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else {
                                    // no diff, both take the <= path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFLT) {
                                /* ">=" in src */
                                if (newCon_conditVal == -1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else if (oldCon_conditVal == -1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else {
                                    // no diff, both take the >= path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFGE) {
                                /* "<" in src */
                                if (newCon_conditVal == -1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else if (oldCon_conditVal == -1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else {
                                    // no diff, both take the >= path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFGT) {
                                /* "<=" in src */
                                if (newCon_conditVal == 1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else if (oldCon_conditVal == 1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else {
                                    // no diff, both take the <= path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFNE) {
                                /* == in src */
                                if (newCon_conditVal == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else if (oldCon_conditVal == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else {
                                    // no diff, both take the != path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFEQ) {
                                /* != in src */
                                if (newCon_conditVal == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else if (oldCon_conditVal == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else {
                                    // no diff, both take the == path, should be not reachable.
                                }
                            } else {
                                /*
                                 * Pattern does not match, it could be that there is another construct (e.g., if
                                 * (..||..)) which follows different bytecode generation. The just us a diffTrue path.
                                 */
                                pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                            }
                        }
                        curCg.setCurrentPC(pc);
                        sf.push(newCon_conditVal, false);
                        return this.getNext(ti);
                    } else {
                        if (ti.getExecutionMode() == Execute.OLD) {
                            addDet(oldCon_conditVal, pc, oldSym_v2, oldSym_v1);
                            curCg.select(getChoiceForConditional(oldCon_conditVal));
                            curCg.setCurrentPC(pc);
                            sf.push(oldCon_conditVal, false);
                            return this.getNext(ti);
                        } else {
                            addDet(newCon_conditVal, pc, newSym_v2, newSym_v1);
                            curCg.select(getChoiceForConditional(newCon_conditVal));
                            curCg.setCurrentPC(pc);
                            sf.push(newCon_conditVal, false);
                            return this.getNext(ti);
                        }
                    }

                } else {
                    /* Full four-way symbolic forking, i.e. here nine-way symbolic forking */
                    int choice = curCg.getNextChoice();
                    
                    int newConditionValue = newConditionValues[choice];
                    int oldConditionValue = oldConditionValues[choice];

                    if (ti.getExecutionMode() == Execute.NEW || ti.getExecutionMode() == Execute.BOTH) {
                        addDet(newConditionValue, pc, newSym_v2, newSym_v1);
                    }

                    if (ti.getExecutionMode() == Execute.OLD || ti.getExecutionMode() == Execute.BOTH) {
                        if (!pc.isDiffPC()) {
                            addDet(oldConditionValue, pc, oldSym_v2, oldSym_v1);
                        }
                    }

                    Instruction nextInstr = this.getNext(ti);
                    if (!pc.simplify()) {
                        // path not feasible
                        ti.getVM().getSystemState().setIgnored(true);
                    } else {
                        if (oldConditionValue != newConditionValue) {
                            /*
                             * Assuming the following byte code pattern for source code comparison: > -> DCMPL + IFLE >=
                             * -> DCMPL + IFLT < -> DCMPG + IFGE <= -> DCMPG + IFGT , we can select diffTrue or
                             * diffFalse path.
                             */
                            if (nextInstr instanceof IFLE) {
                                /* ">" in src */
                                if (newConditionValue == 1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else if (oldConditionValue == 1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else {
                                    // no diff, both take the <= path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFLT) {
                                /* ">=" in src */
                                if (newConditionValue == -1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else if (oldConditionValue == -1) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else {
                                    // no diff, both take the >= path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFNE) {
                                /* == in src */
                                if (newConditionValue == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else if (oldConditionValue == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else {
                                    // no diff, both take the != path, conditionValue still can be different.
                                }
                            } else if (nextInstr instanceof IFEQ) {
                                /* != in src */
                                if (newConditionValue == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffFalse);
                                } else if (oldConditionValue == 0) {
                                    pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                                } else {
                                    // no diff, both take the == path, should be not reachable.
                                }
                            } else {
                                /*
                                 * Pattern does not match, it could be that there is another construct (e.g., if
                                 * (..||..)) which follows different bytecode generation. The just us a diffTrue path.
                                 */
                                pc.markAsDiffPC(this.getLineNumber(), Diff.diffTrue);
                            }
                        }
                        curCg.setCurrentPC(pc);
                    }
                    sf.push(newConditionValue, false);
                    return this.getNext(ti);
                }
            }

        }

    }

}
