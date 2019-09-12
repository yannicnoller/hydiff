package edu.cmu.sv.badger.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.cmu.sv.badger.app.BadgerInput;

/**
 * Utility to write statistic files.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public class Statistics {

    public static void initFiles(BadgerInput input) {
        if (input.printStatistics) {

            File f1 = new File(input.importStatisticsFile);
            f1.delete();
            try {
                f1.createNewFile();
                Files.write(Paths.get(input.importStatisticsFile), ("# time, file, cost \n").getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to initialize: " + input.importStatisticsFile, e);
            }

            File f3 = new File(input.generationStatisticsFile);
            f3.delete();
            try {
                f3.createNewFile();
                Files.write(Paths.get(input.generationStatisticsFile), ("# time, file \n").getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to initialize: " + input.generationStatisticsFile, e);
            }

            File f4 = new File(input.exportStatisticsFile);
            f4.delete();
            try {
                f4.createNewFile();
                Files.write(Paths.get(input.exportStatisticsFile),
                        ("# time, tmpFile, file, branch, highscore \n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to initialize: " + input.exportStatisticsFile, e);
            }

            File f5 = new File(input.trieStatisticsFile);
            f5.delete();
            try {
                f5.createNewFile();
                Files.write(Paths.get(input.trieStatisticsFile),
                        ("# time, numberOfNodes, lengthPrioQueue, instructionMapping, sizeSolutionQueue , alreadyReadInputFiles \n")
                                .getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to initialize: " + input.trieStatisticsFile, e);
            }

        }

        if (input.printPC) {
            File f2 = new File(input.pcMappingFile);
            f2.delete();
            try {
                f2.createNewFile();
                Files.write(Paths.get(input.pcMappingFile), ("# time, file, pc \n").getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to initialize: " + input.pcMappingFile, e);
            }
        }
    }

    public static void appendTrieStatistics(BadgerInput input, String trieStatistics, int pcAndSolutionQueueSize,
            int numberOfAlreadyReadInputFiles, long timeStamp) {
        if (input.printStatistics) {
            String statistics = String.valueOf(timeStamp) + "," + trieStatistics + "," + pcAndSolutionQueueSize + ","
                    + numberOfAlreadyReadInputFiles + "\n";
            try {
                Files.write(Paths.get(input.trieStatisticsFile), (statistics).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to write trie statistics file", e);
            }
        }
    }

    public static void appendImportStatistics(BadgerInput input, String statistics) {
        if (input.printStatistics) {
            try {
                Files.write(Paths.get(input.importStatisticsFile), (statistics).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to write import statistics file", e);
            }
        }
    }

    public static void appendExportStatistics(BadgerInput input, String statistics) {
        if (input.printStatistics) {
            try {
                Files.write(Paths.get(input.exportStatisticsFile), (statistics).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to write export statistics file", e);
            }
        }
    }

    public static void appendGenerationStatistics(BadgerInput input, String generatedFile, long timeStamp) {
        if (input.printStatistics) {
            try {
                Files.write(Paths.get(input.generationStatisticsFile),
                        (timeStamp + "," + generatedFile + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to write generation statistics file", e);
            }
        }
    }

    public static void appendPCMapping(BadgerInput input, String generatedFile, String pcSolution, long timeStamp) {
        if (input.printPC) {
            try {
                Files.write(Paths.get(input.pcMappingFile),
                        (timeStamp + "," + generatedFile + "," + pcSolution + "\n")
                                .getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("[ERROR] Unable to write pc mapping file", e);
            }
        }
    }

}
