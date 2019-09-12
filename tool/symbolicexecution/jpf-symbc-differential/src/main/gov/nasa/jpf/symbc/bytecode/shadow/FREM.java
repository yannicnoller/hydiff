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

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Remainder float ..., value1, value2 => ..., result
 */
public class FREM extends gov.nasa.jpf.jvm.bytecode.FREM {

    @Override
    public Instruction execute(ThreadInfo th) {
        StackFrame sf = th.getModifiableTopFrame();
        Object shadow_v1 = sf.getOperandAttr();
        float v1 = sf.popFloat();
        Object shadow_v2 = sf.getOperandAttr();
        float v2 = sf.popFloat();

        if (shadow_v1 == null && shadow_v2 == null) {
            if (v1 == 0) {
                return th.createAndThrowException("java.lang.ArithmeticException", "division by zero");
            }
            sf.pushFloat(v2 % v1);
        } else {
            sf.push(0, false);
            throw new RuntimeException("## Error: SYMBOLIC FREM not supported");
        }

        return getNext(th);
    }

}
