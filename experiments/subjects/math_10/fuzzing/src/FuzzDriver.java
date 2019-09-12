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
import test.org.apache.commons.math3.analysis.differentiation.DerivativeStructure_v0;
import test.org.apache.commons.math3.analysis.differentiation.DerivativeStructure_v1;

public class FuzzDriver {

    /* v1 has problem when both x and y value == 0 -> NaN */

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        int x_variables;/* = 2; */
        int x_order;/* = 2; */
        int x_index;/* = 1; */
        double x_value;/* = +0.0; */

        int y_variables;/* = 2; */
        int y_order;/* = 2; */
        int y_index;/* = 1; */
        double y_value;/* = +0.0; */

        /* Read input file. */
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            // x_variables = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            // x_order = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            x_index = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Double.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            x_value = Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            // y_variables = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            // y_order = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            y_index = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Double.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            y_value = Double.longBitsToDouble(ByteBuffer.wrap(bytes).getLong());

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }

        x_variables = 2;
        x_order = 2;
        // x_index = 1;
        y_variables = 2;
        y_order = 2;
        // y_index = 1;

        System.out.println("x_variables=" + x_variables);
        System.out.println("x_order=" + x_order);
        System.out.println("x_index=" + x_index);
        System.out.println("x_value=" + x_value);
        System.out.println("y_variables=" + y_variables);
        System.out.println("y_order=" + y_order);
        System.out.println("y_index=" + y_index);
        System.out.println("y_value=" + y_value);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = DerivativeStructure_v0.atan2(new DerivativeStructure_v0(x_variables, x_order, x_index, x_value),
                    new DerivativeStructure_v0(y_variables, y_order, y_index, y_value)).getValue();
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
            res2 = DerivativeStructure_v1.atan2(new DerivativeStructure_v1(x_variables, x_order, x_index, x_value),
                    new DerivativeStructure_v1(y_variables, y_order, y_index, y_value)).getValue();
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
