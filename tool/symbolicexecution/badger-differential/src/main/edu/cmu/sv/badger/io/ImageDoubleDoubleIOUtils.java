package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class ImageDoubleDoubleIOUtils extends IOUtils {

    public static final String ID = "image-double-double-array";

    public int n;
    public int m;
    public int o;

    public ImageDoubleDoubleIOUtils(int n, int m, int o) {
        this.n = n;
        this.m = m;
        this.o = o;
    }
    
    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        byte[] data = new byte[n * m * o];
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
                    data[i * m + j * o + k] = byteValue;
                }
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
