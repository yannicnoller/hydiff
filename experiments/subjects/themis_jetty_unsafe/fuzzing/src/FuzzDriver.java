import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class FuzzDriver {

    public static final boolean SAFE_MODE = false;
    public static final int MAX_INPUT_LENGTH = 100; // chars

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        char[] secret1_arr;
        char[] secret2_arr;
        char[] public_arr;

        // Read all inputs.
        List<Character> values = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Character.BYTES];
            while (((fis.read(bytes)) != -1) && values.size() < 3 * MAX_INPUT_LENGTH) {
                char value = ByteBuffer.wrap(bytes).getChar();
                values.add(value);
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        int n = values.size() / 3;
        System.out.println("n=" + n);
        
        // Read public.
        public_arr = new char[n];
        for (int i = 0; i < n; i++) {
            public_arr[i] = values.get(i);
        }

        // Read secret1.
        secret1_arr = new char[n];
        for (int i = 0; i < n; i++) {
            secret1_arr[i] = values.get(i + n);
        }

        // Read secret2.
        secret2_arr = new char[n];
        for (int i = 0; i < n; i++) {
            secret2_arr[i] = values.get(i + 2 * n);
        }

        System.out.println("public_actual=" + Arrays.toString(public_arr));
        System.out.println("secret1_expected=" + Arrays.toString(secret1_arr));
        System.out.println("secret2_expected=" + Arrays.toString(secret2_arr));

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            if (SAFE_MODE) {
                res1 = Credential.stringEquals_safe(public_arr, secret1_arr);
            } else {
                res1 = Credential.stringEquals_original(public_arr, secret1_arr);
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
                res2 = Credential.stringEquals_safe(public_arr, secret2_arr);
            } else {
                res2 = Credential.stringEquals_original(public_arr, secret2_arr);
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
