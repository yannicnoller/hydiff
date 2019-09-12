package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/*! problems with negative integers*/
public class FullDoubleArrayIOUtils extends IOUtils {

    public static final String ID = "full-double-byte-array";
    
    public int N;
    
    public FullDoubleArrayIOUtils(int N) {
        this.N = N;
    }


    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {
        generateByteArrayInput(pc, solution, outputFile);
    }

    private void generateByteArrayInput(PathCondition pc, Map<String, Object> solution, String outputFile) {
        byte[] data = new byte[N  * Double.BYTES];
        int dataCounter = 0;
        for (int i = 0; i < N; i++) {
            
            Object value = solution.get("sym_" + i);
            double doubleValue;
            if (value != null) {
                doubleValue = (double) value;
            } else {
                doubleValue = 0.0;
            }

            // Transform int in byte[].
            ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
            bb.putLong(Double.doubleToLongBits(doubleValue));
            byte[] byteArray = bb.array();

            // Save byte values in data[].
            data[dataCounter++] = byteArray[0];
            data[dataCounter++] = byteArray[1];
            data[dataCounter++] = byteArray[2];
            data[dataCounter++] = byteArray[3];
            data[dataCounter++] = byteArray[4];
            data[dataCounter++] = byteArray[5];
            data[dataCounter++] = byteArray[6];
            data[dataCounter++] = byteArray[7];
            
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
