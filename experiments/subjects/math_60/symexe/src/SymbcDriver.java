import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;
import test.org.apache.commons.math.MathException;
import test.org.apache.commons.math.distribution.NormalDistribution;
import test.org.apache.commons.math.distribution.NormalDistributionImpl1;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class SymbcDriver {

    public static void main(String[] args) {

        int mean;
        int std;
        double value;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read input file. */
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                mean = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_mean");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                std = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_std");

                bytes = new byte[Long.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                value = Debug.addSymbolicDouble((double) Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()), "sym_value");
                
                
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }
        } else {
            mean = Debug.makeSymbolicInteger("sym_mean");
            std = Debug.makeSymbolicInteger("sym_std");
            value = Debug.makeSymbolicDouble("sym_value");
        }
        
        System.out.println("mean=" + mean);
        System.out.println("std=" + std);
        System.out.println("value=" + value);
        
        NormalDistribution distribution1 = new NormalDistributionImpl1(mean, std);
        
        System.out.println("#1"); // TODO YN: remove
        
        // YN: dummy pc
        if (mean > 0) {
            int g = 3;
        }
        
        try {
            distribution1.cumulativeProbability(value);
            System.out.println("#end");
        } catch (MathException e) {
            throw new RuntimeException(e);
        }
    }

}
