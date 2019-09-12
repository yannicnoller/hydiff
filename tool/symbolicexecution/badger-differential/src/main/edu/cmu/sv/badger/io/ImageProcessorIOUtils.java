package edu.cmu.sv.badger.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public class ImageProcessorIOUtils extends IOUtils {

    public static final String ID = "image";

    private static AtomicInteger lastProcessedFileId = new AtomicInteger(-1);

    @Override
    public Map<String, String> processInput(List<String> inputFiles) {
        Map<String, String> res = new HashMap<>();
        for (String filename : inputFiles) {
            
            // Don't import own exports!
            if (filename.contains("sync:spf")) {
                continue;
            }
            
            try {
                BufferedImage original = ImageIO.read(new File(filename));
                int width = original.getWidth();
                int height = original.getHeight();
                int imageType = original.getType();

                int[][] pixelMatrix = new int[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        pixelMatrix[y][x] = original.getRGB(x, y);
                    }
                }
                
                String imageFilePath = generateByteImage(pixelMatrix, lastProcessedFileId.incrementAndGet() + ":" + width + ":" + height
                        + ":" + imageType + ":");
                if (imageFilePath != null) {
                    res.put(filename, imageFilePath);
                }
                

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        // only works for 2x2
        BufferedImage image = new BufferedImage(2, 2, 5);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Object value = solution.get("sym_" + x + "_" + y);
                int pixelValue;
                if (value == null) {
                    pixelValue = 0;
                } else {
                    pixelValue = Math.toIntExact((long) value);
                }
                image.setRGB(x, y, pixelValue);
            }
        }

        try {
            /*
             * YN: Actually it should be a jpg but for some reason "jpg" generates a wrong
             * file (the colors are not correct displayed). Using an png compression solves
             * it.
             */
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String generateByteImage(int[][] pixelMatrix, String fileName) {
        // Get data to write file.
        byte[] data = generateByteArrayForOfImage(pixelMatrix);
        try {
            File tmpFile = File.createTempFile(fileName, "");
            tmpFile.deleteOnExit();
            Files.write(tmpFile.toPath(), data);
            return tmpFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generateByteArrayForOfImage(int[][] pixelMatrix) {
        int height = pixelMatrix.length;
        int width = pixelMatrix[0].length;
        byte[] imageInByte = new byte[height * width * Integer.BYTES];
        int pixelCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = pixelMatrix[y][x];
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
