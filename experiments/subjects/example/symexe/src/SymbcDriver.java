import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;

public class SymbcDriver {

    public static void main(String[] args) {

        int x;
        int y;

        if (args.length == 1) {
            String fileName = args[0].replace("#", ",");
            
            try (FileInputStream fis = new FileInputStream(fileName)) {
            
                byte[] bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                x = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_0");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                y = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_1");
                
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }

        } else {
            x = Debug.makeSymbolicInteger("sym_0");
            y = Debug.makeSymbolicInteger("sym_1");
        }
        
        System.out.println("x=" + x);
        System.out.println("y=" + y);

        Example.calculate(x, y);
        
        System.out.println("Done.");
    }

}
