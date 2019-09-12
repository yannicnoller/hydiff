package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class TCASMixedArray extends IOUtils {

    public static final String ID = "tcas-mixed-array";
    
    public int N;

    public TCASMixedArray(int N) {
        this.N = N;
        if (N != 12) {
            throw new RuntimeException("invalid numer of variables for TCAS! It should be 12, but it is: " + N);
        }
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    /*
     * int cvs;
     * boolean hc;
     * boolean ttrv;
     * int ota;
     * int otar;
     * int otTa;
     * int alv;
     * int upS;
     * int dS;
     * int oRAC;
     * int oc;
     * boolean ci;
     */
    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        byte[] data = new byte[9 * Integer.BYTES + 3 * Byte.BYTES];
        String[] names = {"sym_cvs", "sym_hc", "sym_ttrv", "sym_ota", "sym_otar", "sym_otTa", "sym_alv", "sym_upS", "sym_dS", "sym_oRAC", "sym_oc", "sym_ci"};
        String[] types = {"int", "boolean", "boolean", "int", "int", "int", "int", "int", "int", "int", "int", "boolean"};
        
        int dataCounter = 0;
        for (int i = 0; i < N; i++) {
            
            Object value = solution.get(names[i]);
            
            //  Get int value from model.
            int intValue;
            if (value == null) {
                intValue = 0;
            } else {
                intValue = Math.toIntExact((long) value);
            }
            
            if (types[i].equals("int")) {
                // Transform int in byte[].
                ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                bb.putInt(intValue);
                byte[] byteArray = bb.array();
                // Save byte values in data[].
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
                data[dataCounter++] = byteArray[2];
                data[dataCounter++] = byteArray[3];
            } else if (types[i].equals("boolean")) {
                data[dataCounter++] = (byte) intValue;
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
