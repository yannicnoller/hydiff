package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class Math_60_MixedArray extends IOUtils {

    public static final String ID = "math-60-mixed-array";

    public int numberOfVariables;

    public Math_60_MixedArray() {
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {

        byte[] data = new byte[Integer.BYTES * 2 + Double.BYTES * 1];
        String[] names = { "sym_mean", "sym_std", "sym_value" };
        String[] types = { "int", "int", "double" };

        int dataCounter = 0;
        for (int i = 0; i < 3; i++) {
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
    
    public static void main(String[] args) {

        int mean = 1;
        int std = 1;
        double value = 100.0;
        
        byte[] data = new byte[Integer.BYTES * 2 + Double.BYTES * 1];
        int dataCounter = 0;
        
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(mean);
        byte[] byteArray = bb.array();
        // Save byte values in data[].
        data[dataCounter++] = byteArray[0];
        data[dataCounter++] = byteArray[1];
        data[dataCounter++] = byteArray[2];
        data[dataCounter++] = byteArray[3];
        
        bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(std);
        byteArray = bb.array();
        // Save byte values in data[].
        data[dataCounter++] = byteArray[0];
        data[dataCounter++] = byteArray[1];
        data[dataCounter++] = byteArray[2];
        data[dataCounter++] = byteArray[3];
        
        bb = ByteBuffer.allocate(Long.BYTES);
        bb.putLong(Double.doubleToLongBits(value));
        byteArray = bb.array();
        // Save byte values in data[].
        data[dataCounter++] = byteArray[0];
        data[dataCounter++] = byteArray[1];
        data[dataCounter++] = byteArray[2];
        data[dataCounter++] = byteArray[3];
        data[dataCounter++] = byteArray[4];
        data[dataCounter++] = byteArray[5];
        data[dataCounter++] = byteArray[6];
        data[dataCounter++] = byteArray[7];
        
        try {
            Files.write(Paths.get("/Users/yannic/example"), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
