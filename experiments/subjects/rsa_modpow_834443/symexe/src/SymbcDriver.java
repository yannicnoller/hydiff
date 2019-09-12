import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;
import modpow.SimplifiedRSA;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class SymbcDriver {

    public static int MODULO = 834443;
    public static int MAX_HIGH = Integer.MAX_VALUE;

    public static void main(String[] args) {

        int modulus_fixed = MODULO;
        int base_public;
        int exponent_secret1;
        int exponent_secret2;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read all data. */
            try (FileInputStream fis = new FileInputStream(fileName)) {

                byte[] bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                base_public = Debug.addConstrainedSymbolicInt(Math.floorMod(ByteBuffer.wrap(bytes).getInt(), MODULO+1),
                        "sym_0", 0, MODULO);

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                exponent_secret1 = Debug.addConstrainedSymbolicInt(
                        Math.floorMod(ByteBuffer.wrap(bytes).getInt(), MODULO+1), "sym_1", 0, MODULO);

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                exponent_secret2 = Debug.addConstrainedSymbolicInt(
                        Math.floorMod(ByteBuffer.wrap(bytes).getInt(), MODULO+1), "sym_2", 0, MODULO);

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

        } else {

            base_public = Debug.makeSymbolicInteger("sym_0");
            exponent_secret1 = Debug.makeSymbolicInteger("sym_1");
            exponent_secret2 = Debug.makeSymbolicInteger("sym_2");

        }

        System.out.println("modulus_fixed=" + modulus_fixed);
        System.out.println("base_public=" + base_public);
        System.out.println("exponent_1=" + exponent_secret1);
        System.out.println("exponent_2=" + exponent_secret2);

        // Dummy PC.
        if (base_public > 0) {
            int res = 3;
        }

        // Introduce changes.
        int changed_secret = change(exponent_secret1, exponent_secret2);

        SimplifiedRSA.modPowFastKocherReduction(base_public, changed_secret, modulus_fixed, MAX_HIGH);

        System.out.println("Done.");
    }

}
