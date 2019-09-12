package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PathCondition;

public class MerArbiterMixedArray extends IOUtils {

    public static final String ID = "merarbiter-dynamic-mixed-array";

    public int maxN;
    public int numberOfVariables;

    public MerArbiterMixedArray(int maxN, int numberOfVariables) {
        this.maxN = maxN;
        this.numberOfVariables = numberOfVariables;
        if (numberOfVariables != 3) {
            throw new RuntimeException(
                    "invalid numer of variables for MerArbiter! It should be 3, but it is: " + numberOfVariables);
        }
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {

        int input_length = Math.min(Observations.lastObservedInputSizes[0], maxN);

        List<Byte> dataList = new ArrayList<>();
        ByteBuffer bb;
        byte[] byteArray;
        for (int i = 0; i < input_length; i++) {

            Object flag_value = solution.get("sym_flag_" + i);
            int flag_intValue;
            if (flag_value == null) {
                flag_intValue = 0;
            } else {
                flag_intValue = Math.toIntExact((long) flag_value);
            }
            bb = ByteBuffer.allocate(Integer.BYTES);
            bb.putInt(flag_intValue);
            byteArray = bb.array();
            dataList.add(byteArray[0]);
            dataList.add(byteArray[1]);
            dataList.add(byteArray[2]);
            dataList.add(byteArray[3]);

            if (flag_intValue != 2) {

                Object resource_value = solution.get("sym_resource_" + i);
                int resource_intValue;
                if (resource_value == null) {
                    resource_intValue = 0;
                } else {
                    resource_intValue = Math.toIntExact((long) resource_value);
                }
                bb = ByteBuffer.allocate(Integer.BYTES);
                bb.putInt(resource_intValue);
                byteArray = bb.array();
                dataList.add(byteArray[0]);
                dataList.add(byteArray[1]);
                dataList.add(byteArray[2]);
                dataList.add(byteArray[3]);

                Object reset_value = solution.get("sym_reset_" + i);
                int reset_intValue;
                if (reset_value == null) {
                    reset_intValue = 0;
                } else {
                    reset_intValue = Math.toIntExact((long) reset_value);
                }
                dataList.add((byte) reset_intValue);
            }
        }

        byte[] data = new byte[dataList.size()];
        for (int i=0; i<data.length; i++) {
            data[i] = dataList.get(i);
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
