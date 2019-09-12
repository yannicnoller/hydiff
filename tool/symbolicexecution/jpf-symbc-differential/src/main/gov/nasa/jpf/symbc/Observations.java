package gov.nasa.jpf.symbc;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.util.InternalData;

public class Observations {

    /** Used for user-defined cost. */
    public static double lastObservedUserdefinedCostOldVersion = 0.0;
    public static double lastObservedUserdefinedCostNewVersion = 0.0;

    /** Used for maximization of user-defined cost. */
    public static Expression lastObservedSymbolicExpressionOldVersion = null;
    public static Expression lastObservedSymbolicExpressionNewVersion = null;

    /** Used to set current input size in side-channel analysis in order to generate correct input file. */
    public static int[] lastObservedInputSizes = new int[0];

    /** Used by MetricListener to propagate last measured metric value. */
    public static double lastMeasuredMetricValueOldVersion = 0.0;
    public static double lastMeasuredMetricValueNewVersion = 0.0;

    public static void reset() {
        lastObservedUserdefinedCostOldVersion = 0.0;
        lastObservedUserdefinedCostNewVersion = 0.0;
        lastObservedSymbolicExpressionOldVersion = null;
        lastObservedSymbolicExpressionNewVersion = null;
        lastObservedInputSizes = new int[0];
        lastMeasuredMetricValueOldVersion = 0.0;
        lastMeasuredMetricValueNewVersion = 0.0;
    }

    /* YN: read and store the internal data of the DNN. */
    public static InternalData internal = null;

    public static void loadInternalData(String path) {
        Observations.internal = InternalData.createFromDataFiles(path);
    }

    public static String dataDir = "";

    public static void setDataDir(String dir) {
        dataDir = dir;
    }

    public static String getDataDir() {
        return dataDir;
    }
}
