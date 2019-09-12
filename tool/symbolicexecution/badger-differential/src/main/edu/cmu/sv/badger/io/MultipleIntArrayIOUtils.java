package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class MultipleIntArrayIOUtils extends IOUtils {

    public static final String ID = "multi-int-byte-array";

    public int n;
    public int m;

    public MultipleIntArrayIOUtils(int n, int m) {
        this.n = n;
        this.m = m;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        byte[] data = new byte[n * m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Object value = solution.get("sym_" + i + "_" + j);
                int intValue;
                if (value == null) {
                    intValue = 0;
                } else {
                    intValue = Math.toIntExact((long) value);
                }
                data[i*m+j] = (byte) intValue;
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
