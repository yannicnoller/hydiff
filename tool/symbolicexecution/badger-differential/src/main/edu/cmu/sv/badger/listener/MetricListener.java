package edu.cmu.sv.badger.listener;

import edu.cmu.sv.badger.analysis.StateBuilder;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class MetricListener extends PropertyListenerAdapter {

    private StateBuilder stateBuilder;

    public MetricListener(Config jpfConf, JPF jpf, StateBuilder stateBuilder) {
        this.stateBuilder = stateBuilder;
    }

    @Override
    public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
        if (!currentThread.isFirstStepInsn()) {
            this.stateBuilder.handleExecuteInstruction(vm, currentThread, instructionToExecute);
        }
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
            Instruction executedInstruction) {
        this.stateBuilder.handleInstructionExecuted(vm, currentThread, nextInstruction, executedInstruction);
    }

}
