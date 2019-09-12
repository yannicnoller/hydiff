package edu.cmu.sv.badger.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Triple;

import edu.cmu.sv.badger.analysis.StateBuilder;
import edu.cmu.sv.badger.listener.ConcreteInput2TrieListener;
import edu.cmu.sv.badger.listener.MetricListener;
import edu.cmu.sv.badger.listener.SymCreteCostListener;
import edu.cmu.sv.badger.listener.TrieGuidanceListener;
import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;
import edu.cmu.sv.badger.util.Statistics;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPF.ExitException;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.ShadowListener;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.Pair;

/**
 * Implements the SymExe compartment of Badger.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public class SymExe {

    private BadgerInput input;
    private Trie trie;
    private BlockingQueue<Pair<PathCondition, Map<String, Object>>> pcAndSolutionQueue;
    public static AtomicInteger lastId = new AtomicInteger(-1);
    private static AtomicInteger lastTempFileId = new AtomicInteger(-1);
    List<String> alreadyReadInputFiles = new ArrayList<>();
    private long startTime;

    public static enum ConcreteSPFMode {
        IMPORT, EXPORT;
    }

    public SymExe(BadgerInput input) {
        this.input = input;
        this.trie = new Trie(input.trieAnalysisMethod);
        lastId.set(input.initialId);
        this.pcAndSolutionQueue = new ArrayBlockingQueue<>(1000);
    }

    public void run() {

        startTime = (System.currentTimeMillis() / 1000L);

        if (input.secUntilFirstCycle > 0) {
            try {
                Thread.sleep(input.secUntilFirstCycle * 1000);
            } catch (InterruptedException e) {
                return;
            }
        }

        boolean firstStep = true;
        while (true) {

            // Read input.
            List<String> newInputfiles;
            if (firstStep) {
                firstStep = false;
                newInputfiles = analyzeInputFiles(input.initialInputDir);
            } else {
                newInputfiles = analyzeInputFiles(
                        input.syncInputdir.isPresent() ? input.syncInputdir.get() : input.initialInputDir);
            }

            // Run one step.
            boolean needsABreak = runStep(newInputfiles);

            // Only make a break if there was at least one file exported.
            if (needsABreak) {
                try {
                    Thread.sleep(input.cycleWaitingSec * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }
    }

    /**
     * @return Returns True if we need a small break.
     */
    private boolean runStep(List<String> newInputFiles) {

        if (newInputFiles.isEmpty() && !input.trieAnalysisMethod.isNodeLeftforAnalysis()) {
            // If there is no new input (from fuzzer), and we assume that our last run was complete, then
            // here is no need to further analyze or process the trie, because there is no path left to find.
            System.out.println("[SPF] nothing to process, wait for " + input.cycleWaitingSec + " sec ...");
            return true; // there is currently no sense in running further, waiting for fuzzer makes more sense.
        }

        Map<String, String> processedNewInputs = input.ioUtils.processInput(newInputFiles);

        // Extend trie for new input.
        if (!processedNewInputs.isEmpty()) {
            buildTrieFromProcessedInput(processedNewInputs, ConcreteSPFMode.IMPORT);
            Statistics.appendTrieStatistics(input, trie.getStatistics(), pcAndSolutionQueue.size(),
                    alreadyReadInputFiles.size(), (System.currentTimeMillis() / 1000L - startTime));
        }

        /*
         * Explore new nodes according to the settings. The reason for making a loop here is that we only can select one
         * execution path as guidance, because the choice generator does not support a multi-selection.
         */
        for (int i = 0; i < input.maximumNumberOfSymExeIterations; i++) {

            // Analyze trie: pick next node and mark path in trie for efficient replay.
            TrieNode identifiedNode = input.trieAnalysisMethod.analyze(trie);

            if (input.printTrieAsDot) {
                Trie.storeTrieAsDot(trie, "trie-analyzed.dot", input.printTrieMaxDepth.orElse(null));
            }

            // Break the loop if no new node was identified.
            if (identifiedNode == null) {
                break;
            }

            // Replay trie for enabled nodes and extract path conditions for new explored nodes.
            runJPFReplayAndBSE(trie, input.numberOfAdditionalDecisions, identifiedNode.getInputSizes());

            Statistics.appendTrieStatistics(input, trie.getStatistics(), pcAndSolutionQueue.size(),
                    alreadyReadInputFiles.size(), (System.currentTimeMillis() / 1000L - startTime));

            if (input.printTrieAsDot) {
                Trie.storeTrieAsDot(trie, "trie-explored.dot", input.printTrieMaxDepth.orElse(null));
            }

            // Generate input.
            List<String> generatedTmpFiles = generateTmpInputFiles();

            // Read new input files, updated trie, and extract relevant inputs for fuzzer.
            Map<String, String> processedGeneratedTmpFiles = input.ioUtils.processInput(generatedTmpFiles);
            if (!processedGeneratedTmpFiles.isEmpty()) {
                buildTrieFromProcessedInput(processedGeneratedTmpFiles, ConcreteSPFMode.EXPORT);
            }

            if (input.printTrieAsDot) {
                Trie.storeTrieAsDot(trie, "trie-extended.dot", input.printTrieMaxDepth.orElse(null));
            }

            Statistics.appendTrieStatistics(input, trie.getStatistics(), pcAndSolutionQueue.size(),
                    alreadyReadInputFiles.size(), (System.currentTimeMillis() / 1000L - startTime));
        }

        return false;
    }

    private List<String> generateTmpInputFiles() {
        List<String> generatedTmpFiles = new ArrayList<>();
        while (!pcAndSolutionQueue.isEmpty()) {
            try {

                Pair<PathCondition, Map<String, Object>> pcAndSolution = pcAndSolutionQueue.take();

                String outputfile = generateTmpInputfile(pcAndSolution);
                generatedTmpFiles.add(outputfile);

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return generatedTmpFiles;
    }

    private String generateTmpInputfile(Pair<PathCondition, Map<String, Object>> pcAndSolution) {
        String outputfile = input.tmpDir + "/" + String.valueOf(lastTempFileId.incrementAndGet());
        input.ioUtils.generateInputFiles(pcAndSolution._1, pcAndSolution._2, outputfile);

        Statistics.appendGenerationStatistics(input, outputfile, (System.currentTimeMillis() / 1000L - startTime));
        Statistics.appendPCMapping(input, outputfile, pcAndSolution._1.toString(),
                (System.currentTimeMillis() / 1000L - startTime));

        return (outputfile);
    }

    /**
     * Return files in inputDir that were not already read into trie.
     * 
     * @param inputDir
     * @return list of file names that need to be read.
     */
    private List<String> analyzeInputFiles(String inputDir) {
        File aflQueueFolder = new File(inputDir);

        List<String> newInputFiles = new ArrayList<>();

        File[] inputFiles = aflQueueFolder.listFiles();
        if (inputFiles == null) {
            System.out.println("[WARNING] import dir does not exist: " + inputDir);
            return newInputFiles;
        }

        for (File inputFile : inputFiles) {
            if (!inputFile.isHidden()) {
                String fileName = inputFile.getAbsolutePath();
                if (alreadyReadInputFiles.contains(fileName)) {
                    continue;
                }
                if (fileName.contains("sync:spf")) {
                    // Don't import own exports!
                    continue;
                }
                alreadyReadInputFiles.add(fileName);
                newInputFiles.add(fileName);
            }
        }

        return newInputFiles;
    }

    private Triple<Double, Double, Boolean> runJPFSymCrete(String targetArgument, String originalFileName, Trie trie,
            ConcreteSPFMode spfMode) {

        if (targetArgument == null) {
            return null;
        }

        System.out.println("Run JPF with argument: " + targetArgument);

        try {
            Config conf = initSPFConfig();

            /*
             * Collect constraints = true, i.e. choice generators will only have ONE choice, exactly as the concrete
             * input determines
             */
            conf.setProperty("symbolic.collect_constraints", "true");

            /* Disable solving because we only follow one path, which is determined by the concrete input. */
            conf.setProperty("symbolic.dp", "no_solver"); // symcrete execution, no solver

            conf.setProperty("target.args", input.jpf_argument.replace("@@", targetArgument));

            JPF jpf = new JPF(conf);

            // SymbolicListener symbolicListener = new SymbolicListener(conf, jpf); // TODO YN: have or have it not?
            // jpf.addListener(symbolicListener);

            if (input.symShadow.isPresent() && input.symShadow.get().equals("true")) {
                ShadowListener shadowListener = new ShadowListener(conf, jpf);
                jpf.addListener(shadowListener);
            }

            StateBuilder stateBuilder = null;
            if (input.stateBuilderFactory.isPresent()) {
                stateBuilder = input.stateBuilderFactory.get().createStateBuilder();
                MetricListener metricListener = new MetricListener(conf, jpf, stateBuilder);
                jpf.addListener(metricListener);
            }

            // reset last observed cost before each execution.
            Observations.reset();

            ConcreteInput2TrieListener trieListener = new ConcreteInput2TrieListener(conf, jpf, trie, stateBuilder,
                    originalFileName, input.useUserDefinedCost);
            jpf.addListener(trieListener);

            jpf.run();

            if (jpf.foundErrors()) {
                System.out.println("#FOUND ERRORS = " + jpf.getSearchErrors().size());
            }

            if (spfMode.equals(ConcreteSPFMode.EXPORT)) {

                if (input.explorationHeuristic.didObserveNewBehavior(trieListener)) {
                    String outputfile = input.exportDir + "/id:" + String.format("%06d", lastId.incrementAndGet());

                    File tmpFile = new File(originalFileName);
                    File newFile = new File(outputfile);
                    tmpFile.renameTo(newFile);

                    String statistics = (System.currentTimeMillis() / 1000L - startTime) + "," + originalFileName + ","
                            + outputfile + (trieListener.didExposeNewBranch() ? ",+cov" : "")
                            + (trieListener.didObserveBetterScore()
                                    ? ",highscore," + trieListener.getObservedOldCostForLeafNode() + "/"
                                            + trieListener.getObservedNewCostForLeafNode()
                                    : "")
                            + (trieListener.didFindNewDiffPath() ? ",+ddiff" : "")
                            // we only want to mark crashes which happen in the new version only -> new crashes
                            + ((trieListener.finishedWithCrash() && trieListener.didFindNewDiffPath()) ? ",+crash" : "")
                            + (trieListener.didImprovePatchDistance() ? ",+patchdist" : "") + "\n";
                    Statistics.appendExportStatistics(input, statistics);
                }
            }
            if (spfMode.equals(ConcreteSPFMode.IMPORT)) {
                String statistic = (System.currentTimeMillis() / 1000L - startTime) + "," + originalFileName + ","
                        + trieListener.getObservedOldCostForLeafNode() + "/"
                        + trieListener.getObservedNewCostForLeafNode()
                        + (trieListener.didObserveBetterScore() ? ",highscore" : "") + "\n";
                Statistics.appendImportStatistics(input, statistic);
            }

            this.trie = trieListener.getResultingTrie();

            return Triple.of(trieListener.getObservedOldCostForLeafNode(), trieListener.getObservedNewCostForLeafNode(),
                    trieListener.didObserveBetterScore());

        } catch (JPFConfigException cx) {
            cx.printStackTrace();
        } catch (JPFException jx) {
            jx.printStackTrace();
        }

        return null;
    }

    private void runJPFReplayAndBSE(Trie trie, int additionalDecisions, int[] inputSizes) {
        TrieGuidanceListener trieBuilderListener = null;
        try {
            Config conf = initSPFConfig();

            /*
             * Here we do not want to use symcrete execution, instead we will start with guided trie execution and alter
             * to proper bounded symbolic execution. The switching betweeen replay and BSE is implemented in the
             * listener below.
             */
            conf.setProperty("symbolic.collect_constraints", "false");
            conf.setProperty("target.args", input.jpf_argument.replace("@@", ""));

            /* We only have additional listener for the real JPF runs. */
            input.symListener.ifPresent(value -> conf.setProperty("listener", value));

            JPF jpf = new JPF(conf);

            // SymbolicListener symbolicListener = new SymbolicListener(conf, jpf); // TODO YN: have or have it not?
            // jpf.addListener(symbolicListener);

            if (input.symShadow.isPresent() && input.symShadow.get().equals("true")) {
                ShadowListener shadowListener = new ShadowListener(conf, jpf);
                jpf.addListener(shadowListener);
            }

            trieBuilderListener = new TrieGuidanceListener(conf, jpf, trie, additionalDecisions, pcAndSolutionQueue);
            jpf.addListener(trieBuilderListener);

            // reset last observed cost before each execution.
            Observations.reset();

            // Set the correct input size for the node of interest (only important for side-channel analysis).
            if (inputSizes != null) {
                if (inputSizes.length > 0) {
                    Observations.lastObservedInputSizes = inputSizes;
                } else {
                    Observations.lastObservedInputSizes = input.inputSizes; // set maximum
                }
            }

            jpf.run();

            if (jpf.foundErrors()) {
                System.out.println("#FOUND ERRORS = " + jpf.getSearchErrors().size());
            }
        } catch (JPFConfigException cx) {
            cx.printStackTrace();
            System.exit(1);
        } catch (JPFException jx) {
            if (jx.getCause() instanceof ExitException) {
                if (((ExitException) jx.getCause()).shouldReport()) {
                    jx.printStackTrace();
                    System.exit(1);
                }
            } else {
                jx.printStackTrace();
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private Triple<Pair<PathCondition, Map<String, Object>>, Double, Double> runJPF_NoTrieModificationButOptimize(
            String targetArgument, String originalFileName, ConcreteSPFMode spfMode) {

        if (targetArgument == null) {
            return null;
        }

        System.out.println("Run JPF with argument: " + targetArgument);

        try {
            Config conf = initSPFConfig();

            /*
             * Collect constraints = true, i.e. choice generators will only have ONE choice, exactly as the concrete
             * input determines
             */
            conf.setProperty("symbolic.collect_constraints", "true");

            /*
             * Here we do *not* want to disable solving because we want to optimize the cost for the current input. We
             * need this optimization because the user-defined cost might depend on symbolic values which are not
             * represented in decisions, hence, we first want to make sure that we use an input that really provides the
             * maximum user-defined cost before adding this input to the trie.
             */
            // conf.setProperty("symbolic.dp", "no_solver");

            conf.setProperty("target.args", input.jpf_argument.replace("@@", targetArgument));

            JPF jpf = new JPF(conf);

            // SymbolicListener symbolicListener = new SymbolicListener(conf, jpf);
            // jpf.addListener(symbolicListener);

            if (input.symShadow.isPresent() && input.symShadow.get().equals("true")) {
                ShadowListener shadowListener = new ShadowListener(conf, jpf);
                jpf.addListener(shadowListener);
            }

            // reset last observed cost before each execution.
            Observations.lastObservedUserdefinedCostOldVersion = 0.0;
            Observations.lastObservedUserdefinedCostNewVersion = 0.0;
            Observations.lastObservedSymbolicExpressionOldVersion = null;
            Observations.lastObservedSymbolicExpressionNewVersion = null;

            SymCreteCostListener symcreteListener = new SymCreteCostListener(conf, jpf);
            jpf.addListener(symcreteListener);

            jpf.run();

            if (jpf.foundErrors()) {
                System.out.println("#FOUND ERRORS = " + jpf.getSearchErrors().size());
            }

            Double observedOldCost = symcreteListener.getObservedFinalOldCost();
            Double observedNewCost = symcreteListener.getObservedFinalNewCost();
            PathCondition observedPC = symcreteListener.getObservedPathCondition();
            Map<String, Object> observedSolution = symcreteListener.getObservedPCSolution();

            return Triple.of(new Pair<>(observedPC, observedSolution), observedOldCost, observedNewCost);

        } catch (JPFConfigException cx) {
            cx.printStackTrace();
        } catch (JPFException jx) {
            jx.printStackTrace();
        }

        return null;
    }

    private Config initSPFConfig() {
        Config conf = JPF.createConfig(new String[0]);
        conf.setProperty("classpath", input.jpf_classpath);
        conf.setProperty("target", input.jpf_targetClass);
        conf.setProperty("jvm.insn_factory.class", "gov.nasa.jpf.symbc.SymbolicInstructionFactory");
        conf.setProperty("vm.storage.class", "nil");
        conf.setProperty("symbolic.dp", input.spf_dp);
        input.spf_symbolicMethod.ifPresent(value -> conf.setProperty("symbolic.method", value));
        input.symMaxInt.ifPresent(value -> conf.setProperty("symbolic.max_int", value));
        input.symMinInt.ifPresent(value -> conf.setProperty("symbolic.min_int", value));
        input.symMaxChar.ifPresent(value -> conf.setProperty("symbolic.max_char", value));
        input.symMinChar.ifPresent(value -> conf.setProperty("symbolic.min_char", value));
        input.symMaxByte.ifPresent(value -> conf.setProperty("symbolic.max_byte", value));
        input.symMinByte.ifPresent(value -> conf.setProperty("symbolic.min_byte", value));
        input.symMaxLong.ifPresent(value -> conf.setProperty("symbolic.max_long", value));
        input.symMinLong.ifPresent(value -> conf.setProperty("symbolic.min_long", value));
        input.symMaxDouble.ifPresent(value -> conf.setProperty("symbolic.max_double", value));
        input.symMinDouble.ifPresent(value -> conf.setProperty("symbolic.min_double", value));
        input.symPrintDebug.ifPresent(value -> conf.setProperty("symbolic.debug", value));
        input.symDefaultValue.ifPresent(value -> conf.setProperty("symbolic.undefined", value));
        input.symOptimizeChoices.ifPresent(value -> conf.setProperty("symbolic.optimizechoices", value));
        input.symShadow.ifPresent(value -> conf.setProperty("symbolic.shadow", value));
        conf.setProperty("symbolic.arrays", input.symArrays);
        conf.setProperty("symbolic.lazy", input.symLazy);
        return conf;
    }

    private void buildTrieFromProcessedInput(Map<String, String> parseInputs, ConcreteSPFMode spfMode) {
        if (parseInputs != null) {
            for (Entry<String, String> inputEntry : parseInputs.entrySet()) {
                String originalFileName = inputEntry.getKey();
                String processedFileName = inputEntry.getValue().replaceAll(",", "#");

                // If the optimization parameter is enabled, then first try to optimize the current file. This makes
                // only sense if we use a user-defined cost metric because only there we might have a
                if (input.spf_dp.endsWith("optimize") && input.useUserDefinedCost) {

                    // Make a dry (without changing anything from the trie).
                    Triple<Pair<PathCondition, Map<String, Object>>, Double, Double> resultOriginalInput = runJPF_NoTrieModificationButOptimize(
                            processedFileName, originalFileName, spfMode);
                    Pair<PathCondition, Map<String, Object>> observedPcAndSolution = resultOriginalInput.getLeft();
                    Double observedOldCostOriginalInput = resultOriginalInput.getMiddle();
                    Double observedNewCostOriginalInput = resultOriginalInput.getRight();

                    if (observedPcAndSolution._1 == null || observedPcAndSolution._2 == null
                            || observedPcAndSolution._2.isEmpty()) {
                        // run is not complete, likely because of an exeption, i.e. there is nothing to optimize.
                        System.out.println(); // TODO YN in general, how should we handle exception, also if it is not
                                              // in userdefined mode?, for now I disabled this in the application code
                        throw new RuntimeException("incomplete run");
                    }

                    // Generate input for maximized cost.
                    String maximizedInputFile = generateTmpInputfile(observedPcAndSolution);
                    List<String> listToMatchAPI = new ArrayList<>();
                    listToMatchAPI.add(maximizedInputFile);
                    Map<String, String> processedVersionOfMaximizedInputFile = input.ioUtils
                            .processInput(listToMatchAPI);
                    String processedMaximizedInputFile = processedVersionOfMaximizedInputFile.get(maximizedInputFile);

                    // Perform real run with trie.
                    Triple<Double, Double, Boolean> resultMaximizedInput = runJPFSymCrete(processedMaximizedInputFile,
                            maximizedInputFile, this.trie, spfMode);
                    Double observedOldCostMaximizedInput = resultMaximizedInput.getLeft();
                    Double observedNewCostMaximizedInput = resultMaximizedInput.getMiddle();
                    Boolean maximizedCostTriggeredNewHighscore = resultMaximizedInput.getRight();

                    if (spfMode.equals(ConcreteSPFMode.IMPORT)) {
                        // If we import files from AFL, we may want to directly export a maximized version if it is also
                        // a new highscore.
                        if ((observedOldCostMaximizedInput > observedOldCostOriginalInput
                                || observedNewCostMaximizedInput > observedNewCostOriginalInput)
                                && maximizedCostTriggeredNewHighscore) {
                            String outputfile = input.exportDir + "/id:"
                                    + String.format("%06d", lastId.incrementAndGet());

                            File tmpFile = new File(maximizedInputFile);
                            File newFile = new File(outputfile);
                            tmpFile.renameTo(newFile);

                            String statistics = (System.currentTimeMillis() / 1000L - startTime) + ","
                                    + maximizedInputFile + "," + outputfile + ",highscore,maximized,"
                                    + observedOldCostMaximizedInput + "/" + observedNewCostMaximizedInput + "\n";
                            Statistics.appendExportStatistics(input, statistics);
                        }
                    }
                } else {
                    // If we do not maximize any terms, then this represents the normal run.
                    runJPFSymCrete(processedFileName, originalFileName, this.trie, spfMode);
                }

            }
        }
    }

}
