import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;
import modpow.ModPow;

public class FuzzDriver_BigInt {

    public static int MAX_LENGTH = 3;
    public static final boolean SAFE_MODE = false;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        /* Read input. */
        BigInteger modulus_fixed = new BigInteger("1717", 10);
        BigInteger base_public;
        BigInteger exponent_secret1;
        BigInteger exponent_secret2;

        List<Byte> values = new ArrayList<>();
        int i = 0;
        try (FileInputStream fis = new FileInputStream(args[0])) {
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

        int eachSize = values.size() / 3;

        byte[] base_public_arr = new byte[eachSize];
        for (i = 0; i < base_public_arr.length; i++) {
            base_public_arr[i] = values.get(i);
        }
        base_public = new BigInteger(base_public_arr);

        byte[] exponent_secret1_arr = new byte[eachSize];
        for (i = 0; i < exponent_secret1_arr.length; i++) {
            exponent_secret1_arr[i] = values.get(i + eachSize);
        }
        exponent_secret1 = new BigInteger(exponent_secret1_arr);

        byte[] exponent_secret2_arr = new byte[eachSize];
        for (i = 0; i < exponent_secret2_arr.length; i++) {
            exponent_secret2_arr[i] = values.get(i + 2 * eachSize);
        }
        exponent_secret2 = new BigInteger(exponent_secret2_arr);

        System.out.println("modulus_fixed=" + modulus_fixed);
        System.out.println("base_public=" + base_public);
        System.out.println("exponent_1=" + Arrays.toString(exponent_secret1_arr));
        System.out.println("exponent_2=" + Arrays.toString(exponent_secret2_arr));

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            if (SAFE_MODE) {
                res1 = ModPow.modPow(base_public, exponent_secret1, modulus_fixed);     
            } else {
                res1 = ModPow.modPowNoNoise(base_public, exponent_secret1, modulus_fixed);
            }
        } catch (Throwable e) {
            res1 = e;
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;

        Mem.clear();
        DecisionHistory.clear();
        Object res2 = null;
        try {
            if (SAFE_MODE) {
                res2 = ModPow.modPow(base_public, exponent_secret2, modulus_fixed);     
            } else {
                res2 = ModPow.modPowNoNoise(base_public, exponent_secret2, modulus_fixed);
            }
        } catch (Throwable e) {
            res2 = e;
        }
        boolean[] dec2 = DecisionHistory.getDecisions();
        long cost2 = Mem.instrCost;

        DecisionHistoryDifference d = DecisionHistoryDifference.createDecisionHistoryDifference(dec1, dec2);
        Kelinci.setNewDecisionDifference(d);
        Kelinci.setNewOutputDifference(new OutputSummary(res1, res2));
        Kelinci.addCost(Math.abs(cost1 - cost2));

        System.out.println("res1=" + res1);
        System.out.println("res2=" + res2);
        System.out.println("dec1=" + Arrays.toString(dec1));
        System.out.println("dec2=" + Arrays.toString(dec2));
        System.out.println("decisionDiff=" + d.mergedHistory);
        System.out.println("cost1=" + cost1);
        System.out.println("cost2=" + cost2);

        System.out.println("Done.");
    }

}
