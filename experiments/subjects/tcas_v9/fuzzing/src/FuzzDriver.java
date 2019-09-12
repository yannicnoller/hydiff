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
import tcas.TCAS_V0;
import tcas.TCAS_V9;

public class FuzzDriver {

    /* INPUT PARSING FOR FUZZING */
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        /* Read one input for both program versions. */
        int cvs;
        boolean hc;
        boolean ttrv;
        int ota;
        int otar;
        int otTa;
        int alv;
        int upS;
        int dS;
        int oRAC;
        int oc;
        boolean ci;

        /* Read input file. */
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            cvs = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Byte.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            hc = bytes[0] >= 0;

            bytes = new byte[Byte.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            ttrv = bytes[0] >= 0;

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            ota = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            otar = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            otTa = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            alv = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), 4); // assumption by program: alv in {0, 1, 2, 3}

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            upS = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            dS = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            oRAC = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Integer.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            oc = ByteBuffer.wrap(bytes).getInt();

            bytes = new byte[Byte.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            ci = bytes[0] >= 0;

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }

        System.out.println("cvs=" + cvs);
        System.out.println("hc=" + hc);
        System.out.println("ttrv=" + ttrv);
        System.out.println("ota=" + ota);
        System.out.println("otar=" + otar);
        System.out.println("otTa=" + otTa);
        System.out.println("alv=" + alv);
        System.out.println("upS=" + upS);
        System.out.println("dS=" + dS);
        System.out.println("oRAC=" + oRAC);
        System.out.println("oc=" + oc);
        System.out.println("ci=" + ci);

        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            TCAS_V0 oldTCAS = new TCAS_V0();
            res1 = oldTCAS.startTcas(cvs, hc, ttrv, ota, otar, otTa, alv, upS, dS, oRAC, oc, ci);
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
            TCAS_V9 newTCAS = new TCAS_V9();
            res2 = newTCAS.startTcas(cvs, hc, ttrv, ota, otar, otTa, alv, upS, dS, oRAC, oc, ci);
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
