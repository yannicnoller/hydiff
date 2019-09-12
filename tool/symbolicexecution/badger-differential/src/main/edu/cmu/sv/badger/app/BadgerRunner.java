package edu.cmu.sv.badger.app;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import edu.cmu.sv.badger.analysis.CFGAnalyzer;
import edu.cmu.sv.badger.util.Statistics;
import gov.nasa.jpf.symbc.Observations;

/**
 * Main class to start Badger execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public class BadgerRunner {

    public static void main(String[] args) {

        String configFilePath = args[0];
        BadgerInput input = BadgerInput.loadFromConfigFile(configFilePath);

        // TODO YN just a hack to make experiments
        if (args.length > 1) {
            String runId = args[1];
            if (input.syncInputdir.isPresent()) {
                input.syncInputdir = Optional
                        .of(input.syncInputdir.get().replace("@@", runId));
            }
            input.tmpDir = input.tmpDir.replace("@@", runId);
            input.exportDir = input.exportDir.replace("@@", runId);
            input.importStatisticsFile = input.importStatisticsFile.replace("@@", runId);
            input.generationStatisticsFile = input.generationStatisticsFile.replace("@@", runId);
            input.exportStatisticsFile = input.exportStatisticsFile.replace("@@", runId);
            input.trieStatisticsFile = input.trieStatisticsFile.replace("@@", runId);
            input.pcMappingFile = input.pcMappingFile.replace("@@", runId);
        }

        ensureAndCleanOutputDirectoriesExist(input);
        Statistics.initFiles(input);

        /* For shadow symbolic execution we want to load or deserialize the cfg information. */
        if (input.symShadow.isPresent() && input.symShadow.get().equals("true")) {
            boolean useCFG = Boolean.valueOf(input.symShadowCFG.orElse("false"));
            if (useCFG) {
                if (input.symShadowCFGDir.isPresent()) {
                    String pathToClasses = input.symShadowCFGDir.get();
                    String pathToChangeAnnotationClass = input.symShadowCFGChangeAnnotationClass.get();
                    String classesToSkip = input.symShadowCFSkip.orElse("");
                    String exportDir = input.symShadowCFGExportDir.orElse("");
                    CFGAnalyzer.buildCFG(pathToClasses, pathToChangeAnnotationClass, classesToSkip, exportDir);
                } else {
                    String serializedCFGFolderPath = input.symShadowCFGImportDir.get();
                    CFGAnalyzer.importCFG(serializedCFGFolderPath);
                }
                System.out.println("Loaded CFG. Let's go!");
            }
        }
        
        /* YN: Read internal data for DNN once before symbolic execution runs. */
        if (input.dnnDataDir.isPresent()) {
            System.out.println("Read internal data for DNN..");
            Observations.loadInternalData(input.dnnDataDir.get());
            System.out.println("Done. Okay let's start!");
        }
        
        if (input.dataDir.isPresent()) {
            Observations.setDataDir(input.dataDir.get());
        }

        SymExe symExe = new SymExe(input);
        symExe.run();

    }

    private static void ensureAndCleanOutputDirectoriesExist(BadgerInput input) {

        File tmpDir = new File(input.tmpDir);
        try {
            FileUtils.forceMkdir(tmpDir);
            FileUtils.cleanDirectory(tmpDir);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Unable to create tmp directory: " + input.tmpDir, e);
        }

        File exportDir = new File(input.exportDir);
        try {
            FileUtils.forceMkdir(exportDir);
            FileUtils.cleanDirectory(exportDir);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Unable to create tmp directory: " + input.exportDir, e);
        }
    }

}
