package gov.nasa.jpf.symbc;

import java.util.HashSet;
import java.util.Set;

public class ChangeAnnotation {

    public static Set<String> signatureOfChangeMethods;
    static {
        signatureOfChangeMethods = new HashSet<>();
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(II)I:22");
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(FF)F:26");
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(DD)D:30");
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(ZZ)Z:34");
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(JJ)J:38");
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(CC)C:42");
        // signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.change(Ljava.lang.Object;Ljava.lang.Object;)Ljava.lang.Object;:46");
        signatureOfChangeMethods.add("gov.nasa.jpf.symbc.ChangeAnnotation.execute(Z)Z:53");
    }

    public static int change(int oldVal, int newVal) {
        return oldVal;
    }

    public static float change(float oldVal, float newVal) {
        return oldVal;
    }

    public static double change(double oldVal, double newVal) {
        return oldVal;
    }

    public static boolean change(boolean oldVal, boolean newVal) {
        return oldVal;
    }

    public static long change(long oldVal, long newVal) {
        return oldVal;
    }

    public static char change(char oldVal, char newVal) {
        return oldVal;
    }

    // public static Object change(Object oldVal, Object newVal) {
    // return oldVal;
    // }

    public static final boolean OLD = true;
    public static final boolean NEW = false;

    public static boolean execute(boolean executionMode) {
        return executionMode;
    };

}
