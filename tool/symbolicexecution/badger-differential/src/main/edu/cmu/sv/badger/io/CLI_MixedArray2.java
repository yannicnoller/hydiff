package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class CLI_MixedArray2 extends IOUtils {

    public static final String ID = "cli-mixed-array2";

    public int numberOfOptions;
    public int sizeOfValues;

    public CLI_MixedArray2(int numberOfOptions, int sizeOfValues) {
        this.numberOfOptions = numberOfOptions;
        this.sizeOfValues = sizeOfValues;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {

        int neededLength = Character.BYTES /* Byte.Bytes */ * numberOfOptions // sym_opt_i
                + Character.BYTES * numberOfOptions * sizeOfValues // sym_val_i_j
                + Byte.BYTES // sym_optidx
                + Byte.BYTES * numberOfOptions // sym_dec_i
                + Byte.BYTES * numberOfOptions // sym_prop_i
                + Character.BYTES * numberOfOptions // sym_prop_val_i
                + Byte.BYTES // sym_stopAtNonOptions
                + Byte.BYTES * numberOfOptions; // sym_hasArg_i

        byte[] data = new byte[neededLength];

        int dataCounter = 0;
        Object value;
        for (int i = 0; i < numberOfOptions; i++) {

            value = solution.get("sym_opt_" + i);
            // int letter_value;
            // if (value == null) {
            // letter_value = 0;
            // } else {
            // letter_value = Math.toIntExact((long) value);
            // }
            // data[dataCounter++] = (byte) letter_value;
            char letter_char_value;
            if (value == null) {
                letter_char_value = 0;
            } else {
                int t = Math.toIntExact((long) value);
                letter_char_value = (char) t;
            }
            ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
            bb.putChar(letter_char_value);
            byte[] byteArray = bb.array();
            data[dataCounter++] = byteArray[0];
            data[dataCounter++] = byteArray[1];

            for (int j = 0; j < sizeOfValues; j++) {

                value = solution.get("sym_val_" + i + "_" + j);
                char charValue;
                if (value == null) {
                    charValue = 0;
                } else {
                    int t = Math.toIntExact((long) value);
                    charValue = (char) t;
                }
                bb = ByteBuffer.allocate(Character.BYTES);
                bb.putChar(charValue);
                byteArray = bb.array();
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];

            }
        }

        value = solution.get("sym_optidx");
        int opt_idx_value;
        if (value == null) {
            opt_idx_value = 0;
        } else {
            opt_idx_value = Math.toIntExact((long) value);
        }
        data[dataCounter++] = (byte) opt_idx_value;

        for (int i = 0; i < numberOfOptions; i++) {
            value = solution.get("sym_dec_" + i);
            int dec_value;
            if (value == null) {
                dec_value = 0;
            } else {
                dec_value = Math.toIntExact((long) value);
            }
            data[dataCounter++] = (byte) dec_value;
        }

        for (int i = 0; i < numberOfOptions; i++) {
            value = solution.get("sym_prop_" + i);
            int prop_value;
            if (value == null) {
                prop_value = 0;
            } else {
                prop_value = Math.toIntExact((long) value);
            }
            data[dataCounter++] = (byte) prop_value;

            value = solution.get("sym_prop_val_" + i);
            char prop_valValue;
            if (value == null) {
                prop_valValue = 0;
            } else {
                int t = Math.toIntExact((long) value);
                prop_valValue = (char) t;
            }
            ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
            bb.putChar(prop_valValue);
            byte[] byteArray = bb.array();
            data[dataCounter++] = byteArray[0];
            data[dataCounter++] = byteArray[1];
        }

        value = solution.get("sym_stopAtNonOptions");
        int stopAtNonOptions_value;
        if (value == null) {
            stopAtNonOptions_value = 0;
        } else {
            stopAtNonOptions_value = Math.toIntExact((long) value);
        }
        data[dataCounter++] = (byte) stopAtNonOptions_value;

        for (int i = 0; i < numberOfOptions; i++) {
            value = solution.get("sym_hasArg_" + i);
            int hasArg_value;
            if (value == null) {
                hasArg_value = 0;
            } else {
                hasArg_value = Math.toIntExact((long) value);
            }
            data[dataCounter++] = (byte) hasArg_value;
        }

        try {
            Files.write(Paths.get(outputFile), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        byte[] data = new byte[Integer.BYTES * 1 + Character.BYTES * 3 * 3 + Character.BYTES * 5];
        int dataCounter = 0;

        int numberOfValues = 3;
        String[] values = { "abc", "edf", "ghi" };
        String opt = "fooAB";

        data[dataCounter++] = (byte) numberOfValues;

        for (String value : values) {
            for (char charValue : value.toCharArray()) {
                ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
                bb.putChar(charValue);
                byte[] byteArray = bb.array();
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
            }
        }

        for (char charValue : opt.toCharArray()) {
            ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
            bb.putChar(charValue);
            byte[] byteArray = bb.array();
            data[dataCounter++] = byteArray[0];
            data[dataCounter++] = byteArray[1];
        }

        try {
            Files.write(Paths.get("/Users/yannic/example"), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done.");

    }
}
