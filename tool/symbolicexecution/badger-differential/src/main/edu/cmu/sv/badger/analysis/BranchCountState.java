/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.badger.analysis;

import gov.nasa.jpf.jvm.bytecode.DCMPG;
import gov.nasa.jpf.jvm.bytecode.DCMPL;
import gov.nasa.jpf.jvm.bytecode.FCMPG;
import gov.nasa.jpf.jvm.bytecode.FCMPL;
import gov.nasa.jpf.jvm.bytecode.GOTO;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.jvm.bytecode.JSR;
import gov.nasa.jpf.jvm.bytecode.LCMP;
import gov.nasa.jpf.jvm.bytecode.SwitchInstruction;
import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Yannic Noller <nolleryc@gmail.com> - YN branch instruction counting cost model
 */
public final class BranchCountState extends State {

    public final static String ID = "jumps";

    public final static class BranchBuilderFactory extends StateBuilderFactory {

        @Override
        public StateBuilder createStateBuilder() {
            return new BranchCountStateBuilder();
        }

    }

    public final static class BranchCountStateBuilder extends StateBuilderAdapter {

        public BranchCountStateBuilder() {
        }

        private BranchCountStateBuilder(double oldInstrCount, double newInstrCount) {
            Observations.lastMeasuredMetricValueOldVersion = oldInstrCount;
            Observations.lastMeasuredMetricValueNewVersion = newInstrCount;
        }

        @Override
        public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
                Instruction executedInstruction) {
            ChoiceGenerator<?> cg = vm.getChoiceGenerator();
            if (cg != null && cg instanceof PCChoiceGenerator) {
                if (executedInstruction.getMethodInfo().isInitOrClinit()) {
                    return;
                }
                if (executedInstruction.getMethodInfo().getName().contains("main")) {
                    return;
                }
                if (isBranchInstruction(executedInstruction)) {
                    switch (currentThread.getExecutionMode()) {
                    case BOTH:
                        Observations.lastMeasuredMetricValueOldVersion++;
                        Observations.lastMeasuredMetricValueNewVersion++;
                        break;
                    case NEW:
                        Observations.lastMeasuredMetricValueNewVersion++;
                        break;
                    case OLD:
                        Observations.lastMeasuredMetricValueOldVersion++;
                        break;
                    default:
                        throw new RuntimeException("Unknown execution mode: " + currentThread.getExecutionMode());
                    }
                }
            }
        }

        private boolean isBranchInstruction(Instruction executedInstruction) {
            if (executedInstruction instanceof IfInstruction) {
                return true;
            }

            if (executedInstruction instanceof SwitchInstruction) {
                return true;
            }

            if (executedInstruction instanceof GOTO) {
                return true;
            }
            if (executedInstruction instanceof JSR) {
                return true;
            }
            if (executedInstruction instanceof LCMP) {
                return true;
            }

            if (executedInstruction instanceof LCMP) {
                return true;
            }
            if (executedInstruction instanceof FCMPL) {
                return true;
            }
            if (executedInstruction instanceof FCMPG) {
                return true;
            }
            if (executedInstruction instanceof DCMPL) {
                return true;
            }
            if (executedInstruction instanceof DCMPG) {
                return true;
            }

            return false;
        }

        @Override
        public StateBuilder copy() {
            return new BranchCountStateBuilder(Observations.lastMeasuredMetricValueOldVersion,
                    Observations.lastMeasuredMetricValueNewVersion);
        }

        @Override
        public State build(PathCondition resultingPC) {
            return new BranchCountState(Observations.lastMeasuredMetricValueOldVersion,
                    Observations.lastMeasuredMetricValueNewVersion, resultingPC);
        }

    }

    private final double oldInstrCount;
    private final double newInstrCount;

    private BranchCountState(double oldInstrCount, double newInstrCount, PathCondition pc) {
        super(pc);
        this.oldInstrCount = oldInstrCount;
        this.newInstrCount = newInstrCount;
    }

    @Override
    public int compareTo(State o) {
        if (!(o instanceof BranchCountState)) {
            throw new IllegalStateException("Expected state of type " + BranchCountState.class.getName());
        }
        BranchCountState other = (BranchCountState) o;
        /* Compare only new instruction count. */
        return this.newInstrCount < other.newInstrCount ? -1 : this.newInstrCount > other.newInstrCount ? 1 : 0;
    }

    public double getOldBranchInstructionCount() {
        return this.oldInstrCount;
    }

    public double getNewBranchInstructionCount() {
        return this.newInstrCount;
    }

    @Override
    public double getOldWC() {
        return this.getOldBranchInstructionCount();
    }

    @Override
    public double getNewWC() {
        return this.getNewBranchInstructionCount();
    }
}
