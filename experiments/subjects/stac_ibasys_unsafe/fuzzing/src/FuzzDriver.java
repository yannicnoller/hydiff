import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class FuzzDriver {

    public static final int P_CODE_LENGTH = 128;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        byte[] secret1_pw;
        byte[] secret2_pw;
        byte[] public_guess;

        // Read all inputs.
        byte[] bytes;
        secret1_pw = new byte[P_CODE_LENGTH];
        secret2_pw = new byte[P_CODE_LENGTH];
        try (FileInputStream fis = new FileInputStream(args[0])) {
            for (int i = 0; i < P_CODE_LENGTH; i++) {
                bytes = new byte[1];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough input data...");
                }
                secret1_pw[i] = bytes[0];
            }
            for (int i = 0; i < P_CODE_LENGTH; i++) {
                bytes = new byte[1];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough input data...");
                }
                secret2_pw[i] = bytes[0];
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        /* Read static image. */
        List<Byte> values_public = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream("./data/public.jpg")) {
            bytes = new byte[1];
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

        System.out.println("secret1=" + Arrays.toString(secret1_pw));
        System.out.println("secret2=" + Arrays.toString(secret2_pw));
        // System.out.println("public_guess=" + Arrays.toString(public_guess));

        // init
        ImageMatcherWorker.test(public_guess, secret1_pw);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = ImageMatcherWorker.test(public_guess, secret1_pw);
        } catch (Throwable e) {
            res1 = e;
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;

        Mem.clear();
        DecisionHistory.clear();
        Object res2 = null;
        try {
            res2 = ImageMatcherWorker.test(public_guess, secret2_pw);
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
        // System.out.println("dec1=" + Arrays.toString(dec1));
        // System.out.println("dec2=" + Arrays.toString(dec2));
        // System.out.println("decisionDiff=" + d.mergedHistory);
        System.out.println("cost1=" + cost1);
        System.out.println("cost2=" + cost2);

        System.out.println("Done.");
    }

}
