import static gov.nasa.jpf.symbc.ChangeAnnotation.change;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;

public class SymbcDriver {

    public static final boolean SAFE_MODE = false;
    public static final int MAX_INPUT_LENGTH = 100; // chars

    public static void main(String[] args) {

        char[] secret1_arr;
        char[] secret2_arr;
        char[] publicCred_arr;
        int n;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            // Read all inputs.
            List<Character> values = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Character.BYTES];
                while (((fis.read(bytes)) != -1) && values.size() < MAX_INPUT_LENGTH) {
                    char value = ByteBuffer.wrap(bytes).getChar();
                    values.add(value);
                }
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

            int n_concrete = values.size() / 3;
            n = Debug.addSymbolicInt(values.size() / 3, "sym_n");

            // Read public.
            publicCred_arr = new char[n_concrete];
            for (int i = 0; i < n_concrete; i++) {
                publicCred_arr[i] = Debug.addSymbolicChar(values.get(i), "sym_0_" + i);
            }

            // Read secret1.
            secret1_arr = new char[n_concrete];
            for (int i = 0; i < n_concrete; i++) {
                secret1_arr[i] = Debug.addSymbolicChar(values.get(i + n_concrete), "sym_1_" + i);
            }

            // Read secret2.
            secret2_arr = new char[n_concrete];
            for (int i = 0; i < n_concrete; i++) {
                secret2_arr[i] = Debug.addSymbolicChar(values.get(i + 2 * n_concrete), "sym_2_" + i);
            }

        } else {
            int currentN = Debug.getLastObservedInputSizes()[0];
            publicCred_arr = new char[currentN];
            secret1_arr = new char[currentN];
            secret2_arr = new char[currentN];

            n = Debug.makeSymbolicInteger("sym_n");
            for (int i = 0; i < currentN; i++) {
                publicCred_arr[i] = Debug.makeSymbolicChar("sym_0_" + i);
                secret1_arr[i] = Debug.makeSymbolicChar("sym_1_" + i);
                secret2_arr[i] = Debug.makeSymbolicChar("sym_2_" + i);
            }
        }

        System.out.println("public_actual=" + Arrays.toString(publicCred_arr));
        System.out.println("secret1_expected=" + Arrays.toString(secret1_arr));
        System.out.println("secret2_expected=" + Arrays.toString(secret2_arr));

        driver(n, publicCred_arr, secret1_arr, secret2_arr);

        System.out.println("Done.");
    }

    public static void driver(int n, char[] publicCred, char[] secret_1, char[] secret_2) {
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
         case 65:
            sizes[0] = 65;
            break;
         case 66:
            sizes[0] = 66;
            break;
         case 67:
            sizes[0] = 67;
            break;
         case 68:
            sizes[0] = 68;
            break;
         case 69:
            sizes[0] = 69;
            break;
         case 70:
            sizes[0] = 70;
            break;
         case 71:
            sizes[0] = 71;
            break;
         case 72:
            sizes[0] = 72;
            break;
         case 73:
            sizes[0] = 73;
            break;
         case 74:
            sizes[0] = 74;
            break;
         case 75:
            sizes[0] = 75;
            break;
         case 76:
            sizes[0] = 76;
            break;
         case 77:
            sizes[0] = 77;
            break;
         case 78:
            sizes[0] = 78;
            break;
         case 79:
            sizes[0] = 79;
            break;
         case 80:
            sizes[0] = 80;
            break;
         case 81:
            sizes[0] = 81;
            break;
         case 82:
            sizes[0] = 82;
            break;
         case 83:
            sizes[0] = 83;
            break;
         case 84:
            sizes[0] = 84;
            break;
         case 85:
            sizes[0] = 85;
            break;
         case 86:
            sizes[0] = 86;
            break;
         case 87:
            sizes[0] = 87;
            break;
         case 88:
            sizes[0] = 88;
            break;
         case 89:
            sizes[0] = 89;
            break;
         case 90:
            sizes[0] = 90;
            break;
         case 91:
            sizes[0] = 91;
            break;
         case 92:
            sizes[0] = 92;
            break;
         case 93:
            sizes[0] = 93;
            break;
         case 94:
            sizes[0] = 94;
            break;
         case 95:
            sizes[0] = 95;
            break;
         case 96:
            sizes[0] = 96;
            break;
         case 97:
            sizes[0] = 97;
            break;
         case 98:
            sizes[0] = 98;
            break;
         case 99:
            sizes[0] = 99;
            break;
         case 100:
            sizes[0] = 100;
            break;
        default:
            throw new RuntimeException("unintended input size");
        }
        Debug.setLastObservedInputSizes(sizes);

        char[] secret_arr = new char[secret_1.length];
        for (int i = 0; i < secret_arr.length; i++) {
            secret_arr[i] = change(secret_1[i], secret_2[i]);
        }

        if (SAFE_MODE) {
            Credential.stringEquals_safe(publicCred, secret_arr);
        } else {
            Credential.stringEquals_original(publicCred, secret_arr);
        }
        // System.out.println("res=" + res);
    }

}
