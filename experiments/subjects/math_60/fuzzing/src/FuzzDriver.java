import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.CFGSummary;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;
import test.org.apache.commons.math.distribution.NormalDistribution;
import test.org.apache.commons.math.distribution.NormalDistributionImpl0;
import test.org.apache.commons.math.distribution.NormalDistributionImpl1;

public class FuzzDriver {

    /* v1 throws exception for extreme values, value==Double.MAX_VALUE (mean =0, str=1). */
    // int mean = 0;
    // int std = 1;
    // double value = Double.MAX_VALUE;
    //
    // try {
    // NormalDistribution distribution = new NormalDistributionImpl1(mean, std);
    // System.out.println(distribution.cumulativeProbability(value));
    // } catch (MathException e) {
    // e.printStackTrace();
    // }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        int mean;
        int std;
        double value;

        /* Read input file. */
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            mean = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            std = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Long.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            value = (double) Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }
        

        System.out.println("mean=" + mean);
        System.out.println("std=" + std);
        System.out.println("value=" + value);
        
        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            NormalDistribution distribution0 = new NormalDistributionImpl0(mean, std);
            res1 = distribution0.cumulativeProbability(value);
        } catch (Throwable e) {
            res1 = e;
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;
        
        Mem.clear();
        DecisionHistory.clear();
        CFGSummary.clear(); /* Only record the distances for the new version. */
        Object res2 = null;
        try {
            NormalDistribution distribution1 = new NormalDistributionImpl1(mean, std);
            res2 = distribution1.cumulativeProbability(value);
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
        System.out.println("decisionDiff=" + d.mergedHistory);
        System.out.println("cost1=" + cost1);
        System.out.println("cost2=" + cost2);

        System.out.println("Done.");
    }

}
