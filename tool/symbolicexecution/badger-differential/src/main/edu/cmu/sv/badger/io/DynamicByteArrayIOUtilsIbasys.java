package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class DynamicByteArrayIOUtilsIbasys extends IOUtils {

    public static final String ID = "ibasys-dynamic-byte-array";

    public int pCodeLength;

    public DynamicByteArrayIOUtilsIbasys(int pCodeLength) {
        this.pCodeLength = pCodeLength;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {

        byte[] data = new byte[pCodeLength * 2];
        int data_counter = 0;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < pCodeLength; j++) {
                Object value = solution.get("sym_" + i + "_" + j);
                int intValue;
                if (value == null) {
                    intValue = 0;
                } else {
                    intValue = Math.toIntExact((long) value);
                }
                data[data_counter++] = (byte) intValue;
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
