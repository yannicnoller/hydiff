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

package gov.nasa.jpf.symbc;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.util.Configuration;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.symbc.bytecode.*;
import gov.nasa.jpf.symbc.numeric.MinMax;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemChoco;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemCoral;
import gov.nasa.jpf.util.ClassInfoFilter;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;

public class SymbolicInstructionFactory extends gov.nasa.jpf.jvm.bytecode.InstructionFactory {

    public Instruction aload(int localVarIndex) {
        return filter.isPassing(ci) ? new ALOAD(localVarIndex) : super.aload(localVarIndex);
    }

    public Instruction aload_0() {
        return (filter.isPassing(ci) ? new ALOAD(0) : super.aload_0());
    }

    public Instruction aload_1() {
        return (filter.isPassing(ci) ? new ALOAD(1) : super.aload_1());
    }

    public Instruction aload_2() {
        return (filter.isPassing(ci) ? new ALOAD(2) : super.aload_2());
    }

    public Instruction aload_3() {
        return (filter.isPassing(ci) ? new ALOAD(3) : super.aload_3());
    }

    public Instruction iadd() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IADD();
            } else {
                return new IADD();
            }

        } else {
            return super.iadd();
        }
    }

    public Instruction iand() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IAND();
            } else {
                return new IAND();
            }

        } else {
            return super.iand();
        }
    }

    public Instruction iinc(int localVarIndex, int incConstant) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IINC(localVarIndex, incConstant);
            } else {
                return new IINC(localVarIndex, incConstant);
            }
        } else {
            return super.iinc(localVarIndex, incConstant);
        }
    }

    public Instruction isub() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.ISUB();
            } else {
                return new ISUB();
            }
        } else {
            return super.isub();
        }
    }

    public Instruction imul() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IMUL();
            } else {
                return new IMUL();
            }
        } else {
            return super.imul();
        }
    }

    public Instruction ineg() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.INEG();
            } else {
                return new INEG();
            }
        } else {
            return super.ineg();
        }
    }

    public Instruction ifle(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IFLE(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IFLE(targetPc);
            } else {
                return new IFLE(targetPc);
            }
        } else {
            return super.ifle(targetPc);
        }
    }

    public Instruction iflt(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IFLT(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IFLT(targetPc);
            } else {
                return new IFLT(targetPc);
            }
        } else {
            return super.iflt(targetPc);
        }
    }

    public Instruction ifge(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IFGE(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IFGE(targetPc);
            } else {
                return new IFGE(targetPc);
            }
        } else {
            return super.ifge(targetPc);
        }
    }

    public Instruction ifgt(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IFGT(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IFGT(targetPc);
            } else {
                return new IFGT(targetPc);
            }
        } else {
            return super.ifgt(targetPc);
        }
    }

    public Instruction ifeq(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IFEQ(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IFEQ(targetPc);
            } else {
                return new IFEQ(targetPc);
            }
        } else {
            return super.ifeq(targetPc);
        }
    }

    public Instruction ifne(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IFNE(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IFNE(targetPc);
            } else {
                return new IFNE(targetPc);
            }
        } else {
            return super.ifne(targetPc);
        }
    }

    public Instruction invokestatic(String clsName, String methodName, String methodSignature) {
        return (filter.isPassing(ci) ? new INVOKESTATIC(clsName, methodName, methodSignature)
                : super.invokestatic(clsName, methodName, methodSignature));
    }

    public Instruction invokevirtual(String clsName, String methodName, String methodSignature) {
        return (filter.isPassing(ci) ? new INVOKEVIRTUAL(clsName, methodName, methodSignature)
                : super.invokevirtual(clsName, methodName, methodSignature));
    }

    public Instruction invokeinterface(String clsName, String methodName, String methodSignature) {
        return (filter.isPassing(ci) ? new INVOKEINTERFACE(clsName, methodName, methodSignature)
                : super.invokeinterface(clsName, methodName, methodSignature));
    }

    public Instruction invokespecial(String clsName, String methodName, String methodSignature) {
        return (filter.isPassing(ci) ? new INVOKESPECIAL(clsName, methodName, methodSignature)
                : super.invokespecial(clsName, methodName, methodSignature));
    }

    public Instruction if_icmpge(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IF_ICMPGE(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IF_ICMPGE(targetPc);
            } else {
                return new IF_ICMPGE(targetPc);
            }
        } else {
            return super.if_icmpge(targetPc);
        }
    }

    public Instruction if_icmpgt(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IF_ICMPGT(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IF_ICMPGT(targetPc);
            } else {
                return new IF_ICMPGT(targetPc);
            }
        } else {
            return super.if_icmpgt(targetPc);
        }
    }

    public Instruction if_icmple(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IF_ICMPLE(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IF_ICMPLE(targetPc);
            } else {
                return new IF_ICMPLE(targetPc);
            }
        } else {
            return super.if_icmple(targetPc);
        }
    }

    public Instruction if_icmplt(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IF_ICMPLT(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IF_ICMPLT(targetPc);
            } else {
                return new IF_ICMPLT(targetPc);
            }
        } else {
            return super.if_icmplt(targetPc);
        }
    }

    public Instruction idiv() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IDIV();
            } else {
                return new IDIV();
            }
        } else {
            return super.idiv();
        }
    }

    public Instruction ishl() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.ISHL();
            } else {
                return new ISHL();
            }
        } else {
            return super.ishl();
        }
    }

    public Instruction ishr() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.ISHR();
            } else {
                return new ISHR();
            }
        } else {
            return super.ishr();
        }
    }

    public Instruction iushr() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IUSHR();
            } else {
                return new IUSHR();
            }
        } else {
            return super.iushr();
        }
    }

    public Instruction ixor() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IXOR();
            } else {
                return new IXOR();
            }
        } else {
            return super.ixor();
        }
    }

    public Instruction ior() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IOR();
            } else {
                return new IOR();
            }
        } else {
            return super.ior();
        }
    }

    public Instruction irem() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IREM();
            } else {
                return new IREM();
            }
        } else {
            return super.irem();
        }
    }

    public Instruction if_icmpeq(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IF_ICMPEQ(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IF_ICMPEQ(targetPc);
            } else {
                return new IF_ICMPEQ(targetPc);
            }
        } else {
            return super.if_icmpeq(targetPc);
        }
    }

    public Instruction if_icmpne(int targetPc) {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.IF_ICMPNE(targetPc);
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.IF_ICMPNE(targetPc);
            } else {
                return new IF_ICMPNE(targetPc);
            }
        } else {
            return super.if_icmpne(targetPc);
        }
    }

    public Instruction fadd() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FADD();
            } else {
                return new FADD();
            }
        } else {
            return super.fadd();
        }
    }

    public Instruction fdiv() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FDIV();
            } else {
                return new FDIV();
            }
        } else {
            return super.fdiv();
        }
    }

    public Instruction fmul() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FMUL();
            } else {
                return new FMUL();
            }
        } else {
            return super.fmul();
        }
    }

    public Instruction fneg() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FNEG();
            } else {
                return new FNEG();
            }
        } else {
            return super.fneg();
        }
    }

    public Instruction frem() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FREM();
            } else {
                return new FREM();
            }
        } else {
            return super.frem();
        }
    }

    public Instruction fsub() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FSUB();
            } else {
                return new FSUB();
            }
        } else {
            return super.fsub();
        }
    }

    public Instruction fcmpg() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FCMPG();
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.FCMPG();
            } else {
                return new FCMPG();
            }
        } else {
            return super.fcmpg();
        }
    }

    public Instruction fcmpl() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.FCMPL();
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.FCMPL();
            } else {
                return new FCMPL();
            }
        } else {
            return super.fcmpl();
        }
    }

    public Instruction dadd() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DADD();
            } else {
                return new DADD();
            }
        } else {
            return super.dadd();
        }
    }

    public Instruction dcmpg() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DCMPG();
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.DCMPG();
            } else {
                return new DCMPG();
            }
        } else {
            return super.dcmpg();
        }
    }

    public Instruction dcmpl() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DCMPL();
            } else if (this.pcChoiceOptimization) {
                return new gov.nasa.jpf.symbc.bytecode.optimization.DCMPL();
            } else {
                return new DCMPL();
            }
        } else {
            return super.dcmpl();
        }
    }

    public Instruction ddiv() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DDIV();
            } else {
                return new DDIV();
            }
        } else {
            return super.ddiv();
        }
    }

    public Instruction dmul() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DMUL();
            } else {
                return new DMUL();
            }
        } else {
            return super.dmul();
        }
    }

    public Instruction dneg() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DNEG();
            } else {
                return new DNEG();
            }
        } else {
            return super.dneg();
        }
    }

    public Instruction drem() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DREM();
            } else {
                return new DREM();
            }
        } else {
            return super.drem();
        }
    }

    public Instruction dsub() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.DSUB();
            } else {
                return new DSUB();
            }
        } else {
            return super.dsub();
        }
    }

    public Instruction ladd() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LADD();
            } else {
                return new LADD();
            }
        } else {
            return super.ladd();
        }
    }

    public Instruction land() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LAND();
            } else {
                return new LAND();
            }
        } else {
            return super.land();
        }
    }

    public Instruction lcmp() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LCMP();
            } else {
                return new LCMP();
            }
        } else {
            return super.lcmp();
        }
    }

    public Instruction ldiv() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LDIV();
            } else {
                return new LDIV();
            }
        } else {
            return super.ldiv();
        }
    }

    public Instruction lmul() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LMUL();
            } else {
                return new LMUL();
            }
        } else {
            return super.lmul();
        }
    }

    public Instruction lneg() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LNEG();
            } else {
                return new LNEG();
            }
        } else {
            return super.lneg();
        }
    }

    public Instruction lor() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LOR();
            } else {
                return new LOR();
            }
        } else {
            return super.lor();
        }
    }

    public Instruction lrem() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LREM();
            } else {
                return new LREM();
            }
        } else {
            return super.lrem();
        }
    }

    public Instruction lshl() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LSHL();
            } else {
                return new LSHL();
            }
        } else {
            return super.lshl();
        }
    }

    public Instruction lshr() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LSHR();
            } else {
                return new LSHR();
            }
        } else {
            return super.lshr();
        }
    }

    public Instruction lsub() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LSUB();
            } else {
                return new LSUB();
            }
        } else {
            return super.lsub();
        }
    }

    public Instruction lushr() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LUSHR();
            } else {
                return new LUSHR();
            }
        } else {
            return super.lushr();
        }
    }

    public Instruction lxor() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.LXOR();
            } else {
                return new LXOR();
            }
        } else {
            return super.lxor();
        }
    }

    public Instruction i2d() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.I2D();
            } else {
                return new I2D();
            }
        } else {
            return super.i2d();
        }
    }

    public Instruction d2i() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.D2I();
            } else {
                return new D2I();
            }
        } else {
            return super.d2i();
        }
    }

    public Instruction d2l() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.D2L();
            } else {
                return new D2L();
            }
        } else {
            return super.d2l();
        }
    }

    public Instruction i2f() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.I2F();
            } else {
                return new I2F();
            }
        } else {
            return super.i2f();
        }
    }

    public Instruction l2d() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.L2D();
            } else {
                return new L2D();
            }
        } else {
            return super.l2d();
        }
    }

    public Instruction l2f() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.L2F();
            } else {
                return new L2F();
            }
        } else {
            return super.l2f();
        }
    }

    public Instruction f2l() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.F2L();
            } else {
                return new F2L();
            }
        } else {
            return super.f2l();
        }
    }

    public Instruction f2i() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.F2I();
            } else {
                return new F2I();
            }
        } else {
            return super.f2i();
        }
    }

    public Instruction lookupswitch(int defaultTargetPc, int nEntries) {
        return (filter.isPassing(ci) ? new LOOKUPSWITCH(defaultTargetPc, nEntries)
                : super.lookupswitch(defaultTargetPc, nEntries));
    }

    public Instruction tableswitch(int defaultTargetPc, int low, int high) {
        return (filter.isPassing(ci) ? new TABLESWITCH(defaultTargetPc, low, high)
                : super.tableswitch(defaultTargetPc, low, high));
    }

    public Instruction d2f() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.D2F();
            } else {
                return new D2F();
            }
        } else {
            return super.d2f();
        }
    }

    public Instruction f2d() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.F2D();
            } else {
                return new F2D();
            }
        } else {
            return super.f2d();
        }
    }

    public Instruction i2b() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.I2B();
            } else {
                return new I2B();
            }
        } else {
            return super.i2b();
        }
    }

    public Instruction i2c() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.I2C();
            } else {
                return new I2C();
            }
        } else {
            return super.i2c();
        }
    }

    public Instruction i2s() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.I2S();
            } else {
                return new I2S();
            }
        } else {
            return super.i2s();
        }
    }

    public Instruction i2l() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.I2L();
            } else {
                return new I2L();
            }
        } else {
            return super.i2l();
        }
    }

    public Instruction l2i() {
        if (filter.isPassing(ci)) {
            if (shadowSymExeMode) {
                return new gov.nasa.jpf.symbc.bytecode.shadow.L2I();
            } else {
                return new L2I();
            }
        } else {
            return super.l2i();
        }
    }

    public Instruction getfield(String fieldName, String clsName, String fieldDescriptor) {
        return (filter.isPassing(ci) ? new GETFIELD(fieldName, clsName, fieldDescriptor)
                : super.getfield(fieldName, clsName, fieldDescriptor));
    }

    public Instruction getstatic(String fieldName, String clsName, String fieldDescriptor) {
        return (filter.isPassing(ci) ? new GETSTATIC(fieldName, clsName, fieldDescriptor)
                : super.getstatic(fieldName, clsName, fieldDescriptor));
    }

    // array ops
    public Instruction arraylength() {
        return (symArrays ? new gov.nasa.jpf.symbc.bytecode.symarrays.ARRAYLENGTH() : super.arraylength());
    }

    public Instruction aaload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.AALOAD() : new AALOAD()
                : super.aaload());
    }

    public Instruction aastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.AASTORE() : new AASTORE()
                : super.aastore());
    }

    public Instruction baload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.BALOAD() : new BALOAD()
                : super.baload());
    }

    public Instruction bastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.BASTORE() : new BASTORE()
                : super.bastore());
    }

    public Instruction caload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.CALOAD() : new CALOAD()
                : super.caload());
    }

    public Instruction castore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.CASTORE() : new CASTORE()
                : super.castore());
    }

    public Instruction daload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.DALOAD() : new DALOAD()
                : super.daload());
    }

    public Instruction dastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.DASTORE() : new DASTORE()
                : super.dastore());
    }

    public Instruction faload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.FALOAD() : new FALOAD()
                : super.faload());
    }

    public Instruction fastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.FASTORE() : new FASTORE()
                : super.fastore());
    }

    public Instruction iaload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.IALOAD() : new IALOAD()
                : super.iaload());
    }

    public Instruction iastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.IASTORE() : new IASTORE()
                : super.iastore());
    }

    public Instruction laload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.LALOAD() : new LALOAD()
                : super.laload());
    }

    public Instruction lastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.LASTORE() : new LASTORE()
                : super.lastore());
    }

    public Instruction saload() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.SALOAD() : new SALOAD()
                : super.saload());
    }

    public Instruction sastore() {
        return (filter.isPassing(ci) ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.SASTORE() : new SASTORE()
                : super.sastore());
    }

    // TODO: to review
    // From Fujitsu:

    public Instruction new_(String clsName) {
        return (filter.isPassing(ci) ? new NEW(clsName) : super.new_(clsName));
    }

    public Instruction ifnonnull(int targetPc) {
        return (filter.isPassing(ci) ? new IFNONNULL(targetPc) : super.ifnonnull(targetPc));
    }

    public Instruction ifnull(int targetPc) {
        return (filter.isPassing(ci) ? new IFNULL(targetPc) : super.ifnull(targetPc));
    }

    public Instruction newarray(int typeCode) {
        return (filter.isPassing(ci)
                ? (symArrays) ? new gov.nasa.jpf.symbc.bytecode.symarrays.NEWARRAY(typeCode) : new NEWARRAY(typeCode)
                : super.newarray(typeCode));
    }

    public Instruction anewarray(String typeDescriptor) {
        return (filter.isPassing(ci) && (symArrays)
                ? new gov.nasa.jpf.symbc.bytecode.symarrays.ANEWARRAY(typeDescriptor)
                : super.anewarray(typeDescriptor));
    }

    public Instruction multianewarray(String clsName, int dimensions) {
        return (filter.isPassing(ci) ? new MULTIANEWARRAY(clsName, dimensions)
                : super.multianewarray(clsName, dimensions));
    }

    /*
     * START: additional bytecode instruction only handled by jpf-shadow different than jpf-core
     */

    @Override
    public Instruction dstore(int localVarIndex) {
        if (shadowSymExeMode) {
            return new gov.nasa.jpf.symbc.bytecode.shadow.DSTORE(localVarIndex);
        } else {
            return super.dstore(localVarIndex);
        }
    }

    @Override
    public Instruction dstore_0() {
        return dstore(0);
    }

    @Override
    public Instruction dstore_1() {
        return dstore(1);
    }

    @Override
    public Instruction dstore_2() {
        return dstore(2);
    }

    @Override
    public Instruction dstore_3() {
        return dstore(3);
    }

    @Override
    public Instruction fstore(int localVarIndex) {
        if (shadowSymExeMode) {
            return new gov.nasa.jpf.symbc.bytecode.shadow.FSTORE(localVarIndex);
        } else {
            return super.fstore(localVarIndex);
        }
    }

    @Override
    public Instruction fstore_0() {
        return fstore(0);
    }

    @Override
    public Instruction fstore_1() {
        return fstore(1);
    }

    @Override
    public Instruction fstore_2() {
        return fstore(2);
    }

    @Override
    public Instruction fstore_3() {
        return fstore(3);
    }

    @Override
    public Instruction istore(int localVarIndex) {
        if (shadowSymExeMode) {
            return new gov.nasa.jpf.symbc.bytecode.shadow.ISTORE(localVarIndex);
        } else {
            return super.istore(localVarIndex);
        }
    }

    @Override
    public Instruction istore_0() {
        return istore(0);
    }

    @Override
    public Instruction istore_1() {
        return istore(1);
    }

    @Override
    public Instruction istore_2() {
        return istore(2);
    }

    @Override
    public Instruction istore_3() {
        return istore(3);
    }

    @Override
    public Instruction lstore(int localVarIndex) {
        if (shadowSymExeMode) {
            return new gov.nasa.jpf.symbc.bytecode.shadow.LSTORE(localVarIndex);
        } else {
            return super.lstore(localVarIndex);
        }
    }

    @Override
    public Instruction lstore_0() {
        return lstore(0);
    }

    @Override
    public Instruction lstore_1() {
        return lstore(1);
    }

    @Override
    public Instruction lstore_2() {
        return lstore(2);
    }

    @Override
    public Instruction lstore_3() {
        return lstore(3);
    }

    /*
     * END: additional bytecode instruction only handled by jpf-shadow different than jpf-core
     */

    static public String[] dp;

    /* Symbolic String configuration */
    static public String[] string_dp;
    static public int stringTimeout;
    static public boolean preprocesOnly;

    /*
     * This is intended to serve as a catchall debug flag. If there's some debug printing/outputing, conditionally print
     * using this flag.
     */
    static public boolean debugMode;

    // new "concolic" mode to collect constraints along concrete paths
    static public boolean collect_constraints = false;

    /*
     * Enable logging of info used to detect regressions
     */
    static public boolean regressMode;

    /*
     * If Green is enabled this solver will be used Later we just check if this is null to know if Green is enabled
     */
    static public Green greenSolver = null;

    /*
     * Allow user to set the bitvector length for Z3bitvector and potentially other bv-based solvers.
     */
    static public int bvlength;

    /*
     * Use floating point theory for reals in Z3 (or other solvers that might support this).
     */
    static public boolean fp;

    /*
     * Concolic mode where we concrete execute for now only Math operations
     */

    /*
     * With this setting, pc choices are only added if multiple branches are feasible
     */
    private final boolean pcChoiceOptimization;

    /*
     * With this setting SPF will execute shadow symbolic execution, i.e. include four-way forking.
     */
    public static boolean shadowSymExeMode;

    /*
     * With this setting, symbolic arrays rely on array theory in Z3
     */
    private final boolean symArrays;

    static public boolean concolicMode;
    static public boolean heuristicRandomMode;
    static public boolean heuristicPartitionMode;
    static public int MaxTries = 1;

    static public int maxPcLength;
    static public long maxPcMSec;
    static public long startSystemMillis;

    ClassInfo ci;
    ClassInfoFilter filter; // TODO: fix; do we still need this?

    private void setupGreen(Config conf) {
        // ------------------------------------
        // Construct the solver
        // ------------------------------------
        greenSolver = new Green();
        new Configuration(greenSolver, conf).configure();
        // fix to make sure when Green is used there is no NPE when poking at dp[0] in
        // some bytecodes
        dp = new String[] { "green" };
    }

    public SymbolicInstructionFactory(Config conf) {
        String[] cc = conf.getStringArray("symbolic.collect_constraints");
        if (cc != null && cc[0].equals("true")) {
            System.out.println("Mixed symbolic/concrete execution ...");
            collect_constraints = true;
        } else {
            collect_constraints = false;
        }

        // Just checking if set, don't care about any values
        String[] dummy = conf.getStringArray("symbolic.debug");
        if (dummy != null && dummy[0].equals("true")) {
            debugMode = true;
        } else {
            debugMode = false;
        }

        this.pcChoiceOptimization = conf.getBoolean("symbolic.optimizechoices", true);
        this.symArrays = conf.getBoolean("symbolic.arrays", false);

        shadowSymExeMode = conf.getBoolean("symbolic.shadow", false);

        if (shadowSymExeMode) {
            System.out.println("Running Symbolic PathFinder in SHADOW mode...");
        } else {
            if (debugMode)
                System.out.println("Running Symbolic PathFinder ...");
        }

        filter = new ClassInfoFilter(null, new String[] { /* "java.*", */ "javax.*" }, null, null);

        if (conf.getBoolean("symbolic.green", false)) {
            System.out.println("Using Green Framework...");
            setupGreen(conf);
        } else {
            dp = conf.getStringArray("symbolic.dp");
            if (dp == null) {
                dp = new String[1];
                dp[0] = "choco";
            }
            if (debugMode)
                System.out.println("symbolic.dp=" + dp[0]);

            stringTimeout = conf.getInt("symbolic.string_dp_timeout_ms");
            if (debugMode)
                System.out.println("symbolic.string_dp_timeout_ms=" + stringTimeout);

            string_dp = conf.getStringArray("symbolic.string_dp");
            if (string_dp == null) {
                string_dp = new String[1];
                string_dp[0] = "none";
            }
            if (debugMode)
                System.out.println("symbolic.string_dp=" + string_dp[0]);

            preprocesOnly = conf.getBoolean("symbolic.string_preprocess_only", false);
            String[] concolic = conf.getStringArray("symbolic.concolic");
            if (concolic != null) {
                concolicMode = true;
                if (debugMode)
                    System.out.println("symbolic.concolic=true");
            } else {
                concolicMode = false;
            }

            String[] concolicMaxTries = conf.getStringArray("symbolic.concolic.MAX_TRIES");
            if (concolicMaxTries != null) {
                MaxTries = Integer.parseInt(concolicMaxTries[0]);
                assert (MaxTries > 0);
                if (debugMode)
                    System.out.println("symbolic.concolic.MAX_TRIES=" + MaxTries);
            } else {
                MaxTries = 1;
            }

            String[] heuristicRandom = conf.getStringArray("symbolic.heuristicRandom");
            if (heuristicRandom != null) {
                heuristicRandomMode = true;
                if (debugMode)
                    System.out.println("symbolic.heuristicRandom=true");
            } else {
                heuristicRandomMode = false;
            }

            String[] heuristicPartition = conf.getStringArray("symbolic.heuristicPartition");
            if (heuristicPartition != null) {
                assert (!heuristicRandomMode);
                heuristicPartitionMode = true;
                if (debugMode)
                    System.out.println("symbolic.heuristicPartition=true");
            } else {
                heuristicPartitionMode = false;
            }

            if (dp[0].equalsIgnoreCase("choco") || dp[0].equalsIgnoreCase("debug") || dp[0].equalsIgnoreCase("compare")
                    || dp == null) { // default is choco
                ProblemChoco.timeBound = conf.getInt("symbolic.choco_time_bound", 30000);
                if (debugMode)
                    System.out.println("symbolic.choco_time_bound=" + ProblemChoco.timeBound);
            }
            // load CORAL's parameters
            if (dp[0].equalsIgnoreCase("coral") || dp[0].equalsIgnoreCase("debug")
                    || dp[0].equalsIgnoreCase("compare")) {
                ProblemCoral.configure(conf);
            }

            maxPcLength = conf.getInt("symbolic.max_pc_length", Integer.MAX_VALUE);
            if (maxPcLength == -1) {
                maxPcLength = Integer.MAX_VALUE;
            }
            if (maxPcLength <= 0) {
                throw new IllegalArgumentException(
                        "symbolic.max_pc_length must be positive (>0), but was " + maxPcLength);
            }
            if (debugMode)
                System.out.println("symbolic.max_pc_length=" + maxPcLength);

            maxPcMSec = conf.getLong("symbolic.max_pc_msec", 0);
            if (maxPcLength < 0) {
                throw new IllegalArgumentException(
                        "symbolic.max_pc_msec must be non-negative (>=0), but was " + maxPcMSec);
            }
            if (debugMode)
                System.out.println("symbolic.max_pc_msec=" + maxPcMSec);
            startSystemMillis = System.currentTimeMillis();
        }

        String regress = conf.getProperty("symbolic.regression_output");
        if (regress != null && regress.equals("true")) {
            regressMode = true;
        } else {
            regressMode = false;
        }

        /* load bitvector length, default to 32 */
        bvlength = conf.getInt("symbolic.bvlength", 32);
        if (debugMode)
            System.out.println("symbolic.bvlength=" + bvlength);

        /* use floating point theory for reals in Z3? */
        fp = conf.getBoolean("symbolic.fp", false);
        if (fp && debugMode)
            System.out.println("Using floating point theory for reals in Z3.");

        MinMax.collectMinMaxInformation(conf);
        /*
         * no longer required here, now read in MinMax, see line above
         * 
         * String[] intmin, intmax, realmin, realmax, dontcare; intmin = conf.getStringArray("symbolic.minint"); intmax
         * = conf.getStringArray("symbolic.maxint"); realmin = conf.getStringArray("symbolic.minreal"); realmax =
         * conf.getStringArray("symbolic.maxreal"); dontcare = conf.getStringArray("symbolic.undefined");
         * 
         * if (intmin != null && intmin[0] != null) MinMax.MININT = new Integer(intmin[0]); if (intmax != null &&
         * intmax[0] != null) MinMax.MAXINT = new Integer(intmax[0]); if (realmin != null && realmin[0] != null)
         * MinMax.MINDOUBLE = new Double(realmin[0]); if (realmax != null && realmax[0] != null) MinMax.MAXDOUBLE = new
         * Double(realmax[0]); if (dontcare != null && dontcare[0] != null) { SymbolicInteger.UNDEFINED = new
         * Integer(dontcare[0]); SymbolicReal.UNDEFINED = new Double(dontcare[0]); }
         * System.out.println("symbolic.minint="+MinMax.MININT); System.out.println("symbolic.maxint="+MinMax.MAXINT);
         * System.out.println("symbolic.minreal="+MinMax.MINDOUBLE);
         * System.out.println("symbolic.maxreal="+MinMax.MAXDOUBLE);
         * System.out.println("symbolic.undefined="+SymbolicInteger.UNDEFINED); if((SymbolicInteger.UNDEFINED >=
         * MinMax.MININT && SymbolicInteger.UNDEFINED <= MinMax.MAXINT) && (SymbolicInteger.UNDEFINED >=
         * MinMax.MINDOUBLE && SymbolicInteger.UNDEFINED <= MinMax.MAXDOUBLE)) System.err.
         * println("Warning: undefined value should be outside  min..max ranges");
         */
    }

}
