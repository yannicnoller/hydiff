import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class FuzzDiffDriver {

    static InternalData internalData;
    static {
        /* Later: Will be loaded only once at Kelinci Fuzzing Server. */
        internalData = InternalData.createFromDataFiles("./data");
    }

    static final int IMG_HEIGHT = 28; /* 28 */
    static final int IMG_WIDTH = 28; /* 28 */

    static final int NUMBER_OF_PIXEL_CHANGE = 15; // 2%
    // static final int NUMBER_OF_PIXEL_CHANGE = 784; // 100%

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        // Reading input from fuzzed file.
        double[][][] a1 = new double[28][28][1];
        double[][][] a2 = new double[28][28][1];
        try (FileInputStream fis = new FileInputStream(args[0])) {
            /* Read pixel values from [0, 255] and normalize them to [0, 1] */
            byte[] bytes = new byte[1];

            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                    for (int k = 0; k < 1; k++) {

                        if (fis.read(bytes) == -1) {
                            throw new RuntimeException("Not enough data to read input!");
                        }

                        /* Normalize value from [-128,127] to be in range [0, 1] */
                        a1[i][j][k] = (bytes[0] + 128) / 255.0;
                        a2[i][j][k] = a1[i][j][k];
                    }
                }
            }

            // Introduce change for second input.
            /* In total 784 pixels, let say change 1% of that, i.e. 7 pixel values. */
            for (int i = 0; i < NUMBER_OF_PIXEL_CHANGE; i++) {
                if (fis.read(bytes) == -1) {
                    throw new RuntimeException("Not enough data to read input!");
                }
                int i_pos = Math.floorMod(bytes[0], 28);
                if (fis.read(bytes) == -1) {
                    throw new RuntimeException("Not enough data to read input!");
                }
                int j_pos = Math.floorMod(bytes[0], 28);
                if (fis.read(bytes) == -1) {
                    throw new RuntimeException("Not enough data to read input!");
                }
                a2[i_pos][j_pos][0] = (bytes[0] + 128) / 255.0;

                System.out.println("a1[" + i_pos + "][" + j_pos + "][0] = " + a1[i_pos][j_pos][0]);
                System.out.println("a2[" + i_pos + "][" + j_pos + "][0] = " + a2[i_pos][j_pos][0]);
                System.out.println();
            }

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        System.out.println();
        DNNt dnn = new DNNt(internalData);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = dnn.run(a1);
        } catch (Throwable e) {
            res1 = e;
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;

        Mem.clear();
        DecisionHistory.clear();
        Object res2 = null;
        try {
            res2 = dnn.run(a2);
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

        // int index = args[0].indexOf("-is-");
        // int should = Integer.valueOf(args[0].substring(index + 4, index + 5));
        //
        // if (res != should) {
        // System.out.println(args[0]);
        // System.out.println("should=" + should);
        // System.out.println("result=" + res);
        // System.out.println();
        // }

        System.out.println("Done.");
    }
}
