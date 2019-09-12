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
// Copyright (C) 2006 United States Government as represented by the
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

package gov.nasa.jpf.symbc;

import gov.nasa.jpf.vm.Verify;

public class Debug {

    native public static void printPC(String msg);

    native public static String getSolvedPC();

    native public static String getPC_prefix_notation();

    native public static String getSymbolicIntegerValue(int v);

    native public static String getSymbolicLongValue(long v);

    native public static String getSymbolicShortValue(short v);

    native public static String getSymbolicByteValue(byte v);

    native public static String getSymbolicCharValue(char v);

    native public static String getSymbolicRealValue(double v);

    native public static String getSymbolicBooleanValue(boolean v);

    native public static String getSymbolicStringValue(String v);

    native public static void printSymbolicValue(int v);

    native public static int addSymbolicInt(int v, String name);

    native public static int addConstrainedSymbolicInt(int v, String name, int l, int u);

    native public static long addSymbolicLong(long v, String name);
    // native public static short addSymbolic(short v, String name);
    native public static byte addSymbolicByte(byte v, String name);
    native public static byte addConstrainedSymbolicByte(byte v, String name, int l, int u);

    native public static char addSymbolicChar(char v, String name);

    native public static double addSymbolicDouble(double v, String name);

    // native public static boolean addSymbolic(boolean v, String name);
    // native public static String addSymbolic(String v, String name);
    native public static boolean addSymbolicBoolean(boolean v, String name);

    native public static boolean isSymbolicInteger(int v);

    native public static boolean isSymbolicLong(long v);

    native public static boolean isSymbolicShort(short v);

    native public static boolean isSymbolicByte(byte v);

    native public static boolean isSymbolicChar(char v);

    native public static boolean checkAccuracy(double v, double err);
    // check accuracy of floating point computation
    // wrt given error

    public static void assume(boolean c) {
        if (!c)
            Verify.ignoreIf(true);
    }

    // puts a new symbolic value in the arg attribute
    native public static int makeSymbolicInteger(String name);

    native public static int makeConstrainedSymbolicInteger(String name, int l, int u);

    native public static long makeSymbolicLong(String name);

    native public static short makeSymbolicShort(String name);

    native public static byte makeSymbolicByte(String name);
    native public static byte makeConstrainedSymbolicByte(String name, int l, int u);

    native public static double makeSymbolicDouble(String name);

    native public static boolean makeSymbolicBoolean(String name);

    native public static char makeSymbolicChar(String name);

    native public static String makeSymbolicString(String name);

    // this method should be used instead of the native one in
    // the no-string-models branch of jpf-core
    public static String makeSymbolicString(String name, int size) {
        char str[] = new char[size];
        for (int i = 0; i < size; i++) {
            str[i] = makeSymbolicChar(name + i);
        }
        return new String(str);
    }

    // makes v a symbolic object
    public static Object makeSymbolicRef(String name, Object v) {
        assert (v != null); // needed for type info
        if (Verify.randomBool()) {

            makeFieldsSymbolic(name, v);
        } else {

            v = makeSymbolicNull(name);
        }
        return v;
    }

    native public static void makeFieldsSymbolic(String name, Object v);

    native public static Object makeSymbolicNull(String name);

    native public static void printSymbolicRef(Object v, String msg);

    native public static void printHeapPC(String msg);

    // performs abstract state matching
    native public static boolean matchAbstractState(Object v);

    /* YN: user-defined cost */
    native public static void addCost(Object v);

    native public static void addCostOldVersion(Object v);

    native public static void addCostNewVersion(Object v);

    native public static int get_number_of_last_observed_input_sizes();

    native public static int get_last_observed_input_size(int index);

    public static int[] getLastObservedInputSizes() {
        int[] tmp = new int[get_number_of_last_observed_input_sizes()];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = get_last_observed_input_size(i);
        }
        return tmp;
    }

    native public static void set_last_observed_input_size(int index, int value);

    native public static void reset_last_observed_input_size(int length);

    public static void setLastObservedInputSizes(int[] values) {
        reset_last_observed_input_size(values.length);
        for (int i = 0; i < values.length; i++) {
            set_last_observed_input_size(i, values[i]);
        }
    }

    native public static double getLastMeasuredMetricValue();

    native public static double getLastMeasuredOldMetricValue();

    native public static double getLastMeasuredNewMetricValue();

    native public static void clearMeasurements();

    /* YN: Methods to read the internal values of the DNN on the SPF side. */
    native public static double get_biases0_value(int index);

    native public static double get_biases2_value(int index);

    native public static double get_biases6_value(int index);

    native public static double get_biases8_value(int index);

    native public static double get_weights6_value(int index0, int index1);

    native public static double get_weights8_value(int index0, int index1);

    native public static double get_weights0_value(int index0, int index1, int index2, int index3);

    native public static double get_weights2_value(int index0, int index1, int index2, int index3);

    public static double[] getBiases0() {
        double[] biases0 = new double[2];
        for (int i = 0; i < biases0.length; i++) {
            biases0[i] = get_biases0_value(i);
        }
        return biases0;
    }

    public static double[] getBiases2() {
        double[] biases2 = new double[4];
        for (int i = 0; i < biases2.length; i++) {
            biases2[i] = get_biases2_value(i);
        }
        return biases2;
    }

    public static double[] getBiases6() {
        double[] biases6 = new double[128];
        for (int i = 0; i < biases6.length; i++) {
            biases6[i] = get_biases6_value(i);
        }
        return biases6;
    }

    public static double[] getBiases8() {
        double[] biases8 = new double[10];
        for (int i = 0; i < biases8.length; i++) {
            biases8[i] = get_biases8_value(i);
        }
        return biases8;
    }

    public static double[][] getWeights6() {
        double[][] weights6 = new double[576][128];
        for (int i = 0; i < weights6.length; i++) {
            for (int j = 0; j < weights6[0].length; j++) {
                weights6[i][j] = get_weights6_value(i, j);
            }
        }
        return weights6;
    }

    public static double[][] getWeights8() {
        double[][] weights8 = new double[128][10];
        for (int i = 0; i < weights8.length; i++) {
            for (int j = 0; j < weights8[0].length; j++) {
                weights8[i][j] = get_weights8_value(i, j);
            }
        }
        return weights8;
    }

    public static double[][][][] getWeights0() {
        double[][][][] weights0 = new double[3][3][1][2];
        for (int i = 0; i < weights0.length; i++) {
            for (int j = 0; j < weights0[0].length; j++) {
                for (int k = 0; k < weights0[0][0].length; k++) {
                    for (int l = 0; l < weights0[0][0][0].length; l++) {
                        weights0[i][j][k][l] = get_weights0_value(i, j, k, l);
                    }
                }

            }
        }
        return weights0;
    }

    public static double[][][][] getWeights2() {
        double[][][][] weights2 = new double[3][3][2][4];
        for (int i = 0; i < weights2.length; i++) {
            for (int j = 0; j < weights2[0].length; j++) {
                for (int k = 0; k < weights2[0][0].length; k++) {
                    for (int l = 0; l < weights2[0][0][0].length; l++) {
                        weights2[i][j][k][l] = get_weights2_value(i, j, k, l);
                    }
                }

            }
        }
        return weights2;
    }
    
    native public static String getDataDir();
}
