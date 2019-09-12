import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import gov.nasa.jpf.symbc.Debug;

public class SymbcDriver {

    private static String getLetter(int i) {
        i = floorMod(i, 26);
        switch (i) {
        case 0:
            return "a";
        case 1:
            return "b";
        case 2:
            return "c";
        case 3:
            return "d";
        case 4:
            return "e";
        case 5:
            return "f";
        case 6:
            return "g";
        case 7:
            return "h";
        case 8:
            return "i";
        case 9:
            return "j";
        case 10:
            return "k";
        case 11:
            return "l";
        case 12:
            return "m";
        case 13:
            return "n";
        case 14:
            return "o";
        case 15:
            return "p";
        case 16:
            return "q";
        case 17:
            return "r";
        case 18:
            return "s";
        case 19:
            return "t";
        case 20:
            return "u";
        case 21:
            return "v";
        case 22:
            return "w";
        case 23:
            return "x";
        case 24:
            return "y";
        case 25:
            return "z";
        default:
            throw new RuntimeException("unknown character value");
        }
    }
    
    public static void main(String[] args) {

        int numberOfOptions = 3;
        int sizeOfValues = 3; // number of chars

        byte[] bytes;
        List<String> options_list = new ArrayList<>();
        List<String> values_list = new ArrayList<>();
        Properties properties = new Properties();
        boolean stopAtNonOption;
        boolean[] options_args = new boolean[numberOfOptions];
        
        String[] args_test;
        String opt;
        
        if (args.length == 1) {
            
            String fileName = args[0].replace("#", ",");
            try (FileInputStream fis = new FileInputStream(fileName)) {

                for (int i = 0; i < numberOfOptions; i++) {

                    bytes = new byte[Byte.BYTES];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough data!");
                    }
                    byte letter_value = Debug.addSymbolicByte(bytes[0], "sym_opt_" + i);
                    String s = getLetter(letter_value);
                    options_list.add(s);

                    char[] char_ar = new char[sizeOfValues];
                    for (int j = 0; j < sizeOfValues; j++) {
                        bytes = new byte[Character.BYTES];
                        if ((fis.read(bytes)) == -1) {
                            throw new RuntimeException("Not enough data!");
                        }
                        char_ar[j] = Debug.addSymbolicChar(ByteBuffer.wrap(bytes).getChar(), "sym_val_" + i + "_" + j);
                    }
                    values_list.add(new String(char_ar));
                    
                }
                
                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                byte opt_idx = Debug.addSymbolicByte(bytes[0], "sym_optidx");
                opt = options_list.get(floorMod(opt_idx, numberOfOptions));

                args_test = new String[numberOfOptions * 2];
                for (int j = 0; j < options_list.size(); j++) {
                    
                    bytes = new byte[Byte.BYTES];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough data!");
                    }
                    byte dec = Debug.addSymbolicByte(bytes[0], "sym_dec_" + j);
                    if (dec > 0) {
                        args_test[2 * j] = "-" + options_list.get(j);
                    } else {
                        args_test[2 * j] = "--" + "enable_" + options_list.get(j);
                    }
                    args_test[2 * j + 1] = values_list.get(j);
                    
                }
                
                // fill properties
                for (int j = 0; j < numberOfOptions; j++) {
                    bytes = new byte[Byte.BYTES];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough data!");
                    }
                    
                    boolean fillProperty = Debug.addSymbolicBoolean(bytes[0] > 0, "sym_prop_" + j);
                    
                    bytes = new byte[Character.BYTES];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough data!");
                    }
                    
                    char value = Debug.addSymbolicChar(ByteBuffer.wrap(bytes).getChar(), "sym_prop_val_" + j);
                    
                    if (fillProperty) {
                        properties.setProperty(options_list.get(j), "" + value);
                    }
                }
                
                // read stopAtNonOption boolean
                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                stopAtNonOption = Debug.addSymbolicBoolean(bytes[0] > 0, "sym_stopAtNonOptions");
                
                // boolean for arguments per option
                for (int i = 0; i < numberOfOptions; i++) {
                    bytes = new byte[Byte.BYTES];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough data!");
                    }
                    options_args[i] = Debug.addSymbolicBoolean(bytes[0] > 0, "sym_hasArg_" + i);
                }
                
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                throw new RuntimeException("Error reading input");
            }
            
        } else {
            
            for (int i = 0; i < numberOfOptions; i++) {
                byte letter_value = Debug.makeSymbolicByte("sym_opt_" + i);
                options_list.add(getLetter(letter_value));

                char[] char_ar = new char[sizeOfValues];
                for (int j = 0; j < sizeOfValues; j++) {
                    char_ar[j] = Debug.makeSymbolicChar("sym_val_" + i);
                }
                values_list.add(new String(char_ar));
            }
            
            byte opt_idx = Debug.makeSymbolicByte("sym_optidx");
            opt = options_list.get(floorMod(opt_idx, numberOfOptions));

            args_test = new String[numberOfOptions * 2];
            for (int j = 0; j < options_list.size(); j++) {
                
                byte dec = Debug.makeSymbolicByte("sym_dec_" + j);
                if (dec > 0) {
                    args_test[2 * j] = "-" + options_list.get(j);
                } else {
                    args_test[2 * j] = "--" + "enable_" + options_list.get(j);
                }
                args_test[2 * j + 1] = values_list.get(j);
            }
            
            for (int j = 0; j < numberOfOptions; j++) {
                boolean fillProperty = Debug.makeSymbolicBoolean("sym_prop_" + j);
                char value = Debug.makeSymbolicChar("sym_prop_val_" + j);
                if (fillProperty) {
                    properties.setProperty(options_list.get(j), "" + value);
                }
            }
            
            stopAtNonOption = Debug.makeSymbolicBoolean("sym_stopAtNonOptions");
            
            for (int i = 0; i < numberOfOptions; i++) {
                options_args[i] = Debug.makeSymbolicBoolean("sym_hasArg_" + i);
            }
            
        }

        System.out.println("arg_test=" + Arrays.toString(args_test));
        System.out.println("opt=" + opt);
        System.out.println("properties=" + properties);
        System.out.println("stopAtNonOption=" + stopAtNonOption);
        System.out.println("options_args=" + Arrays.toString(options_args));

        try {
          v4_org.apache.commons.cli.Options _options4_gnu = new v4_org.apache.commons.cli.Options();
          for (int i = 0; i < numberOfOptions; i++) {
            _options4_gnu.addOption(options_list.get(i), "enable_" + options_list.get(i), options_args[i],
                                    "turn [" + options_list.get(i) + "] on or off");
          }
          v4_org.apache.commons.cli.CommandLineParser _parser4gnu = new v4_org.apache.commons.cli.GnuParser();
          v4_org.apache.commons.cli.CommandLine cl4gnu = _parser4gnu.parse(_options4_gnu, args_test, properties, stopAtNonOption);
          cl4gnu.getOptionValue(opt);
          
          v4_org.apache.commons.cli.Options _options4_posix = new v4_org.apache.commons.cli.Options();
          for (int i = 0; i < numberOfOptions; i++) {
            _options4_posix.addOption(options_list.get(i), "enable_" + options_list.get(i), options_args[i],
                                      "turn [" + options_list.get(i) + "] on or off");
          }
          v4_org.apache.commons.cli.CommandLineParser _parser4posix = new v4_org.apache.commons.cli.PosixParser();
          v4_org.apache.commons.cli.CommandLine cl4posix = _parser4posix.parse(_options4_posix, args_test, properties, stopAtNonOption);
          cl4posix.getOptionValue(opt);
            
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done.");
    }
    
    public static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }
    
    public static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }
    
}
