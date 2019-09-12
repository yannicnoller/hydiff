package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class DiffImageDoubleDoubleIOUtils extends IOUtils {

    public static final String ID = "diff-image-double-double-array";

    public int n;
    public int m;
    public int o;
    public int p;

    public DiffImageDoubleDoubleIOUtils(int numberOfXPixels, int numberofYPixels, int numberOfZPixels, int numberOfChangePixels) {
        this.n = numberOfXPixels;
        this.m = numberofYPixels;
        this.o = numberOfZPixels;
        this.p = numberOfChangePixels;
    }
    
    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        if (pc.toString().contains("change")) {
            System.out.println("here");
        }
        byte[] data = new byte[n * m * o + 3 * p];
        int dataCounter = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < o; k++) {
                    Object value = solution.get("sym_" + i + "_" + j + "_" + k);
                    
                    /* TODO YN: FIXME might lead to not feasible results */
                    double doubleValue;
                    if (value != null) {
                        doubleValue = (double) value;
                    } else {
                        doubleValue = 0.0;
                    }
                    
                    byte byteValue = (byte) (doubleValue * 255 - 128);
                    data[dataCounter++] = byteValue;
                }
            }
        }
        
        for (int i=0; i<p; i++) {
            Object value_ipos = solution.get("sym_ipos_" + i);
            int value_iposValue;
            if (value_ipos == null) {
                value_iposValue = 0;
            } else {
                value_iposValue = Math.toIntExact((long) value_ipos);
            }
            data[dataCounter++] = (byte) value_iposValue;
            
            Object value_jpos = solution.get("sym_jpos_" + i);
            int value_jposValue;
            if (value_jpos == null) {
                value_jposValue = 0;
            } else {
                value_jposValue = Math.toIntExact((long) value_jpos);
            }
            data[dataCounter++] = (byte) value_jposValue;
            
            Object value_change = solution.get("sym_change_" + i);
            /* TODO YN: FIXME might lead to not feasible results */
            double doubleValue;
            if (value_change != null) {
                doubleValue = (double) value_change;
            } else {
                doubleValue = 0.0;
            }
            byte byteValue = (byte) (doubleValue * 255 - 128);
            data[dataCounter++] = byteValue;
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
