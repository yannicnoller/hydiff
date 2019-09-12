package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class Math_atan2_MixedArray extends IOUtils {

    public static final String ID = "math-atan2-mixed-array";

    public int numberOfVariables;

    public Math_atan2_MixedArray(int N) {
        this.numberOfVariables = N;
        if (numberOfVariables != 8) {
            throw new RuntimeException(
                    "invalid numer of variables for MerArbiter! It should be 8, but it is: " + numberOfVariables);
        }
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {

        byte[] data = new byte[Integer.BYTES * 6 + Double.BYTES * 2];
        String[] names = { "sym_x_variables", "sym_x_order", "sym_x_index", "sym_x_value", "sym_y_variables",
                "sym_y_order", "sym_y_index", "sym_y_value" };
        String[] types = { "int", "int", "int", "double", "int", "int", "int", "double" };

        int dataCounter = 0;
        for (int i = 0; i < numberOfVariables; i++) {
            Object value = solution.get(names[i]);
            if (types[i].equals("int")) {
                // Get int value from model.
                int intValue;
                if (value == null) {
                    intValue = 0;
                } else {
                    intValue = Math.toIntExact((long) value);
                }
                // Transform int in byte[].
                ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                bb.putInt(intValue);
                byte[] byteArray = bb.array();
                // Save byte values in data[].
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
                data[dataCounter++] = byteArray[2];
                data[dataCounter++] = byteArray[3];
            } else if (types[i].equals("double")) {
                // Get double value from model.
                double doubleValue;
                if (value == null) {
                    doubleValue = 0.0;
                } else {
                    doubleValue = (double) value;
                }
                ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
                bb.putLong(Double.doubleToLongBits(doubleValue));
                byte[] byteArray = bb.array();
                // Save byte values in data[].
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
                data[dataCounter++] = byteArray[2];
                data[dataCounter++] = byteArray[3];
                data[dataCounter++] = byteArray[4];
                data[dataCounter++] = byteArray[5];
                data[dataCounter++] = byteArray[6];
                data[dataCounter++] = byteArray[7];
            } else {
                throw new RuntimeException("implementation missing for type: " + types[i]);
            }

        }
        try {
            Files.write(Paths.get(outputFile), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
