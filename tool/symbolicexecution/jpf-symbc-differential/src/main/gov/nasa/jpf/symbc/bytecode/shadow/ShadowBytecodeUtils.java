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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.bytecode.GOTO;
import gov.nasa.jpf.jvm.bytecode.ICONST;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils.InstructionOrSuper;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils.VarType;
import gov.nasa.jpf.symbc.bytecode.INVOKESTATIC;
import gov.nasa.jpf.symbc.bytecode.SymbolicStringHandler;
import gov.nasa.jpf.symbc.heap.Helper;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.DiffExpression;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.MinMax;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.PreCondition;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;
import gov.nasa.jpf.symbc.string.StringExpression;
import gov.nasa.jpf.symbc.string.StringSymbolic;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadInfo.Execute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ShadowBytecodeUtils {
    
    /* Static arrays for double shadow comparison. */
    public static final int[] oldConditionValues = { -1, -1, -1, 0, 0, 0, 1, 1, 1 };
    public static final int[] newConditionValues = { -1, 0, 1, 0, -1, 1, 1, -1, 0 };
    
    public static void addDet(int conditionValue, PathCondition pc, Expression v2, Expression v1) {
        switch (conditionValue) {
        case -1:
            pc._addDet(Comparator.LT, v2, v1);
            break;
        case 0:
            pc._addDet(Comparator.EQ, v2, v1);
            break;
        case 1:
            pc._addDet(Comparator.GT, v2, v1);
        }
    }

    public static int getChoiceForConditionals(int oldConditional, int newConditional) {
        int choice;
        for (choice = 0; choice < oldConditionValues.length; choice++) {
            if (oldConditionValues[choice] == oldConditional && newConditionValues[choice] == newConditional) {
                return choice;
            }
        }
        throw new RuntimeException("DCMPL: unknown conditional pattern!");
    }

    public static int getChoiceForConditional(int conditional) {
        int choice;
        for (choice = 0; choice < oldConditionValues.length; choice++) {
            if (oldConditionValues[choice] == conditional && newConditionValues[choice] == conditional) {
                return choice;
            }
        }
        throw new RuntimeException("DCMPL: unknown conditional pattern!");
    }

    // Map variables to their concrete values
    // TODO: what happens if a different symbolic method is called that uses the
    // same variable names?
    public static Map<String, Object> valueMap = new HashMap<String, Object>();

    // Map variables to symbolic expressions
    public static Map<String, Expression> expressionMap = new HashMap<String, Expression>();

    /**
     * Execute INVOKESPECIAL, INVOKESTATIC, and INVOKEVIRTUAL symbolically.
     * 
     * @param invInst
     *            The instance of INVOKESPECIAL, INVOKESTATIC, or INVOKEVIRTUAL
     * @param ss
     *            The VM's system state
     * @param ks
     *            The VM's kernel state
     * @param th
     *            The current thread info
     * @return an InstructionOrSuper instance saying what to do next.
     */
    public static InstructionOrSuper execute(JVMInvokeInstruction invInst, ThreadInfo th) {
        boolean isStatic = (invInst instanceof INVOKESTATIC);
        String bytecodeName = invInst.getMnemonic().toUpperCase();
        String mname = invInst.getInvokedMethodName();
        String cname = invInst.getInvokedMethodClassName();

        MethodInfo mi = invInst.getInvokedMethod(th);

        if (mi == null) {
            return new InstructionOrSuper(false,
                    th.createAndThrowException("java.lang.NoSuchMethodException", "calling " + cname + "." + mname));
        }

        /*
         * Here we test if the the method should be executed symbolically. We perform two checks: 1. Does the invoked
         * method correspond to a method listed in the symbolic.method property and does the number of parameters match?
         * 2. Is the method contained in a class that is to be executed symbolically? If the method is symbolic,
         * initialize the parameter attributes and the fields if they are specified as symbolic based on annotations
         *
         */

        String longName = mi.getFullName();
        String[] argTypes = mi.getArgumentTypeNames();
        // System.out.println(longName);

        int argSize = argTypes.length; // does not contain "this"

        Vector<String> args = new Vector<String>();
        Config conf = th.getVM().getConfig();

        // Start string handling: TODO corina it needs reviewing as it does not
        // seem to be correct
        /****
         * This is where we branch off to handle symbolic string variables
         *******/
        String[] symstrings = conf.getStringArray("symbolic.strings");
        boolean symstrings_flag = (symstrings != null && symstrings[0].equalsIgnoreCase("true")) ? true : false;
        if (symstrings_flag) {

            SymbolicStringHandler a = new SymbolicStringHandler();
            Instruction handled = a.handleSymbolicStrings(invInst, th);
            if (handled != null) { // go to next instruction as symbolic string
                                   // operation was done
                // System.out.println("Symbolic string analysis!!!"+invInst);
                return new InstructionOrSuper(false, handled);
            }
        }
        // End string handling

        boolean symClass = BytecodeUtils.isClassSymbolic(conf, cname, mi, mname);
        boolean found = (BytecodeUtils.isMethodSymbolic(conf, longName, argSize, args) || symClass);
        if (found) {
            // method is symbolic
            // create a choice generator to associate the precondition with it
            ChoiceGenerator<?> cg = null;
            if (!th.isFirstStepInsn()) { // first time around
                cg = new PCChoiceGenerator(2, 2, 1);
                th.getVM().setNextChoiceGenerator(cg);
                return new InstructionOrSuper(false, invInst);
            } else { // this is what really returns results
                cg = th.getVM().getChoiceGenerator();
                if (!(cg instanceof PCChoiceGenerator)) { // the choice comes from super
                    return new InstructionOrSuper(true, null);
                }
            }

            String outputString = "\n***Execute symbolic " + bytecodeName + ": " + mname + "  (";

            LocalVarInfo[] argsInfo = mi.getArgumentLocalVars();

            // jpf-shadow: map method parameters to concrete values (used for concrete
            // execution)
            for (LocalVarInfo lvi : argsInfo) {
                String varName = lvi.getName();
                Object value = invInst.getArgumentValue(varName, th);
                if (value != null) {
                    valueMap.put(varName, value);
                }

                /*
                 * Check for symbolic attributes, which are passed with the concrete values. E.g. there is a parameter
                 * which was inserted by Debug.addSymbolic..
                 */
                int slotIndex = lvi.getSlotIndex();
                Object attribute = invInst.getArgumentAttrs(th)[slotIndex];
                if (attribute instanceof Expression) {
                    Expression expr = (Expression) attribute;
                    expressionMap.put(varName, expr);
                }
            }

            int localVarsIdx = 0;
            // if debug option was not used when compiling the class,
            // then we do not have names of the locals

            if (argsInfo != null) {
                // Skip over "this" argument when non-static
                localVarsIdx = (isStatic ? 0 : 1);
            } else {
                throw new RuntimeException("ERROR: you need to turn debug option on");
            }

            // take care of the method arguments get a hold of the stack frame of the caller
            StackFrame sf = th.getModifiableTopFrame();

            // number of words; we skip over 'this' for non-static methods
            int numStackSlots = invInst.getArgSize() - (isStatic ? 0 : 1);

            // stackIdx ranges from numStackSlots-1 to 0
            int stackIdx = numStackSlots - 1;

            // special treatment of "this"
            String lazy[] = conf.getStringArray("symbolic.lazy");
            String symarrays[] = conf.getStringArray("symbolic.arrays");
            boolean symarray = false;
            if (symarrays != null) {
                symarray = symarrays[0].equalsIgnoreCase("true");
            }
            // TODO: to review
            // if(lazy != null) {
            // if(lazy[0].equalsIgnoreCase("true")) {
            // if(!isStatic) {
            //// String name = "this";
            //// IntegerExpression sym_v = new SymbolicInteger(varName(name,
            // VarType.REF));
            //// expressionMap.put(name, sym_v);
            //// sf.setOperandAttr(0, sym_v);
            //// outputString = outputString.concat(" " + sym_v + ",");
            // }
            // }
            // }

            for (int j = 0; j < argSize; j++) {
                // j ranges over actual arguments
                if (symClass || args.get(j).equalsIgnoreCase("SYM")) {
                    String name = argsInfo[localVarsIdx].getName();
                    if (argTypes[j].equalsIgnoreCase("int")) {
                        IntegerExpression sym_v = new SymbolicInteger(varName(name, VarType.INT));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("long")) {
                        String varname = varName(name, VarType.INT);
                        IntegerExpression sym_v = new SymbolicInteger(varname, MinMax.getVarMinLong(varname),
                                MinMax.getVarMaxLong(varname));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("short")) {
                        String varname = varName(name, VarType.INT);
                        IntegerExpression sym_v = new SymbolicInteger(varname, MinMax.getVarMinShort(varname),
                                MinMax.getVarMaxShort(varname));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("byte")) {
                        String varname = varName(name, VarType.INT);
                        IntegerExpression sym_v = new SymbolicInteger(varname, MinMax.getVarMinByte(varname),
                                MinMax.getVarMaxByte(varname));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("char")) {
                        String varname = varName(name, VarType.INT);
                        IntegerExpression sym_v = new SymbolicInteger(varname, MinMax.getVarMinChar(varname),
                                MinMax.getVarMaxChar(varname));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("float") || argTypes[j].equalsIgnoreCase("double")) {
                        String varname = varName(name, VarType.REAL);
                        RealExpression sym_v = new SymbolicReal(varname, MinMax.getVarMinDouble(varname),
                                MinMax.getVarMaxDouble(varname));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("boolean")) {
                        IntegerExpression sym_v = new SymbolicInteger(varName(name, VarType.INT), 0, 1);
                        // treat boolean as an integer with range [0,1]
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("java.lang.String")) {
                        StringExpression sym_v = new StringSymbolic(varName(name, VarType.STRING));
                        expressionMap.put(name, sym_v);
                        sf.setOperandAttr(stackIdx, sym_v);
                        outputString = outputString.concat(" " + sym_v + ",");
                    } else if (argTypes[j].equalsIgnoreCase("int[]") || argTypes[j].equalsIgnoreCase("long[]")
                            || argTypes[j].equalsIgnoreCase("byte[]")) {
                        if (symarray) {
                            ArrayExpression sym_v = new ArrayExpression(th.getElementInfo(sf.peek()).toString());
                            expressionMap.put(name, sym_v);
                            sf.setOperandAttr(stackIdx, sym_v);
                            outputString = outputString.concat(" " + sym_v + ",");

                            PCChoiceGenerator prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
                            PathCondition pc;
                            if (prev_cg == null)
                                pc = new PathCondition();
                            else
                                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

                            pc._addDet(Comparator.GE, sym_v.length, new IntegerConstant(0));
                            ((PCChoiceGenerator) cg).setCurrentPC(pc);
                        } else {
                            Object[] argValues = invInst.getArgumentValues(th);
                            ElementInfo eiArray = (ElementInfo) argValues[j];

                            if (eiArray != null)
                                for (int i = 0; i < eiArray.arrayLength(); i++) {
                                    IntegerExpression sym_v = new SymbolicInteger(varName(name + i, VarType.INT));
                                    expressionMap.put(name + i, sym_v);
                                    eiArray.addElementAttr(i, sym_v);
                                    outputString = outputString.concat(" " + sym_v + ",");
                                }
                            else
                                System.out.println("Warning: input array empty! " + name);
                        }
                    } else if (argTypes[j].equalsIgnoreCase("float[]") || argTypes[j].equalsIgnoreCase("double[]")) {
                        if (symarray) {
                            ArrayExpression sym_v = new ArrayExpression(th.getElementInfo(sf.peek()).toString());
                            expressionMap.put(name, sym_v);
                            sf.setOperandAttr(stackIdx, sym_v);
                            outputString = outputString.concat(" " + sym_v + ",");

                            PCChoiceGenerator prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
                            PathCondition pc;
                            if (prev_cg == null)
                                pc = new PathCondition();
                            else
                                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

                            pc._addDet(Comparator.GE, sym_v.length, new IntegerConstant(0));
                            ((PCChoiceGenerator) cg).setCurrentPC(pc);
                        } else {
                            Object[] argValues = invInst.getArgumentValues(th);
                            ElementInfo eiArray = (ElementInfo) argValues[j];

                            if (eiArray != null)
                                for (int i = 0; i < eiArray.arrayLength(); i++) {
                                    RealExpression sym_v = new SymbolicReal(varName(name + i, VarType.REAL));
                                    expressionMap.put(name + i, sym_v);
                                    eiArray.addElementAttr(i, sym_v);
                                    outputString = outputString.concat(" " + sym_v + ",");
                                }
                            else
                                System.out.println("Warning: input array empty! " + name);
                        }
                    } else if (argTypes[j].equalsIgnoreCase("boolean[]")) {
                        if (symarray) {
                            ArrayExpression sym_v = new ArrayExpression(th.getElementInfo(sf.peek()).toString());
                            expressionMap.put(name, sym_v);
                            sf.setOperandAttr(stackIdx, sym_v);
                            outputString = outputString.concat(" " + sym_v + ",");

                            PCChoiceGenerator prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
                            PathCondition pc;
                            if (prev_cg == null)
                                pc = new PathCondition();
                            else
                                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

                            pc._addDet(Comparator.GE, sym_v.length, new IntegerConstant(0));
                            ((PCChoiceGenerator) cg).setCurrentPC(pc);
                        } else {
                            Object[] argValues = invInst.getArgumentValues(th);
                            ElementInfo eiArray = (ElementInfo) argValues[j];

                            if (eiArray != null)
                                for (int i = 0; i < eiArray.arrayLength(); i++) {
                                    IntegerExpression sym_v = new SymbolicInteger(varName(name + i, VarType.INT), 0, 1);
                                    expressionMap.put(name + i, sym_v);
                                    eiArray.addElementAttr(i, sym_v);
                                    outputString = outputString.concat(" " + sym_v + ",");
                                }
                            else
                                System.out.println("Warning: input array empty! " + name);
                        }
                    } else if (argTypes[j].contains("[]")) {
                        if (symarray) {
                            Object[] argValues = invInst.getArgumentValues(th);
                            ElementInfo eiArray = (ElementInfo) argValues[j];
                            // If the type name contains [] but wasn't catched
                            // previously, it is an object array
                            ArrayExpression sym_v = new ArrayExpression(th.getElementInfo(sf.peek()).toString(),
                                    argTypes[j].substring(0, argTypes[j].length() - 2));
                            // We remove the [] at the end of the type to keep
                            // only the type of the object
                            expressionMap.put(name, sym_v);
                            sf.setOperandAttr(stackIdx, sym_v);
                            outputString = outputString.concat(" " + sym_v + ",");

                            PCChoiceGenerator prev_cg = cg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
                            PathCondition pc;
                            if (prev_cg == null)
                                pc = new PathCondition();
                            else
                                pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

                            pc._addDet(Comparator.GE, sym_v.length, new IntegerConstant(0));
                            ((PCChoiceGenerator) cg).setCurrentPC(pc);
                        }
                    } else {
                        // the argument is of reference type and it is symbolic
                        if (lazy != null) {
                            if (lazy[0].equalsIgnoreCase("true")) {
                                IntegerExpression sym_v = new SymbolicInteger(varName(name, VarType.REF));
                                expressionMap.put(name, sym_v);
                                sf.setOperandAttr(stackIdx, sym_v);
                                outputString = outputString.concat(" " + sym_v + ",");
                            }
                        }
                        // throw new RuntimeException("## Error: parameter type
                        // not yet handled: " + argTypes[j]);
                    }

                } else
                    outputString = outputString.concat(" " + argsInfo[localVarsIdx].getName() + "_CONCRETE" + ",");

                if (argTypes[j].equalsIgnoreCase("long") || argTypes[j].equalsIgnoreCase("double")) {
                    stackIdx--;
                }
                stackIdx--;
                localVarsIdx++;
            }

            if (outputString.endsWith(","))
                outputString = outputString.substring(0, outputString.length() - 1);
            outputString = outputString + " )  (";

            // now, take care of any globals that are indicated as symbolic
            // base on annotation or on symbolic.fields property
            // annotation will override the symbolic.fields property as a
            // way to specify exceptions
            String[] symFields = conf.getStringArray("symbolic.fields");
            boolean symStatic = false;
            boolean symInstance = false;
            if (symFields != null) {
                List<String> symList = Arrays.asList(symFields);
                for (int i = 0; i < symList.size(); i++) {
                    String s = (String) symList.get(i);
                    if (s.equalsIgnoreCase("instance"))
                        symInstance = true;
                    else if (s.equalsIgnoreCase("static"))
                        symStatic = true;
                }
            }
            int index = 1;
            ClassInfo ci = mi.getClassInfo();
            FieldInfo[] fields = ci.getDeclaredInstanceFields();
            ElementInfo ei;
            if (isStatic) {
                ei = th.getElementInfo(ci.getClassObjectRef());
            } else {
                int objRef = th.getCalleeThis(invInst.getArgSize());
                if (objRef == MJIEnv.NULL) { // NPE
                    return new InstructionOrSuper(false, th.createAndThrowException("java.lang.NullPointerException",
                            "calling '" + mname + "' on null object"));
                }
                ei = th.getElementInfo(objRef);
            }

            if (fields.length > 0) {
                for (int i = 0; i < fields.length; i++) {
                    String value = "";
                    int objRef = th.getCalleeThis(invInst.getArgSize());
                    if (fields[i].getAnnotation("gov.nasa.jpf.symbc.Symbolic") != null)
                        value = fields[i].getAnnotation("gov.nasa.jpf.symbc.Symbolic").valueAsString();

                    else {
                        if (true == symInstance)
                            value = "true";
                        else
                            value = "false";
                    }
                    if (value.equalsIgnoreCase("true")) {
                        Expression sym_v = Helper.initializeInstanceField(fields[i], ei, "input[" + objRef + "]", "");
                        String name = fields[i].getName();
                        expressionMap.put(name, sym_v);
                        outputString = outputString.concat(" " + name + ",");
                        // outputString = outputString.concat(" " + fullName +
                        // ",");
                        index++;
                    }
                }
            }

            FieldInfo[] staticFields = ci.getDeclaredStaticFields();
            if (staticFields.length > 0) {
                for (int i = 0; i < staticFields.length; i++) {
                    String value = "";
                    if (staticFields[i].getAnnotation("gov.nasa.jpf.symbc.Symbolic") != null)
                        value = staticFields[i].getAnnotation("gov.nasa.jpf.symbc.Symbolic").valueAsString();
                    else {
                        if (true == symStatic)
                            value = "true";
                        else
                            value = "false";
                    }
                    if (value.equalsIgnoreCase("true")) {
                        Expression sym_v = Helper.initializeStaticField(staticFields[i], ci, th, "");
                        String name = staticFields[i].getName();
                        expressionMap.put(name, sym_v);
                        outputString = outputString.concat(" " + name + ",");
                        // outputString = outputString.concat(" " + fullName +
                        // ",");
                        index++;
                    }
                }
            }

            if (outputString.endsWith(",")) {
                outputString = outputString.substring(0, outputString.length() - 1);
                outputString = outputString + " )";
            } else {
                if (outputString.endsWith("("))
                    outputString = outputString.substring(0, outputString.length() - 1);
            }
            // System.out.println(outputString);

            // Now, set up the initial path condition for this method if the
            // Annotation contains one
            // we'll create a choice generator for this

            // this is pretty inefficient especially when preconditions are not
            // used -- fixed somehow -- TODO: testing

            if (invInst.getInvokedMethod().getAnnotation("gov.nasa.jpf.symbc.Preconditions") != null) {
                AnnotationInfo ai;
                PathCondition pc = null;
                // TODO: should still look at prev pc if we want to generate
                // test sequences
                // here we should get the prev pc
                assert (cg instanceof PCChoiceGenerator) : "expected PCChoiceGenerator, got: " + cg;
                ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
                while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
                    prev_cg = prev_cg.getPreviousChoiceGenerator();
                }

                if (prev_cg == null)
                    pc = new PathCondition();
                else
                    pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

                assert pc != null;

                ai = invInst.getInvokedMethod().getAnnotation("gov.nasa.jpf.symbc.Preconditions");
                String assumeString = (String) ai.getValue("value");

                pc = (new PreCondition()).addConstraints(pc, assumeString, expressionMap);

                // should check PC for satisfiability
                if (!pc.simplify()) {// not satisfiable
                    // System.out.println("Precondition not satisfiable");
                    th.getVM().getSystemState().setIgnored(true);
                } else {
                    // pc.solve();
                    ((PCChoiceGenerator) cg).setCurrentPC(pc);
                    // System.out.println(((PCChoiceGenerator)
                    // cg).getCurrentPC());
                }
            }
        }
        return new InstructionOrSuper(true, null);
    }

    /**
     * Get the path condition of a SystemState's most recent PCChoiceGenerator.
     */
    public static PathCondition getPC(SystemState ss) {
        ChoiceGenerator<?> cg = ss.getChoiceGenerator();
        while (cg != null && !(cg instanceof PCChoiceGenerator)) {
            cg = cg.getPreviousChoiceGenerator();
        }

        if (cg == null) {
            return null;
        } else {
            return ((PCChoiceGenerator) cg).getCurrentPC();
        }
    }

    public static String varName(String name, VarType type) {
        String suffix = "";
        switch (type) {
        case INT:
            suffix = "_SYMINT";
            break;
        case REAL:
            suffix = "_SYMREAL";
            break;
        case REF:
            suffix = "_SYMREF";
            break;
        case STRING:
            suffix = "_SYMSTRING";
            break;
        default:
            throw new RuntimeException("Unhandled SymVarType: " + type);
        }
        return name /* + "_" + (symVarCounter++) + suffix */;
    }

    // jpf-shadow: helper methods to determine symbolic/shadow expressions of
    // variables
    public static IntegerExpression getOldSymbolicExpr(Object expr, int concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (IntegerExpression) diffExpr.getOldSymbolicExpr();
            } else {
                return (IntegerExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getOldConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static IntegerExpression getOldConcreteExpr(Object expr, int concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (IntegerExpression) diffExpr.getOldConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new IntegerConstant(concreteValueFromStack);
        }
    }

    public static IntegerExpression getNewSymbolicExpr(Object expr, int concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (IntegerExpression) diffExpr.getNewSymbolicExpr();
            } else {
                return (IntegerExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getNewConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static IntegerExpression getNewConcreteExpr(Object expr, int concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (IntegerExpression) diffExpr.getNewConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new IntegerConstant(concreteValueFromStack);
        }
    }

    public static IntegerExpression getOldSymbolicExpr(Object expr, long concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (IntegerExpression) diffExpr.getOldSymbolicExpr();
            } else {
                return (IntegerExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getOldConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static IntegerExpression getOldConcreteExpr(Object expr, long concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (IntegerExpression) diffExpr.getOldConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new IntegerConstant(concreteValueFromStack);
        }
    }

    public static IntegerExpression getNewSymbolicExpr(Object expr, long concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (IntegerExpression) diffExpr.getNewSymbolicExpr();
            } else {
                return (IntegerExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getNewConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static IntegerExpression getNewConcreteExpr(Object expr, long concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (IntegerExpression) diffExpr.getNewConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new IntegerConstant(concreteValueFromStack);
        }
    }

    //

    public static RealExpression getOldSymbolicExpr(Object expr, double concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (RealExpression) diffExpr.getOldSymbolicExpr();
            } else {
                return (RealExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getOldConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static RealExpression getOldConcreteExpr(Object expr, double concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (RealExpression) diffExpr.getOldConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new RealConstant(concreteValueFromStack);
        }
    }

    public static RealExpression getNewSymbolicExpr(Object expr, double concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (RealExpression) diffExpr.getNewSymbolicExpr();
            } else {
                return (RealExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getNewConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static RealExpression getNewConcreteExpr(Object expr, double concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (RealExpression) diffExpr.getNewConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new RealConstant(concreteValueFromStack);
        }
    }

    //

    public static RealExpression getOldSymbolicExpr(Object expr, float concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (RealExpression) diffExpr.getOldSymbolicExpr();
            } else {
                return (RealExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getOldConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static RealExpression getOldConcreteExpr(Object expr, float concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (RealExpression) diffExpr.getOldConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new RealConstant(concreteValueFromStack);
        }
    }

    public static RealExpression getNewSymbolicExpr(Object expr, float concreteValueFromStack) {
        if (expr != null) {
            if (expr instanceof DiffExpression) {
                DiffExpression diffExpr = (DiffExpression) expr;
                return (RealExpression) diffExpr.getNewSymbolicExpr();
            } else {
                return (RealExpression) expr;
            }
        } else {
            /*
             * If there is no expression, then there is no symbolic value to return. In order to make it easy for later,
             * simply return the concrete value.
             */
            return getNewConcreteExpr(expr, concreteValueFromStack);
        }
    }

    public static RealExpression getNewConcreteExpr(Object expr, float concreteValueFromStack) {
        if (expr != null && expr instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) expr;
            return (RealExpression) diffExpr.getNewConcreteExpr();
        } else {
            /*
             * If expression is no DiffExpression, then it is a pure symbolic expression, i.e. there is no concrete
             * value to take from. Then take the concrete value from the stack, which is correct for the old and the new
             * execution because there was no divergence.
             */
            return new RealConstant(concreteValueFromStack);
        }
    }

    /**
     * Checks the symbolic variables in the operands for diff expressions. As soon as the old and new expression are
     * present and not eqaul, then it will return true. This does not necessarily mean that there is a true diff path,
     * but it least there is a difference in the conditions and it might be possible. This method is used to determine
     * conditions that can clearly not lead to a diff, e.g. if both (the old and the new operands) are the same.
     */
    public static boolean checkOperandsForPotentialDiff(Object op_v1, Object op_v2) {
        if (op_v1 != null && op_v1 instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) op_v1;
            if (!diffExpr.getNewSymbolicExpr().equals(diffExpr.getOldSymbolicExpr())) {
                return true;
            }
        }
        if (op_v2 != null && op_v2 instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) op_v2;
            if (!diffExpr.getNewSymbolicExpr().equals(diffExpr.getOldSymbolicExpr())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * see checkOperandsForPotentialDiff(Object op_v1, Object op_v2). 
     */
    public static boolean checkOperandsForPotentialDiff(Object op_v) {
        if (op_v != null && op_v instanceof DiffExpression) {
            DiffExpression diffExpr = (DiffExpression) op_v;
            if (!diffExpr.getNewSymbolicExpr().equals(diffExpr.getOldSymbolicExpr())) {
                return true;
            }
        }
        return false;
    }

    /*
     * TODO YN: implement public static IntegerExpression getSymbcExpr(Object expr, long concreteValue); public static
     * IntegerExpression getShadowExpr(Object expr, long concreteValue); public static RealExpression
     * getSymbcExpr(Object expr, float concreteValue); public static RealExpression getShadowExpr(Object expr, float
     * concreteValue); public static RealExpression getSymbcExpr(Object expr, double concreteValue); public static
     * RealExpression getShadowExpr(Object expr, double concreteValue);
     */

    // jpf-shadow: add constraint variable = concreteValue to pc in order to force
    // concrete execution
    public static void addConcreteValues(PathCondition pc) {
        // for each input variable, add constraint variable=value
        for (String var : valueMap.keySet()) {
            Object value = valueMap.get(var);
            Expression sym_v = expressionMap.get(var);

            if (var.equals("this")) {
                continue;
            }

            if (sym_v == null) {
                // No symbolic value for variable, i.e. variable is concrete.
                continue;
            }

            // handle integer parameters, TODO: double,float, long
            if (value instanceof Integer) {
                int v = ((Integer) value).intValue();
                // System.out.println("Added concrete condition:
                // "+sym_v.toString()+"="+v);
                pc._addDet(Comparator.EQ, (IntegerExpression) sym_v, v);
            } else if (value instanceof Boolean) {
                int v = ((Boolean) value).booleanValue() == true ? 1 : 0;
                pc._addDet(Comparator.EQ, (IntegerExpression) sym_v, v);
            } else if (value instanceof Double) {
                double v = ((Double) value).doubleValue();
                pc._addDet(Comparator.EQ, (RealExpression) sym_v, v);
            }
        }
        return;
    }

    // jpf-shadow: returns hashmap mapping symbolic expressions to their concrete
    // values
    public enum ValueType {
        INT, LONG, DOUBLE, FLOAT
    };

    public static HashMap<Expression, Object> getConcreteValueMapping(PathCondition pc, ValueType type) {
        HashMap<Expression, Object> concreteValueMap = new HashMap<Expression, Object>();
        switch (type) {
        case DOUBLE:
            for (String var : valueMap.keySet()) {
                if (var.equals("this")) {
                    continue;
                }
                Object value = valueMap.get(var);
                Expression sym_v = expressionMap.get(var);

                double doubleValue = 0;
                if (value instanceof Integer) {
                    doubleValue = ((Integer) value).doubleValue();
                    concreteValueMap.put(sym_v, new Double(doubleValue));
                } else if (value instanceof Long) {
                    doubleValue = ((Long) value).doubleValue();
                    concreteValueMap.put(sym_v, new Double(doubleValue));
                } else if (value instanceof Float) {
                    doubleValue = ((Float) value).doubleValue();
                    concreteValueMap.put(sym_v, new Double(doubleValue));
                } else if (value instanceof Double) {
                    concreteValueMap.put(sym_v, value);
                } else {
                    throw new RuntimeException("invalid type");
                }
            }
            return concreteValueMap;
        default:
            throw new RuntimeException("not implemented");
        }
    }

    // jpf-shadow: this instruction resets the execution mode to BOTH (necessary to
    // handle change(boolean,boolean) stmts)
    // public static Instruction resetInstruction;
    public static HashSet<Instruction> resetInstructions = new HashSet<Instruction>();

    /*
     * jpf-shadow: Determines whether an if-insn is executed in order to compute one of the parameters of a
     * change(boolean,boolean) invocation. This assumes that the whole if(change(boolean,boolean)) statement is on the
     * same line TODO: Make this look less like a hack
     */
    public static boolean isChangeBoolean(Instruction insn, ThreadInfo ti) {
        Execute executionMode = getIfInsnExecutionMode(insn, ti);
        if (executionMode != Execute.BOTH) {
            return true;
        }
        return false;
    }

    public static Execute getIfInsnExecutionMode(Instruction insn, ThreadInfo ti) {
        /*
         * If-insns prior to a change(boolean,boolean) invocation register choice generators with the execution mode
         * BOTH. If-insns inside the first and second parameter register choice generators with the execution mode OLD
         * and NEW, respectively.
         * 
         * The bytecode is generated in such a way that there are only two instructions (iconst_1 and iconst_0) for each
         * boolean expression argument (no matter how complex) that push the result of the evaluated boolean expression
         * on the stack (is this compiler dependent?).
         * 
         * Usually, the bytecode sequence looks like this: i+0 IF... //The last if instruction evaluating the old
         * boolean expression// i+1 iconst_1 i+2 goto i+4 i+3 iconst_0 Which means that after this sequence the second
         * boolean expression is being evaluated: ... j+0 IF... //The last if instruction evaluating the new boolean
         * expression// j+1 iconst_1 j+2 goto j+4 j+3 iconst_0 j+4 INVOKEVIRTUAL //Invocation of the
         * change(boolean,boolean) method
         * 
         * Note that this is only the case if there is actually any if-insn involved in the evaluation of the first
         * boolean expression (i.e. the old boolean expression is not a constant like true or false). Otherwise, the
         * result will be directly pushed on the stack.
         */

        PCChoiceGenerator curPcCg;
        ChoiceGenerator<?> curCg = ti.getVM().getSystemState().getChoiceGenerator();
        if (curCg instanceof PCChoiceGenerator) {
            curPcCg = (PCChoiceGenerator) curCg;
        } else {
            curPcCg = curCg.getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
        }

        Execute cgExecutionMode = curPcCg.getExecutionMode();
        Execute currentExecutionMode = ti.getExecutionMode();

        // This should only be executed for the first if-insn the OLD/NEW expression of
        // a change(boolean,boolean) stmt
        // The second condition will evaluate to true after we have executed all
        // if-insns of the old expression and
        // the listener reset the ti execution mode to BOTH; from that point on we
        // evaluate the new expression
        if (cgExecutionMode == Execute.BOTH
                || (cgExecutionMode == Execute.OLD && currentExecutionMode == Execute.BOTH)) {
            String sourceline;
            try {
                sourceline = insn.getSourceLine().replaceAll("\\s++", ""); // simple normalization
            } catch (NullPointerException e) {
                // No available source (e.g. library function), hence no change() annotation
                // possible
                ti.setExecutionMode(Execute.BOTH);
                return Execute.BOTH;
            }
            int ifInsnIndex = -1; // insn-index of the querying if-insn
            int oldResultIndex = -1; // insn-index of the bytecode pattern that pushes the OLD result on the stack
            int newResultIndex = -1; // insn-index of the bytecoe pattern that pushes the NEW result on the stack

            // Handle final IFEQ insn after the invokevirtual change(ZZ)Z insn
            if (insn instanceof IFEQ || insn instanceof IFNE) {
                if (insn.getPrev() instanceof JVMInvokeInstruction
                        && ((JVMInvokeInstruction) insn.getPrev()).getInvokedMethodName().endsWith("change(ZZ)Z")) {
                    ti.setExecutionMode(Execute.BOTH);
                    return Execute.BOTH;
                }
            }

            if (sourceline.contains("change(")) {
                Instruction currentInsn = insn;
                // Determine the number of lines of the change(boolean,boolean) stmt
                boolean foundChangeInvocation = false;
                while (!foundChangeInvocation) {
                    assert (currentInsn != null) : "Error searching for INVOKEVIRTUAL insn for change(boolean,boolean) "
                            + "stmt on line " + insn.getLineNumber();
                    if (currentInsn instanceof JVMInvokeInstruction) {
                        String invokedMethod = ((JVMInvokeInstruction) currentInsn).getInvokedMethodName();
                        if (invokedMethod.endsWith("change(ZZ)Z")) { // found change(boolean,boolean)
                            foundChangeInvocation = true;
                        }
                    }
                    currentInsn = currentInsn.getNext();
                }
            } else if (cgExecutionMode == Execute.BOTH) {
                assert (ti.getExecutionMode() == Execute.BOTH);
                return Execute.BOTH;
            }

            // Now search for bytecode pattern iconst_1/0 goto iconst_1/0 that push results
            // of the old/new expression
            Instruction first, second, third, fourth;
            second = insn;
            third = second.getNext();
            fourth = third.getNext();

            // If insns that push the result of the evaluation of the boolean expression on
            // the stack
            // These instructions will reset the execution mode to BOTH
            Instruction resetInsn1 = null, resetInsn2 = null;

            boolean foundPattern = false;
            for (int ind = 0; !foundPattern; ind++) {
                first = second;
                second = third;
                third = fourth;
                fourth = fourth.getNext();

                if (first.equals(insn)) {
                    ifInsnIndex = ind;
                }
                if (first instanceof ICONST && second instanceof GOTO && third instanceof ICONST) {
                    foundPattern = true;

                    // Determine whether the pattern corresponds to the evaluation of the old or new
                    // expression
                    if (fourth instanceof JVMInvokeInstruction) { // new expression
                        newResultIndex = ind;
                    } else {
                        oldResultIndex = ind;
                    }
                    resetInsn1 = first;
                    resetInsn2 = third;
                }
            }

            assert (ifInsnIndex != -1) : "Unable to find if-insn " + insn.getMnemonic()
                    + " inside change(boolean, boolean) stmt on line " + insn.getLineNumber();
            assert (!(oldResultIndex == -1
                    && newResultIndex == -1)) : "Unable to determine where the old/new parameters of the "
                            + "change(boolean,boolean) stmt are evaluated on line " + insn.getLineNumber();

            if (ifInsnIndex == -1) {
                System.out.println(
                        "Unable to find if insn " + insn.getMnemonic() + " searching on line " + insn.getLineNumber());
            }

            if (ifInsnIndex <= oldResultIndex) {
                ti.setExecutionMode(Execute.OLD);
                resetInstructions.add(resetInsn1);
                resetInstructions.add(resetInsn2);
                if (SymbolicInstructionFactory.debugMode)
                    System.out.println("change(b,b) in line " + insn.getLineNumber() + ": instruction "
                            + insn.getMnemonic() + " belongs to OLD expression");
                return Execute.OLD;
            } else {
                ti.setExecutionMode(Execute.NEW);
                resetInstructions.add(resetInsn1);
                resetInstructions.add(resetInsn2);
                if (SymbolicInstructionFactory.debugMode)
                    System.out.println("change(b,b) in line " + insn.getLineNumber() + ": instruction "
                            + insn.getMnemonic() + " belongs to NEW expression");
                return Execute.NEW;
            }

        } else {
            // Last registered CG was either OLD or NEW, note that after executing a
            // change(boolean,boolean) the last registered cg is BOTH
            switch (currentExecutionMode) {
            case OLD: // We're still evaluating the old boolean expression
                ti.setExecutionMode(Execute.OLD);
                return Execute.OLD;
            case NEW: // We're still evaluating the new boolean expression
                ti.setExecutionMode(Execute.NEW);
                return Execute.NEW;
            default: // BOTH
                assert (false) : (insn.getLineNumber() + " " + insn.getMnemonic() + " cg: " + cgExecutionMode + " ti: "
                        + currentExecutionMode + " diff: " + curPcCg.getCurrentPC().isDiffPC() + " pc: "
                        + curPcCg.getCurrentPC().toString());
                return Execute.BOTH;
            }
        }
    }
}
