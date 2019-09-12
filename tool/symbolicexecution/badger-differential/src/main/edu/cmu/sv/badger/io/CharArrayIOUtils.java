package edu.cmu.sv.badger.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

public class CharArrayIOUtils extends IOUtils {

    public static final String ID = "char-byte-array";

    public int N;

    public CharArrayIOUtils(int N) {
        this.N = N;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {

        // char[] str = new char[N];
        // for (int i = 0; i < N; i++) {
        // // Get char value.
        // Object value = solution.get("sym_" + i);
        // char charValue;
        // if (value == null) {
        // charValue = 0;
        // } else {
        // int t = Math.toIntExact((long) value);
        // charValue = (char) t;
        // }
        // str[i] = charValue;
        // }
        // String content = new String(str);
        //
        // try {
        // Files.write(Paths.get(outputFile), content.getBytes());
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        /*
         * Extracts the chars from the solution model and stores them as a sequence of byte values. No String is
         * constructed in order to avoid encoding problems.
         */

        byte[] data = new byte[N * Character.BYTES];
        int dataCounter = 0;
        for (int i = 0; i < N; i++) {

            // Get char value from solution model.
            Object value = solution.get("sym_" + i);
            char charValue;
            if (value == null) {
                charValue = 0;
            } else {
                int t = Math.toIntExact((long) value);
                charValue = (char) t;
            }

            // Transform char in byte[].
            ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
            bb.putChar(charValue);
            byte[] byteArray = bb.array();

            // Save byte values in data[].
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

    public static void main(String[] args) {

        String inputFileName = "/Users/yannic/repositories/cmu/memoise/issta-experiments/03-regex/kelinci_analysis/resources/regex-with-quotes.txt";
        String outputFileName = "/Users/yannic/repositories/cmu/memoise/issta-experiments/03-regex/kelinci_analysis/resources-byte/regex-with-quotes-byte.txt";

        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(inputFileName)));
            content = content.trim();

            byte[] data = new byte[content.length() * Character.BYTES];
            int dataCounter = 0;
            for (char value : content.toCharArray()) {
                // Transform char in byte[].
                ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
                bb.putChar(value);
                byte[] byteArray = bb.array();

                // Save byte values in data[].
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
            }

            File outputFile = new File(outputFileName);
            outputFile.createNewFile();
            Files.write(outputFile.toPath(), data);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
