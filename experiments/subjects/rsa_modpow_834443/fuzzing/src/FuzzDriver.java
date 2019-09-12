import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;
import modpow.SimplifiedRSA;

public class FuzzDriver {

    public static int MODULO = 834443;
    public static int MAX_HIGH = Integer.MAX_VALUE;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        /* Read input. */
        int modulus_fixed = MODULO;
        int base_public;
        int exponent_secret1;
        int exponent_secret2;

        try (FileInputStream fis = new FileInputStream(args[0])) {

            byte[] bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            base_public = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), MODULO+1);

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            exponent_secret1 = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), MODULO+1);

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            exponent_secret2 = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), MODULO+1);

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        System.out.println("modulus_fixed=" + modulus_fixed);
        System.out.println("base_public=" + base_public);
        System.out.println("exponent_1=" + exponent_secret1);
        System.out.println("exponent_2=" + exponent_secret2);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = SimplifiedRSA.modPowFastKocherReduction(base_public, exponent_secret1, modulus_fixed, MAX_HIGH);
        } catch (Throwable e) {
            res1 = e;
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;

        Mem.clear();
        DecisionHistory.clear();
        Object res2 = null;
        try {
            res2 = SimplifiedRSA.modPowFastKocherReduction(base_public, exponent_secret2, modulus_fixed, MAX_HIGH);
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
