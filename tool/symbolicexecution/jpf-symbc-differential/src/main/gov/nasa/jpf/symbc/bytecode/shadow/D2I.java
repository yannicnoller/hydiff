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

import gov.nasa.jpf.symbc.numeric.*;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

/**
 * Convert double to int ..., value => ..., result
 */
public class D2I extends gov.nasa.jpf.jvm.bytecode.D2I {

    @Override
    public Instruction execute(ThreadInfo ti) {
        Object sym_dval = ti.getModifiableTopFrame().getLongOperandAttr();

        if (sym_dval == null) {
            return super.execute(ti);
        } else {
            // here we get a hold of the current path condition and
            // add an extra mixed constraint sym_dval==sym_ival

            ChoiceGenerator<?> cg;
            if (!ti.isFirstStepInsn()) { // first time around
                cg = new PCChoiceGenerator(1); // only one choice
                ti.getVM().getSystemState().setNextChoiceGenerator(cg);
                return this;
            } else { // this is what really returns results
                cg = ti.getVM().getSystemState().getChoiceGenerator();
                assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
            }

            // get the path condition from the
            // previous choice generator of the same type

            PathCondition pc;
            ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);

            if (prev_cg == null)
                pc = new PathCondition(); // TODO: handling of preconditions needs to be changed
            else
                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();
            assert pc != null;

            StackFrame sf = ti.getModifiableTopFrame();
            double v_d = Types.longToDouble(sf.popLong());
            int v_i = (int) v_d;
            sf.push(v_i);

            RealExpression oldSym_v = ShadowBytecodeUtils.getOldSymbolicExpr(sym_dval, v_d);
            RealExpression oldCon_v = ShadowBytecodeUtils.getOldConcreteExpr(sym_dval, v_d);
            RealExpression newSym_v = ShadowBytecodeUtils.getNewSymbolicExpr(sym_dval, v_d);
            RealExpression newCon_v = ShadowBytecodeUtils.getNewConcreteExpr(sym_dval, v_d);

            SymbolicInteger newSym_intValue = new SymbolicInteger();
            pc._addDet(Comparator.EQ, newSym_intValue, newSym_v);
            IntegerExpression newCon_intValue = new IntegerConstant((int) newCon_v.solution());

            if (sym_dval instanceof DiffExpression) {
                SymbolicInteger oldSym_intValue = new SymbolicInteger();
                pc._addDet(Comparator.EQ, oldSym_intValue, oldSym_v);
                IntegerExpression oldCon_intValue = new IntegerConstant((int) oldCon_v.solution());
                DiffExpression result_ival = new DiffExpression(oldSym_intValue, newSym_intValue, oldCon_intValue,
                        newCon_intValue);
                sf.setOperandAttr(result_ival);
            } else {
                sf.setOperandAttr(newCon_intValue);
            }

            if (!pc.simplify()) { // not satisfiable
                ti.getVM().getSystemState().setIgnored(true);
            } else {
                ((PCChoiceGenerator) cg).setCurrentPC(pc);
            }

            return getNext(ti);
        }
    }
}
