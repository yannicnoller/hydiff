import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.CFGSummary;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;
import test.org.apache.commons.math.complex.Complex0;
import test.org.apache.commons.math.complex.Complex1;

public class FuzzDriver {

    /* v1 has different behavior when dividing by zero, it returns infinity, and v0 returns NaN. */
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        double x_real;
        double x_imag;
        double y_real;
        double y_imag;

        /* Read input file. */
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Double.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            x_real = Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

            bytes = new byte[Double.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            x_imag = Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

            bytes = new byte[Double.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            y_real = Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

            bytes = new byte[Double.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            y_imag = Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }

        System.out.println("x_real=" + x_real);
        System.out.println("x_imag=" + x_imag);
        System.out.println("y_real=" + y_real);
        System.out.println("y_imag=" + y_imag);

        Complex0 x0 = new Complex0(x_real, x_imag);
        Complex0 y0 = new Complex0(y_real, y_imag);

        Complex1 x1 = new Complex1(x_real, x_imag);
        Complex1 y1 = new Complex1(y_real, y_imag);

        /* Init */
        x0.divide(y0);
        x1.divide(y1);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = x0.divide(y0);
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
            res2 = x1.divide(y1);
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
