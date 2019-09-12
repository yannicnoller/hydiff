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

import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Rody Kersten instruction counting cost model
 * 
 *         added builder factory Yannic Noller <nolleryc@gmail.com> - YN
 */
public final class InstructionCountState extends State {

    public final static String ID = "instructions";

    public final static class InstructionBuilderFactory extends StateBuilderFactory {

        @Override
        public StateBuilder createStateBuilder() {
            return new InstructionCountStateBuilder();
        }

    }

    public final static class InstructionCountStateBuilder extends StateBuilderAdapter {

        public InstructionCountStateBuilder() {
        }

        private InstructionCountStateBuilder(double oldInstrCount, double newInstrCount) {
            Observations.lastMeasuredMetricValueOldVersion = oldInstrCount;
            Observations.lastMeasuredMetricValueNewVersion = newInstrCount;
        }

        @Override
        public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
                Instruction executedInstruction) {
            if (executedInstruction.getMethodInfo().isInitOrClinit()) {
                return;
            }
            if (executedInstruction.getMethodInfo().isHidden()) {
                return;
            }
            if (executedInstruction.getMethodInfo().isInternalMethod()) {
                return;
            }
            if (executedInstruction.getMethodInfo().isJPFInternal()) {
                return;
            }
            if (executedInstruction.getMethodInfo().isNative()) {
                return;
            }
            if (executedInstruction.getMethodInfo().isMJI()) {
                return;
            }
            if (executedInstruction.getMethodInfo().getName().contains("<clinit>")) {
                return;
            }
            if (executedInstruction.getMethodInfo().getName().contains("main")) {
                return;
            }
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

        @Override
        public StateBuilder copy() {
            return new InstructionCountStateBuilder(Observations.lastMeasuredMetricValueOldVersion,
                    Observations.lastMeasuredMetricValueNewVersion);
        }

        @Override
        public State build(PathCondition resultingPC) {
            return new InstructionCountState(Observations.lastMeasuredMetricValueOldVersion,
                    Observations.lastMeasuredMetricValueNewVersion, resultingPC);
        }

    }

    private final double oldInstrCount;
    private final double newInstrCount;

    private InstructionCountState(double oldInstrCount, double newInstrCount, PathCondition pc) {
        super(pc);
        this.oldInstrCount = oldInstrCount;
        this.newInstrCount = newInstrCount;
    }

    @Override
    public int compareTo(State o) {
        if (!(o instanceof InstructionCountState)) {
            throw new IllegalStateException("Expected state of type " + InstructionCountState.class.getName());
        }
        InstructionCountState other = (InstructionCountState) o;
        /* Compare only new instruction count. */
        return this.newInstrCount < other.newInstrCount ? -1 : this.newInstrCount > other.newInstrCount ? 1 : 0;
    }

    public double getOldInstructionCount() {
        return this.oldInstrCount;
    }

    public double getNewInstructionCount() {
        return this.newInstrCount;
    }

    @Override
    public double getOldWC() {
        return this.getOldInstructionCount();
    }

    @Override
    public double getNewWC() {
        return this.getNewInstructionCount();
    }
}
