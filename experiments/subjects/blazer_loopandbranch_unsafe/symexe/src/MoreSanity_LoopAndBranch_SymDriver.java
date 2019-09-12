import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class MoreSanity_LoopAndBranch_SymDriver {

    /* no maximum length of secret necessary because it is just an integer value */
    public static final boolean SAFE_MODE = false;

    public static void main(String[] args) {

        int secret1_taint = 0;
        int secret2_taint = 0;
        int public_a = 0;
        int n = 3; // how many variables

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            // Read all inputs.
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Integer.BYTES];
                int i = 0;
                while ((fis.read(bytes) != -1) && (i < n)) {
                    switch (i) {
                    case 0:
                        secret1_taint = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_0");
                        break;
                    case 1:
                        secret2_taint = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_1");
                        break;
                    case 2:
                        public_a = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_2");
                        break;
                    default:
                        throw new RuntimeException("unreachable");
                    }
                    i++;
                }

                if (i != n) {
                    throw new RuntimeException("reading imcomplete: too less data");
                }

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }
            
        } else {
            secret1_taint = Debug.makeSymbolicInteger("sym_0");
            secret2_taint = Debug.makeSymbolicInteger("sym_1");
            public_a = Debug.makeSymbolicInteger("sym_2");
        }

        System.out.println("secret1=" + secret1_taint);
        System.out.println("secret2=" + secret1_taint);
        System.out.println("public=" + public_a);
        
        driver(public_a, secret1_taint, secret2_taint);
        
        System.out.println("Done.");

    }
    
    public static void driver(int public_a, int secret_taint1, int secret_taint2) {
        
        if (public_a > 0) {
            System.out.println( );
        }
        
        int secret_taint = change(secret_taint1, secret_taint2);

        if (SAFE_MODE) {
            MoreSanity.loopAndbranch_safe(public_a, secret_taint);
        } else {
            MoreSanity.loopAndbranch_unsafe(public_a, secret_taint);
        }
        
//        MoreSanity.loopAndbranch_unsafe(public_a, secret_taint1);
    }

}
