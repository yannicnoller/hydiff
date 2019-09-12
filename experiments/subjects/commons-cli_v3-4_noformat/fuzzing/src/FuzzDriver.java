import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.regression.DecisionHistory;
import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;
import edu.cmu.sv.kelinci.regression.OutputSummary;

public class FuzzDriver {

    private static String getLetter(int i) {
        i = Math.floorMod(i, 26);
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

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

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
        
        try (FileInputStream fis = new FileInputStream(args[0])) {

            for (int i = 0; i < numberOfOptions; i++) {

                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                options_list.add(getLetter(bytes[0]));

                char[] char_ar = new char[sizeOfValues];
                for (int j = 0; j < sizeOfValues; j++) {
                    bytes = new byte[Character.BYTES];
                    if ((fis.read(bytes)) == -1) {
                        throw new RuntimeException("Not enough data!");
                    }
                    char_ar[j] = ByteBuffer.wrap(bytes).getChar();
                }
                values_list.add(new String(char_ar));
            }
            
            bytes = new byte[Byte.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            opt = options_list.get(Math.floorMod(bytes[0], numberOfOptions));

            args_test = new String[numberOfOptions * 2];
            for (int j = 0; j < options_list.size(); j++) {
                
                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                if (bytes[0] > 0) {
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
                
                boolean fillProperty = bytes[0] > 0;
                
                bytes = new byte[Character.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                
                char value = ByteBuffer.wrap(bytes).getChar();
                
                if (fillProperty) {
                    properties.setProperty(options_list.get(j), "" + value);
                }
            }
            
            // read stopAtNonOption boolean
            bytes = new byte[Byte.BYTES];
            if ((fis.read(bytes)) == -1) {
                throw new RuntimeException("Not enough data!");
            }
            stopAtNonOption = bytes[0] > 0;
            
            // boolean for arguments per option
            for (int i = 0; i < numberOfOptions; i++) {
                bytes = new byte[Byte.BYTES];
                if ((fis.read(bytes)) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                options_args[i] = bytes[0] > 0;
            }
            
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            throw new RuntimeException("Error reading input");
        }

        System.out.println("arg_test=" + Arrays.toString(args_test));
        System.out.println("opt=" + opt);
        System.out.println("properties=" + properties);
        System.out.println("stopAtNonOption=" + stopAtNonOption);
        System.out.println("options_args=" + Arrays.toString(options_args));

        // #### V1
        Mem.clear();
        DecisionHistory.clear();
        Object res1 = null;
        try {
          v3_org.apache.commons.cli.Options _options3_gnu = new v3_org.apache.commons.cli.Options();
          for (int i = 0; i < numberOfOptions; i++) {
            _options3_gnu.addOption(options_list.get(i), "enable_" + options_list.get(i), options_args[i],
                                    "turn [" + options_list.get(i) + "] on or off");
          }
          v3_org.apache.commons.cli.CommandLineParser _parser3gnu = new v3_org.apache.commons.cli.GnuParser();
          v3_org.apache.commons.cli.CommandLine cl3gnu = _parser3gnu.parse(_options3_gnu, args_test, stopAtNonOption);
          String res1_gnu = cl3gnu.getOptionValue(opt);
          
          
          v3_org.apache.commons.cli.Options _options3_posix = new v3_org.apache.commons.cli.Options();
          for (int i = 0; i < numberOfOptions; i++) {
            _options3_posix.addOption(options_list.get(i), "enable_" + options_list.get(i), options_args[i],
                                      "turn [" + options_list.get(i) + "] on or off");
          }
          v3_org.apache.commons.cli.CommandLineParser _parser3posix = new v3_org.apache.commons.cli.PosixParser();
          v3_org.apache.commons.cli.CommandLine cl3posix = _parser3posix.parse(_options3_posix, args_test, stopAtNonOption);
          String res1_posix = cl3posix.getOptionValue(opt);
          
            res1 = res1_gnu + res1_posix;
        } catch (Throwable e) {
            res1 = e;
            e.printStackTrace();
        }
        boolean[] dec1 = DecisionHistory.getDecisions();
        long cost1 = Mem.instrCost;

        // #### V2
        Mem.clear();
        DecisionHistory.clear();
        Object res2 = null;
        try {
          v4_org.apache.commons.cli.Options _options4_gnu = new v4_org.apache.commons.cli.Options();
          for (int i = 0; i < numberOfOptions; i++) {
            _options4_gnu.addOption(options_list.get(i), "enable_" + options_list.get(i), options_args[i],
                                    "turn [" + options_list.get(i) + "] on or off");
          }
          v4_org.apache.commons.cli.CommandLineParser _parser4gnu = new v4_org.apache.commons.cli.GnuParser();
          v4_org.apache.commons.cli.CommandLine cl4gnu = _parser4gnu.parse(_options4_gnu, args_test, properties, stopAtNonOption);
          String res2_gnu = cl4gnu.getOptionValue(opt);
          
          v4_org.apache.commons.cli.Options _options4_posix = new v4_org.apache.commons.cli.Options();
          for (int i = 0; i < numberOfOptions; i++) {
            _options4_posix.addOption(options_list.get(i), "enable_" + options_list.get(i), options_args[i],
                                      "turn [" + options_list.get(i) + "] on or off");
          }
          v4_org.apache.commons.cli.CommandLineParser _parser4posix = new v4_org.apache.commons.cli.PosixParser();
          v4_org.apache.commons.cli.CommandLine cl4posix = _parser4posix.parse(_options4_posix, args_test, properties, stopAtNonOption);
          String res2_posix = cl4posix.getOptionValue(opt);
          
            res2 = res2_gnu + res2_posix;
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
