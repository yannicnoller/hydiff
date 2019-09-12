import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial0;
import org.joda.time.Partial1;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.CFGSummary;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class FuzzDriver {

    /* v1 throws no datetimefield when year after era. The condition to check that was removed. The fixed version adds more checks again. */
    public static final int MAX_LENGTH = 10; /* test had 3 values */
    
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }
        
        int[] values;
        DateTimeFieldType[] types;
        
        /* Read input file. */
        List<Integer> values_list = new ArrayList<>();
        List<Byte> types_list = new ArrayList<>();
        int n = 0;
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] bytes;
            while (n < MAX_LENGTH) {
                // read value
                bytes = new byte[Integer.BYTES];
                if ((fis.read(bytes)) == -1) {
                    break;
                }
                int value = ByteBuffer.wrap(bytes).getInt();
                
                // read type
                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    break;
                }
                byte type = (byte) (Math.floorMod(bytes[0], 23) + 1);
                
                values_list.add(value);
                types_list.add(type);
                
                n++;
            }
        }  catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }
        
        values = new int[n];
        for (int i=0; i<n; i++) {
            values[i] = values_list.get(i);
        }
        
        types = new DateTimeFieldType[n];
        for (int i=0; i<n; i++) {
            switch(types_list.get(i)) {
            case DateTimeFieldType.ERA:
                types[i] = DateTimeFieldType.ERA_TYPE;
                break;
            case DateTimeFieldType.YEAR_OF_ERA:
                types[i] = DateTimeFieldType.YEAR_OF_ERA_TYPE;
                break;
            case DateTimeFieldType.CENTURY_OF_ERA:
                types[i] = DateTimeFieldType.CENTURY_OF_ERA_TYPE;
                break;
            case DateTimeFieldType.YEAR_OF_CENTURY:
                types[i] = DateTimeFieldType.YEAR_OF_CENTURY_TYPE;
                break;
            case DateTimeFieldType.YEAR:
                types[i] = DateTimeFieldType.YEAR_TYPE;
                break;
            case DateTimeFieldType.DAY_OF_YEAR:
                types[i] = DateTimeFieldType.DAY_OF_YEAR_TYPE;
                break;
            case DateTimeFieldType.MONTH_OF_YEAR:
                types[i] = DateTimeFieldType.MONTH_OF_YEAR_TYPE;
                break;
            case DateTimeFieldType.DAY_OF_MONTH:
                types[i] = DateTimeFieldType.DAY_OF_MONTH_TYPE;
                break;
            case DateTimeFieldType.WEEKYEAR_OF_CENTURY:
                types[i] = DateTimeFieldType.WEEKYEAR_OF_CENTURY_TYPE;
                break;
            case DateTimeFieldType.WEEKYEAR:
                types[i] = DateTimeFieldType.WEEKYEAR_TYPE;
                break;
            case DateTimeFieldType.WEEK_OF_WEEKYEAR:
                types[i] = DateTimeFieldType.WEEK_OF_WEEKYEAR_TYPE;
                break;
            case DateTimeFieldType.DAY_OF_WEEK:
                types[i] = DateTimeFieldType.DAY_OF_WEEK_TYPE;
                break;
            case DateTimeFieldType.HALFDAY_OF_DAY:
                types[i] = DateTimeFieldType.HALFDAY_OF_DAY_TYPE;
                break;
            case DateTimeFieldType.HOUR_OF_HALFDAY:
                types[i] = DateTimeFieldType.HOUR_OF_HALFDAY_TYPE;
                break;
            case DateTimeFieldType.CLOCKHOUR_OF_HALFDAY:
                types[i] = DateTimeFieldType.CLOCKHOUR_OF_HALFDAY_TYPE;
                break;
            case DateTimeFieldType.CLOCKHOUR_OF_DAY:
                types[i] = DateTimeFieldType.CLOCKHOUR_OF_DAY_TYPE;
                break;
            case DateTimeFieldType.HOUR_OF_DAY:
                types[i] = DateTimeFieldType.HOUR_OF_DAY_TYPE;
                break;
            case DateTimeFieldType.MINUTE_OF_DAY:
                types[i] = DateTimeFieldType.MINUTE_OF_DAY_TYPE;
                break;
            case DateTimeFieldType.MINUTE_OF_HOUR:
                types[i] = DateTimeFieldType.MINUTE_OF_HOUR_TYPE;
                break;
            case DateTimeFieldType.SECOND_OF_DAY:
                types[i] = DateTimeFieldType.SECOND_OF_DAY_TYPE;
                break;
            case DateTimeFieldType.SECOND_OF_MINUTE:
                types[i] = DateTimeFieldType.SECOND_OF_MINUTE_TYPE;
                break;
            case DateTimeFieldType.MILLIS_OF_DAY:
                types[i] = DateTimeFieldType.MILLIS_OF_DAY_TYPE;
                break;
            case DateTimeFieldType.MILLIS_OF_SECOND:
                types[i] = DateTimeFieldType.MILLIS_OF_SECOND_TYPE;
                break;
            }
        }
        
        System.out.println("values=" + Arrays.toString(values));
        System.out.println("typesB=" + Arrays.toString(types_list.toArray()));
        System.out.println("types=" + Arrays.toString(types));
        
        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
            res1 = new Partial0(types, values);
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
            res2 = new Partial1(types, values);
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
