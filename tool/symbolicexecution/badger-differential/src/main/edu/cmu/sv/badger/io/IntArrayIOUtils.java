package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/*! problems with negative integers*/
public class IntArrayIOUtils extends IOUtils {

    public static final String ID = "int-byte-array";
    
    public int N;
    
    public IntArrayIOUtils(int N) {
        this.N = N;
    }


    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        byte[] data = new byte[N];
        for (int i = 0; i < data.length; i++) {
            Object value = solution.get("sym_" + i);
            int intValue;
            if (value == null) {
                intValue = 0;
            } else {
                intValue = Math.toIntExact((long) value);
            }
            data[i] = (byte) intValue;
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
