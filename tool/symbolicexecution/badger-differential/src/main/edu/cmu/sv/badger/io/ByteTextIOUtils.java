package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class ByteTextIOUtils extends IOUtils {

    public static final String ID = "byte-text";

    public int ioTextSize;
    public int ioStringSize;

    public ByteTextIOUtils(int ioTextSize, int ioStringSize) {
        this.ioTextSize = ioTextSize;
        this.ioStringSize = ioStringSize;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        byte[] data = new byte[ioTextSize*ioStringSize];
        int data_index = 0;
        for (int i = 0; i < ioTextSize; i++) {
            for (int j = 0; j < ioStringSize; j++) {
                Object value = solution.get("sym_" + i + "_" + j);
                char charValue;
                if (value == null) {
                    charValue = 0;
                } else {
                    charValue = (char) Math.toIntExact((long) value);
                }
                data[data_index] = (byte) charValue;
                data_index++;
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
