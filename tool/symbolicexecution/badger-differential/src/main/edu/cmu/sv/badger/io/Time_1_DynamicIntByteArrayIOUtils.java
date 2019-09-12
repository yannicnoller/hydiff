package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PathCondition;

public class Time_1_DynamicIntByteArrayIOUtils extends IOUtils {

    public static final String ID = "time1-dynamic-int-byte-array";

    public int maxN;

    public Time_1_DynamicIntByteArrayIOUtils(int N) {
        this.maxN = N;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        int n = Math.min(Observations.lastObservedInputSizes[0], maxN);
        byte[] data = new byte[n * (Integer.BYTES + Byte.BYTES)];
        int data_counter = 0;
        for (int i = 0; i < n; i++) {

            Object value_Value = solution.get("sym_value_" + i);
            int intValue;
            if (value_Value == null) {
                intValue = 0;
            } else {
                intValue = Math.toIntExact((long) value_Value);
            }
            ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
            bb.putInt(intValue);
            byte[] byteArray = bb.array();
            data[data_counter++] = byteArray[0];
            data[data_counter++] = byteArray[1];
            data[data_counter++] = byteArray[2];
            data[data_counter++] = byteArray[3];

            Object value_Type = solution.get("sym_type_" + i);
            byte byteValue;
            if (value_Type == null) {
                byteValue = 0;
            } else {
                byteValue = (byte) (Math.toIntExact((long) value_Type) - 1);
            }
            data[data_counter++] = byteValue;

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
