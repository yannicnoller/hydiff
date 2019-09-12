import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

import gov.nasa.jpf.symbc.Debug;

public class SymbcDriver {
    
    public static final int MAX_LENGTH = 10; /* test had 3 values */

    public static void main(String[] args) {

        /* Read one input for both program versions. */
        int[] values;
        byte[] types_bytes;
        int n;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            /* Read input file. */
            List<Integer> values_list = new ArrayList<>();
            List<Byte> types_list = new ArrayList<>();
            int counter = 0;
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes;
                while (counter < MAX_LENGTH) {
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
                    
                    counter++;
                }
            }  catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }
            
            int n_concrete = counter;
            n = Debug.addSymbolicInt(counter, "sym_n");
            
            values = new int[n_concrete];
            for (int i=0; i<n_concrete; i++) {
                values[i] = Debug.addSymbolicInt(values_list.get(i), "sym_value_" + i);
            }
            
            types_bytes = new byte[n_concrete];
            for (int i=0; i<n_concrete; i++) {
                types_bytes[i] = Debug.addConstrainedSymbolicByte(types_list.get(i), "sym_type_" + i, 1, 23);
            }
            
        } else {
            int currentN = Debug.getLastObservedInputSizes()[0];
            n = Debug.makeSymbolicInteger("sym_n");
            
            values = new int[currentN];
            types_bytes = new byte[currentN];
            for (int i=0; i<currentN; i++) {
                values[i] = Debug.makeSymbolicInteger("sym_value_" + i);
                types_bytes[i] = Debug.makeConstrainedSymbolicByte("sym_type_" + i, 1, 23);
            }
        }
        
        System.out.println("n=" + n);
        System.out.println("values" + Arrays.toString(values));
        System.out.println("typesB" + Arrays.toString(types_bytes));
        
        driver(n, values, types_bytes);
        
        System.out.println("Done.");
    }
    
    public static void driver(int n, int[] values, byte[] types_bytes) {
        int[] sizes = new int[1];
        switch (n) {
        case 0:
            sizes[0] = 0;
            break;
        case 1:
            sizes[0] = 1;
            break;
        case 2:
            sizes[0] = 2;
            break;
        case 3:
            sizes[0] = 3;
            break;
        case 4:
            sizes[0] = 4;
            break;
        case 5:
            sizes[0] = 5;
            break;
        case 6:
            sizes[0] = 6;
            break;
        case 7:
            sizes[0] = 7;
            break;
        case 8:
            sizes[0] = 8;
            break;
        case 9:
            sizes[0] = 9;
            break;
        case 10:
            sizes[0] = 10;
            break;
        default:
            throw new RuntimeException("unintended input size");
        }
        Debug.setLastObservedInputSizes(sizes);
        
        DateTimeFieldType[] types = new DateTimeFieldType[types_bytes.length];
        for (int i=0; i<types.length; i++) {
            switch(types_bytes[i]) {
            case 0:
                throw new RuntimeException("invalid index..");
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
            default:
                throw new RuntimeException("invalid index..");
            }
        }
        
        new Partial(types, values);
    }
}
