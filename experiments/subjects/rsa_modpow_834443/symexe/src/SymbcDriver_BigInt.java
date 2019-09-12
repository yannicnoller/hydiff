import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;
import modpow.ModPow;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class SymbcDriver_BigInt {

    public static int MAX_LENGTH = 3;
    public static final boolean SAFE_MODE = false;

    public static void main(String[] args) {

        BigInteger modulus_fixed = new BigInteger("1717", 10);
        BigInteger base_public;
        byte[] exponent_secret1_arr;
        byte[] exponent_secret2_arr;
        int n;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read all data. */
            List<Byte> values = new ArrayList<>();
            int i = 0;
            try (FileInputStream fis = new FileInputStream(fileName)) {
                int value;
                while (((value = fis.read()) != -1) && (i < 3 * MAX_LENGTH)) {
                    values.add((byte) value);
                    i++;
                }
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

            int n_concrete = values.size() / 3;
            n = Debug.addSymbolicInt(values.size() / 3, "sym_n");

            // Read public and insert symbolic variables.
            byte[] base_public_arr = new byte[n_concrete];
            for (i = 0; i < base_public_arr.length; i++) {
                base_public_arr[i] = Debug.addSymbolicByte(values.get(i), "sym_0_" + i);
            }
            base_public = new BigInteger(base_public_arr);

            // Read secret1 and insert symbolic variables.
            exponent_secret1_arr = new byte[n_concrete];
            for (i = 0; i < exponent_secret1_arr.length; i++) {
                exponent_secret1_arr[i] = Debug.addSymbolicByte(values.get(i + n_concrete), "sym_1_" + i);
            }

            // Read secret2 and insert symbolic variables.
            exponent_secret2_arr = new byte[n_concrete];
            for (i = 0; i < exponent_secret2_arr.length; i++) {
                exponent_secret2_arr[i] = Debug.addSymbolicByte(values.get(i + 2 * n_concrete), "sym_2_" + i);
            }

        } else {
            int currentN = Debug.getLastObservedInputSizes()[0];

            byte[] base_public_arr = new byte[currentN];
            for (int i = 0; i < base_public_arr.length; i++) {
                base_public_arr[i] = Debug.makeSymbolicByte("sym_0_" + i);
            }
            base_public = new BigInteger(base_public_arr);

            exponent_secret1_arr = new byte[currentN];
            for (int i = 0; i < exponent_secret1_arr.length; i++) {
                exponent_secret1_arr[i] = Debug.makeSymbolicByte("sym_1_" + i);
            }

            exponent_secret2_arr = new byte[currentN];
            for (int i = 0; i < exponent_secret2_arr.length; i++) {
                exponent_secret2_arr[i] = Debug.makeSymbolicByte("sym_2_" + i);
            }

            n = Debug.makeSymbolicInteger("sym_n");
        }

        System.out.println("modulus_fixed=" + modulus_fixed);
        System.out.println("base_public=" + base_public);
        System.out.println("exponent_1=" + Arrays.toString(exponent_secret1_arr));
        System.out.println("exponent_2=" + Arrays.toString(exponent_secret2_arr));

        driver(n, base_public, modulus_fixed, exponent_secret1_arr, exponent_secret2_arr);

        System.out.println("Done.");
    }

    public static void driver(int n, BigInteger base_public, BigInteger modulus_fixed, byte[] exponent_secret1_arr,
            byte[] exponent_secret2_arr) {
        int[] sizes = new int[1];
        switch (n) {
        case 0:
            sizes[0] = 0;
            break;
        case 1:
            sizes[0] = 1;
            break;
        case 2:
            sizes[0] = 2;
            break;
        case 3:
            sizes[0] = 3;
            break;
        default:
            throw new RuntimeException("unintended input size");
        }
        Debug.setLastObservedInputSizes(sizes);

        // Introduce changes.
        byte[] changed_secret_arr = new byte[exponent_secret1_arr.length];
        for (int i = 0; i < changed_secret_arr.length; i++) {
            changed_secret_arr[i] = (byte) change(exponent_secret1_arr[i], exponent_secret2_arr[i]);
        }
        BigInteger changed_exponent_secret = new BigInteger(changed_secret_arr);

        if (SAFE_MODE) {
            ModPow.modPow(base_public, changed_exponent_secret, modulus_fixed);
        } else {
            ModPow.modPowNoNoise(base_public, changed_exponent_secret, modulus_fixed);
        }
    }
    
}
