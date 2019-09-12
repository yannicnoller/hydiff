import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.CFGSummary;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class FuzzDriver {

    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        int x;
        int y;

        /* Read input file. */
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            x = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            y = ByteBuffer.wrap(bytes).getInt();

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }

        System.out.println("x=" + x);
        System.out.println("y=" + y);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = Example1.calculate(x, y);
        } catch (Throwable e) {
            res1 = e;
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;

        Mem.clear();
        DecisionHistory.clear();
        CFGSummary.clear();
        Object res2 = null;
        try {
            res2 = Example2.calculate(x, y);
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
