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

//
// Copyright (C) 2007 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.symbc.numeric;

import java.util.HashMap;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.IntChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;

public class PCChoiceGenerator extends IntIntervalGenerator {

    /* jpf-shadow start modification */
    protected boolean choicesSet = false;

    public boolean choicesSet() {
        return this.choicesSet;
    }

    public void resetAndSetChoices(int min, int max, int delta) {
        isDone = false;
        if (delta == 0) {
            throw new JPFException("PCChoiceGenerator delta value is 0");
        }
        this.min = min;
        this.max = max;
        this.delta = delta;
        /*
         * if (min > max) { int t = max; this.max = min; this.min = t; }
         * 
         * if (delta > 0) { this.next = min; } else { this.next = max; }
         */
        this.next = min;
        choicesSet = true;
    }

    public PCChoiceGenerator(String id, int min, int max, int delta) {
        super(id, min, max, delta);
        PC = new HashMap<Integer, PathCondition>();
        for (int i = min; i <= max; i += delta) {
            PC.put(i, new PathCondition());
        }
        isReverseOrder = false;
    }

    public PCChoiceGenerator(String id, int min, int max) {
        this(id, min, max, 1);
    }

    public PCChoiceGenerator(String id, int size) {
        this(id, 0, size - 1);
    }

    public void setExecutionMode(ThreadInfo.Execute newExecutionMode) {
        executionMode = newExecutionMode;
    }

    public ThreadInfo.Execute getExecutionMode() {
        return executionMode;
    }

    private ThreadInfo.Execute executionMode = Execute.BOTH;
    /* jpf-shadow end modification */

    // protected PathCondition[] PC;
    protected HashMap<Integer, PathCondition> PC;
    boolean isReverseOrder;

    int offset; // to be used in the CFG

    public int getOffset() {
        return offset;
    }

    public void setOffset(int off) {
        // if(SymbolicInstructionFactory.debugMode) System.out.println("offset "+off);
        offset = off;
    }

    String methodName; // to be used in the CFG

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String name) {
        // if(SymbolicInstructionFactory.debugMode) System.out.println("methodName "+ name);
        methodName = name;
    }

    @SuppressWarnings("deprecation")
    public PCChoiceGenerator(int size) {
        super(0, size - 1);
        PC = new HashMap<Integer, PathCondition>();
        for (int i = 0; i < size; i++)
            PC.put(i, new PathCondition());
        isReverseOrder = false;
    }

    public PCChoiceGenerator(int min, int max) {
        this(min, max, 1);
    }

    @SuppressWarnings("deprecation")
    public PCChoiceGenerator(int min, int max, int delta) {
        super(min, max, delta);
        PC = new HashMap<Integer, PathCondition>();
        for (int i = min; i <= max; i += delta)
            PC.put(i, new PathCondition());
        isReverseOrder = false;
    }

    /*
     * If reverseOrder is true, the PCChoiceGenerator explores paths in the opposite order used by the default
     * constructor. If reverseOrder is false the usual behavior is used.
     */
    @SuppressWarnings("deprecation")
    public PCChoiceGenerator(int size, boolean reverseOrder) {
        super(0, size - 1, reverseOrder ? -1 : 1);
        PC = new HashMap<Integer, PathCondition>();
        for (int i = 0; i < size; i++)
            PC.put(i, new PathCondition());
        isReverseOrder = reverseOrder;
    }

    public boolean isReverseOrder() {
        return isReverseOrder;
    }

    // sets the PC constraints for the current choice
    public void setCurrentPC(PathCondition pc) {
        PC.put(getNextChoice(), pc);

    }

    // sets the PC constraints for the specified choice
    public void setPC(PathCondition pc, int choice) {
        PC.put(new Integer(choice), pc);

    }

    // returns the PC constraints for the current choice
    public PathCondition getCurrentPC() {
        PathCondition pc;

        pc = PC.get(getNextChoice());
        if (pc != null) {
            return pc.make_copy();
        } else {
            return null;
        }
    }

    public IntChoiceGenerator randomize() {
        return new PCChoiceGenerator(PC.size(), random.nextBoolean());
    }

    public void setNextChoice(int nextChoice) {
        super.next = nextChoice;
    }

    public void selectGuidedChoice(int choice) {
        if (delta == 1) {
            super.select(choice);
        } else {
            /*
             * Own setting because the standard selection will not use the delta and just iteratively call advance()
             * that might to a much higher choice value than actually possible.
             */
            reset();
            next = choice;
            setDone();
        }
    }

}
