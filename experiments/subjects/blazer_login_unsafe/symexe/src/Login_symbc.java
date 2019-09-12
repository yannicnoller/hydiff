import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class Login_symbc {

    public static final int MAX_PASSWORD_LENGTH = 64; // bytes
    public static final boolean SAFE_MODE = false;

    public static void main(String[] args) {

        /* Read input. */
        String username = "username"; // irrelevant
        byte[] guess;
        byte[] realpassword_secret1;
        byte[] realpassword_secret2;
        int n;

        if (args.length == 1) {
            
            String fileName = args[0].replace("#", ",");

            /* Read all data. */
            List<Byte> values = new ArrayList<>();
            int i = 0;
            try (FileInputStream fis = new FileInputStream(fileName)) {
                int value;
                while (((value = fis.read()) != -1) && (i < 3 * MAX_PASSWORD_LENGTH)) {
                    values.add((byte) (value % 127));
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
            guess = new byte[n_concrete];
            for (i = 0; i < n_concrete; i++) {
                guess[i] = Debug.addSymbolicByte(values.get(i), "sym_0_" + i);
            }

            // Read secret1 and insert symbolic variables.
            realpassword_secret1 = new byte[n_concrete];
            for (i = 0; i < n_concrete; i++) {
                realpassword_secret1[i] = Debug.addSymbolicByte(values.get(i + n_concrete), "sym_1_" + i);
            }

            // Read secret2 and insert symbolic variables.
            realpassword_secret2 = new byte[n_concrete];
            for (i = 0; i < n_concrete; i++) {
                realpassword_secret2[i] = Debug.addSymbolicByte(values.get(i + 2 * n_concrete), "sym_2_" + i);
            }

        } else {
            int currentN = Debug.getLastObservedInputSizes()[0];
            guess = new byte[currentN];
            realpassword_secret1 = new byte[currentN];
            realpassword_secret2 = new byte[currentN];

            n = Debug.makeSymbolicInteger("sym_n");
            for (int i = 0; i < currentN; i++) {
                guess[i] = Debug.makeSymbolicByte("sym_0_" + i);
                realpassword_secret1[i] = Debug.makeSymbolicByte("sym_1_" + i);
                realpassword_secret2[i] = Debug.makeSymbolicByte("sym_2_" + i);
            }
        }

        System.out.println("username=" + username);
        System.out.println("password=" + Arrays.toString(guess));
        System.out.println("secret1=" + Arrays.toString(realpassword_secret1));
        System.out.println("secret2=" + Arrays.toString(realpassword_secret2));

        driver(n, username, guess, realpassword_secret1, realpassword_secret2);

        System.out.println("Done.");
    }

    public static void driver(int n, String username, byte[] guess, byte[] secret_1, byte[] secret_2) {
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
        case 4:
            sizes[0] = 4;
            break;
        case 5:
            sizes[0] = 5;
            break;
        case 6:
            sizes[0] = 6;
            break;
        case 7:
            sizes[0] = 7;
            break;
        case 8:
            sizes[0] = 8;
            break;
        case 9:
            sizes[0] = 9;
            break;
        case 10:
            sizes[0] = 10;
            break;
        case 11:
            sizes[0] = 11;
            break;
        case 12:
            sizes[0] = 12;
            break;
        case 13:
            sizes[0] = 13;
            break;
        case 14:
            sizes[0] = 14;
            break;
        case 15:
            sizes[0] = 15;
            break;
        case 16:
            sizes[0] = 16;
            break;
        case 17:
            sizes[0] = 17;
            break;
        case 18:
            sizes[0] = 18;
            break;
        case 19:
            sizes[0] = 19;
            break;
        case 20:
            sizes[0] = 20;
            break;
        case 21:
            sizes[0] = 21;
            break;
        case 22:
            sizes[0] = 22;
            break;
        case 23:
            sizes[0] = 23;
            break;
        case 24:
            sizes[0] = 24;
            break;
        case 25:
            sizes[0] = 25;
            break;
        case 26:
            sizes[0] = 26;
            break;
        case 27:
            sizes[0] = 27;
            break;
        case 28:
            sizes[0] = 28;
            break;
        case 29:
            sizes[0] = 29;
            break;
        case 30:
            sizes[0] = 30;
            break;
        case 31:
            sizes[0] = 31;
            break;
        case 32:
            sizes[0] = 32;
            break;
        case 33:
            sizes[0] = 33;
            break;
        case 34:
            sizes[0] = 34;
            break;
        case 35:
            sizes[0] = 35;
            break;
        case 36:
            sizes[0] = 36;
            break;
        case 37:
            sizes[0] = 37;
            break;
        case 38:
            sizes[0] = 38;
            break;
        case 39:
            sizes[0] = 39;
            break;
        case 40:
            sizes[0] = 40;
            break;
        case 41:
            sizes[0] = 41;
            break;
        case 42:
            sizes[0] = 42;
            break;
        case 43:
            sizes[0] = 43;
            break;
        case 44:
            sizes[0] = 44;
            break;
        case 45:
            sizes[0] = 45;
            break;
        case 46:
            sizes[0] = 46;
            break;
        case 47:
            sizes[0] = 47;
            break;
        case 48:
            sizes[0] = 48;
            break;
        case 49:
            sizes[0] = 49;
            break;
        case 50:
            sizes[0] = 50;
            break;
        case 51:
            sizes[0] = 51;
            break;
        case 52:
            sizes[0] = 52;
            break;
        case 53:
            sizes[0] = 53;
            break;
        case 54:
            sizes[0] = 54;
            break;
        case 55:
            sizes[0] = 55;
            break;
        case 56:
            sizes[0] = 56;
            break;
        case 57:
            sizes[0] = 57;
            break;
        case 58:
            sizes[0] = 58;
            break;
        case 59:
            sizes[0] = 59;
            break;
        case 60:
            sizes[0] = 60;
            break;
        case 61:
            sizes[0] = 61;
            break;
        case 62:
            sizes[0] = 62;
            break;
        case 63:
            sizes[0] = 63;
            break;
        case 64:
            sizes[0] = 64;
            break;
        default:
            throw new RuntimeException("unintended input size");
        }
        Debug.setLastObservedInputSizes(sizes);

        byte[] secret = new byte[secret_1.length];
        for (int i = 0; i < secret.length; i++) {
            secret[i] = (byte) change(secret_1[i], secret_2[i]);
        }

        Object res;
        if (SAFE_MODE) {
            res = Login.login_safe(secret, guess, username);    
        } else {
            res = Login.login_unsafe(secret, guess, username);
        }

        // System.out.println("res=" + res);
    }

}
