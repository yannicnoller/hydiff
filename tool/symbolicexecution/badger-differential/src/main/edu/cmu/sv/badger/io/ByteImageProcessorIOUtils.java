package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public class ByteImageProcessorIOUtils extends IOUtils {

    public static final String ID = "byte-image";

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteImageInput(pc, solution, outputFile);
    }

    private void generateByteImageInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        // Get data to write file.
        byte[] data = generateByteArrayForOfImage(pc, solution, outputFile);

        try {
            Files.write(Paths.get(outputFile), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] generateByteArrayForOfImage(PathCondition pc, Map<String, Object> valueMapping, String outputfile) {
        // only works for 2x2
        int height = 2;
        int width = 2;
        byte[] imageInByte = new byte[height * width * Integer.BYTES];
        int pixelCount = 0;
        StringBuilder extractedSolution = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Object value = valueMapping.get("sym_" + x + "_" + y);
                int pixelValue;
                if (value == null) {
                    pixelValue = 0;
                } else {
                    pixelValue = Math.toIntExact((long) value);
                }

                extractedSolution.append("sym_" + x + "_" + y + " = " + value + "\n");

                ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
                bb.putInt(pixelValue);
                byte[] byteArray = bb.array();
                imageInByte[pixelCount++] = byteArray[0];
                imageInByte[pixelCount++] = byteArray[1];
                imageInByte[pixelCount++] = byteArray[2];
                imageInByte[pixelCount++] = byteArray[3];
            }
        }
        return imageInByte;
    }

}
