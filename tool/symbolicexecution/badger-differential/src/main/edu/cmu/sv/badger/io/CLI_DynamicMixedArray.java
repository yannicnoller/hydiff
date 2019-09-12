package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class CLI_DynamicMixedArray extends IOUtils {

    public static final String ID = "cli-dynamic-mixed-array";

    public int sizeOfOption;
    public int maxNumberOfValues;
    public int sizeOfValues;

    public CLI_DynamicMixedArray(int sizeOfOption, int maxNumberOfValues, int sizeOfValues) {
        this.sizeOfOption = sizeOfOption;
        this.maxNumberOfValues = maxNumberOfValues;
        this.sizeOfValues = sizeOfValues;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        System.out.println(">> " + pc);
        byte[] data = new byte[1 + Character.BYTES * maxNumberOfValues * sizeOfValues + Character.BYTES * sizeOfOption];

        int dataCounter = 0;
        Object value;
        
        value = solution.get("sym_num_values");
        int numberOfValues;
        if (value == null) {
            numberOfValues = 0;
        } else {
            numberOfValues = Math.toIntExact((long) value);
        }
        data[dataCounter++] = (byte) numberOfValues;
        
        for (int i = 0; i < numberOfValues; i++) {
            for (int j = 0; j < sizeOfValues; j++) {
                value = solution.get("sym_val_" + i + "_" + j);
                char charValue;
                if (value == null) {
                    charValue = 0;
                } else {
                    int t = Math.toIntExact((long) value);
                    charValue = (char) t;
                }
                ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
                bb.putChar(charValue);
                byte[] byteArray = bb.array();
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
            }
        }
        
        for (int i = 0; i < sizeOfOption; i++) {
            value = solution.get("sym_opt_" + i);
            char charValue;
            if (value == null) {
                charValue = 0;
            } else {
                int t = Math.toIntExact((long) value);
                charValue = (char) t;
            }
            ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
            bb.putChar(charValue);
            byte[] byteArray = bb.array();
            data[dataCounter++] = byteArray[0];
            data[dataCounter++] = byteArray[1];
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
