import static gov.nasa.jpf.symbc.ChangeAnnotation.change;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;

public class SymbcDriver {

    public static final int P_CODE_LENGTH = 128;

    public static void main(String[] args) {

        byte[] secret1_pw;
        byte[] secret2_pw;
        byte[] public_guess;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            byte[] bytes;
            secret1_pw = new byte[P_CODE_LENGTH];
            secret2_pw = new byte[P_CODE_LENGTH];

            try (FileInputStream fis = new FileInputStream(fileName)) {
                for (int i = 0; i < P_CODE_LENGTH; i++) {
                    bytes = new byte[1];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough input data...");
                    }
                    secret1_pw[i] = Debug.addSymbolicByte(bytes[0], "sym_0_" + i);
                }
                for (int i = 0; i < P_CODE_LENGTH; i++) {
                    bytes = new byte[1];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough input data...");
                    }
                    secret2_pw[i] = Debug.addSymbolicByte(bytes[0], "sym_1_" + i);
                }
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

            System.out.println("secret1=" + Arrays.toString(secret1_pw));
            System.out.println("secret2=" + Arrays.toString(secret2_pw));

        } else {
            secret1_pw = new byte[P_CODE_LENGTH];
            secret2_pw = new byte[P_CODE_LENGTH];
            for (int i = 0; i < secret1_pw.length; i++) {
                secret1_pw[i] = Debug.makeSymbolicByte("sym_0_" + i);
                secret2_pw[i] = Debug.makeSymbolicByte("sym_1_" + i);
            }
        }
        
        /* Read static image. */
        String filePath = Debug.getDataDir() + "/public.jpg";
        List<Byte> values_public = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] bytes = new byte[1];
            while ((fis.read(bytes)) != -1) {
                values_public.add(bytes[0]);
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }
        public_guess = new byte[values_public.size()];
        for (int i = 0; i < public_guess.length; i++) {
            public_guess[i] = values_public.get(i);
        }
        
        /* dummy call to symbolic decision */
        if (secret1_pw[0] > 0) {
            int b = 0;
        }

        byte[] secret = new byte[secret1_pw.length];
        for (int i = 0; i < secret.length; i++) {
            secret[i] = (byte) change(secret1_pw[i], secret2_pw[i]);
        }
        ImageMatcherWorker.test(public_guess, secret);

        System.out.println("Done.");
    }
    
}
