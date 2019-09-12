import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;
import test.org.apache.commons.math3.analysis.differentiation.DerivativeStructure_v1;

public class SymbcDriver {

    /* v1 has problem when both x and y value == 0 -> NaN */
    public static void main(String[] args) {

        int x_variables;/* = 2; */
        int x_order;/* = 2; */
        int x_index;/* = 1; */
        double x_value;/* = +0.0; */

        int y_variables;/* = 2; */
        int y_order;/* = 2; */
        int y_index;/* = 1; */
        double y_value;/* = +0.0; */

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read input file. */
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                // x_variables = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_x_variables");
                // x_variables = ByteBuffer.wrap(bytes).getInt() % 4;

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                // x_order = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_x_order");
                // x_order = ByteBuffer.wrap(bytes).getInt() % 4;

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                x_index = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_x_index");
                // x_index = ByteBuffer.wrap(bytes).getInt() % 4;

                bytes = new byte[Double.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                x_value = Debug.addSymbolicDouble(Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()),
                        "sym_x_value");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                // y_variables = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_y_variables");
                // y_variables = ByteBuffer.wrap(bytes).getInt() % 4;

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                // y_order = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_y_order");
                // y_order = ByteBuffer.wrap(bytes).getInt() % 4;

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                y_index = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_y_index");
                // y_index = ByteBuffer.wrap(bytes).getInt()%4;

                bytes = new byte[Double.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                // y_value = Debug.addSymbolicDouble(ByteBuffer.wrap(bytes).getDouble(), "sym_y_value");
                y_value = Debug.addSymbolicDouble(Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong()),
                        "sym_y_value");

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }
        } else {
            // x_variables = Debug.makeSymbolicInteger("sym_x_variables");
            // x_order = Debug.makeSymbolicInteger("sym_x_order");
            x_index = Debug.makeSymbolicInteger("sym_x_index");
            x_value = Debug.makeSymbolicDouble("sym_x_value");
            // y_variables = Debug.makeSymbolicInteger("sym_y_variables");
            // y_order = Debug.makeSymbolicInteger("sym_y_order");
            y_index = Debug.makeSymbolicInteger("sym_y_index");
            y_value = Debug.makeSymbolicDouble("sym_y_value");
        }

        x_variables = 2;
        x_order = 2;
        // x_index = 1;
        y_variables = 2;
        y_order = 2;
        // y_index = 1;

        System.out.println("x_variables=" + x_variables);
        System.out.println("x_order" + x_order);
        System.out.println("x_index=" + x_index);
        System.out.println("x_value=" + x_value);
        System.out.println("y_variables=" + y_variables);
        System.out.println("y_order=" + y_order);
        System.out.println("y_index=" + y_index);
        System.out.println("y_value=" + y_value);
        
        DerivativeStructure_v1 x = new DerivativeStructure_v1(x_variables, x_order, x_index, x_value);
        DerivativeStructure_v1 y = new DerivativeStructure_v1(y_variables, y_order, y_index, y_value);

        DerivativeStructure_v1.atan2(x, y).getValue();
        System.out.println("Done.");
    }

}
