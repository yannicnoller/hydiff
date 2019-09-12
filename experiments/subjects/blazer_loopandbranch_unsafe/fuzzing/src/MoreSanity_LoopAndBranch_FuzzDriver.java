import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class MoreSanity_LoopAndBranch_FuzzDriver {

    /* no maximum length of secret necessary because it is just an integer value */
    public static final boolean SAFE_MODE = false;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        int secret1_taint = 0;
        int secret2_taint = 0;
        int public_a = 0;

        int n = 3; // how many variables

        // Read all inputs.
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Integer.BYTES];
            int i = 0;
            while ((fis.read(bytes) != -1) && (i < n)) {
                switch (i) {
                case 0:
                    secret1_taint = ByteBuffer.wrap(bytes).getInt();
                    break;
                case 1:
                    secret2_taint = ByteBuffer.wrap(bytes).getInt();
                    break;
                case 2:
                    public_a = ByteBuffer.wrap(bytes).getInt();
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

        System.out.println("secret1=" + secret1_taint);
        System.out.println("secret2=" + secret1_taint);
        System.out.println("public=" + public_a);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            if (SAFE_MODE) {
                res1 = MoreSanity.loopAndbranch_safe(public_a, secret1_taint);
            } else {
                res1 = MoreSanity.loopAndbranch_unsafe(public_a, secret1_taint);
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
                res2 = MoreSanity.loopAndbranch_safe(public_a, secret2_taint);
            } else {
                res2 = MoreSanity.loopAndbranch_unsafe(public_a, secret2_taint);
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
