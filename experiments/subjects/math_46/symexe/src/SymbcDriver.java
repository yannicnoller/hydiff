import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;
import test.org.apache.commons.math.complex.Complex1;

public class SymbcDriver {

    /* v1 has problem when both x and y value == 0 -> NaN */
    public static void main(String[] args) {

        double x_real;
        double x_imag;
        double y_real;
        double y_imag;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read input file. */
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Double.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                x_real = Debug.addSymbolicDouble(Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()), "sym_0");

                bytes = new byte[Double.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                x_imag = Debug.addSymbolicDouble(Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()), "sym_1");

                bytes = new byte[Double.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                y_real = Debug.addSymbolicDouble(Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()), "sym_2");

                bytes = new byte[Double.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                y_imag = Debug.addSymbolicDouble(Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()), "sym_3");

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }
        } else {
            x_real = Debug.makeSymbolicDouble("sym_0");
            x_imag = Debug.makeSymbolicDouble("sym_1");
            y_real = Debug.makeSymbolicDouble("sym_2");
            y_imag = Debug.makeSymbolicDouble("sym_3");
        }

        System.out.println("x_real=" + x_real);
        System.out.println("x_imag=" + x_imag);
        System.out.println("y_real=" + y_real);
        System.out.println("y_imag=" + y_imag);

        Complex1 x1 = new Complex1(x_real, x_imag);
        Complex1 y1 = new Complex1(y_real, y_imag);
        x1.divide(y1);
    }

}
