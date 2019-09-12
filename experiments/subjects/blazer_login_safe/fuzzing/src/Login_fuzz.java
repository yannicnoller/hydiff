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

public class Login_fuzz {

    public static final int MAX_PASSWORD_LENGTH = 64; // bytes
    public static final boolean SAFE_MODE = true;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        /* Read input. */
        String username = "username"; // irrelevant
        byte[] guess;
        byte[] realpassword_secret1;
        byte[] realpassword_secret2;

        List<Byte> values = new ArrayList<>();
        int i = 0;
        try (FileInputStream fis = new FileInputStream(args[0])) {
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
        
        int eachSize = values.size() / 3;

        guess = new byte[eachSize];
        for (i = 0; i < eachSize; i++) {
            guess[i] = values.get(i);
        }

        realpassword_secret1 = new byte[eachSize];
        for (i = 0; i < eachSize; i++) {
            realpassword_secret1[i] = values.get(i + eachSize);
        }

        realpassword_secret2 = new byte[eachSize];
        for (i = 0; i < eachSize; i++) {
            realpassword_secret2[i] = values.get(i + 2 * eachSize);
        }

        System.out.println("username=" + username);
        System.out.println("password=" + Arrays.toString(guess));
        System.out.println("secret1=" + Arrays.toString(realpassword_secret1));
        System.out.println("secret2=" + Arrays.toString(realpassword_secret2));

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            if (SAFE_MODE) {
                res1 = Login.login_safe(realpassword_secret1, guess, username);                
            } else {
                res1 = Login.login_unsafe(realpassword_secret1, guess, username);
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
                res2 = Login.login_safe(realpassword_secret2, guess, username);               
            } else {
                res2 = Login.login_unsafe(realpassword_secret2, guess, username);
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
