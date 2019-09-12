import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import gov.nasa.jpf.symbc.Debug;
import tcas.TCAS_V7;

public class SymbcDriver {

    public static void main(String[] args) {

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

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read input file. */
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                cvs = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_cvs");

                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                hc = Debug.addSymbolicBoolean(bytes[0] > 0, "sym_hc");

                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                ttrv = Debug.addSymbolicBoolean(bytes[0] > 0, "sym_ttrv");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                ota = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_ota");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                otar = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_otar");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                otTa = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_otTa");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                // assumption by program: alv in {0, 1, 2, 3}
                int tmp = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), 4);
                alv = Debug.addConstrainedSymbolicInt(tmp, "sym_alv", 0, 3);

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                upS = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_upS");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                dS = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_dS");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                oRAC = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_oRAC");

                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                oc = Debug.addSymbolicInt(ByteBuffer.wrap(bytes).getInt(), "sym_oc");

                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                ci = Debug.addSymbolicBoolean(bytes[0] > 0, "sym_ci");

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }

        } else {
            cvs = Debug.makeSymbolicInteger("sym_cvs");
            hc = Debug.makeSymbolicBoolean("sym_hc");
            ttrv = Debug.makeSymbolicBoolean("sym_ttrv");
            ota = Debug.makeSymbolicInteger("sym_ota");
            otar = Debug.makeSymbolicInteger("sym_otar");
            otTa = Debug.makeSymbolicInteger("sym_otTa");

            // alv = Debug.makeSymbolicInteger("sym_alv");
            alv = Debug.makeConstrainedSymbolicInteger("sym_alv", 0, 3);
            // Debug.assume(alv >= 0);
            // Debug.assume(alv < 4);
            // TOOD YN: make sure that this is correct

            upS = Debug.makeSymbolicInteger("sym_upS");
            dS = Debug.makeSymbolicInteger("sym_dS");
            oRAC = Debug.makeSymbolicInteger("sym_oRAC");
            oc = Debug.makeSymbolicInteger("sym_oc");
            ci = Debug.makeSymbolicBoolean("sym_ci");
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

        TCAS_V7 newTCAS = new TCAS_V7();
        newTCAS.startTcas(cvs, hc, ttrv, ota, otar, otTa, alv, upS, dS, oRAC, oc, ci);

        // System.out.println("Done.");
    }
}
