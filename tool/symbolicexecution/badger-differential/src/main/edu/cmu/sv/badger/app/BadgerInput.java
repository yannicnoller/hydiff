package edu.cmu.sv.badger.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

import edu.cmu.sv.badger.analysis.BranchCountState;
import edu.cmu.sv.badger.analysis.CoverageAnalyzer;
import edu.cmu.sv.badger.analysis.CoverageExplorationHeuristic;
import edu.cmu.sv.badger.analysis.DiffAnalyzer;
import edu.cmu.sv.badger.analysis.DiffExplorationHeuristic;
import edu.cmu.sv.badger.analysis.ExplorationHeuristic;
import edu.cmu.sv.badger.analysis.WCAExplorationHeuristic;
import edu.cmu.sv.badger.analysis.InstructionCountState;
import edu.cmu.sv.badger.analysis.StateBuilderFactory;
import edu.cmu.sv.badger.analysis.TrieAnalyzer;
import edu.cmu.sv.badger.analysis.WCAAnalyzer;
import edu.cmu.sv.badger.io.ByteImageProcessorIOUtils;
import edu.cmu.sv.badger.io.ByteTextIOUtils;
import edu.cmu.sv.badger.io.CLI_DynamicMixedArray;
import edu.cmu.sv.badger.io.CLI_MixedArray;
import edu.cmu.sv.badger.io.CLI_MixedArray2;
import edu.cmu.sv.badger.io.CharArrayIOUtils;
import edu.cmu.sv.badger.io.CharArrayIOUtilsEngagement;
import edu.cmu.sv.badger.io.DiffImageDoubleDoubleIOUtils;
import edu.cmu.sv.badger.io.DynamicByteArrayIOUtils;
import edu.cmu.sv.badger.io.DynamicByteArrayIOUtilsIbasys;
import edu.cmu.sv.badger.io.DynamicCharArrayIOUtils;
import edu.cmu.sv.badger.io.DynamicIntArrayIOUtils;
import edu.cmu.sv.badger.io.FullDoubleArrayIOUtils;
import edu.cmu.sv.badger.io.FullIntArrayIOUtils;
import edu.cmu.sv.badger.io.IOUtils;
import edu.cmu.sv.badger.io.ImageByteDoubleIOUtils;
import edu.cmu.sv.badger.io.ImageDoubleDoubleIOUtils;
import edu.cmu.sv.badger.io.ImageProcessorIOUtils;
import edu.cmu.sv.badger.io.IntArrayIOUtils;
import edu.cmu.sv.badger.io.Math_60_MixedArray;
import edu.cmu.sv.badger.io.Math_atan2_MixedArray;
import edu.cmu.sv.badger.io.MerArbiterMixedArray;
import edu.cmu.sv.badger.io.MultipleIntArrayIOUtils;
import edu.cmu.sv.badger.io.TCASMixedArray;
import edu.cmu.sv.badger.io.Time_1_DynamicIntByteArrayIOUtils;
import edu.cmu.sv.badger.trie.TriePrintToDot;

/**
 * Parses the configuration file of Badger and is used as input data object.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public class BadgerInput {

    /* Directories */
    public String initialInputDir;
    public Optional<String> syncInputdir;
    public String exportDir;
    public String tmpDir;

    /* Technical Parameters */
    public int cycleWaitingSec;
    public int maximumNumberOfSymExeIterations;
    public int secUntilFirstCycle;
    public int numberOfAdditionalDecisions;

    /* JPF */
    public String jpf_classpath;
    public String jpf_targetClass;
    public String jpf_argument;

    /* SPF */
    public Optional<String> spf_symbolicMethod;
    public String spf_dp;
    public Optional<String> symMaxInt;
    public Optional<String> symMinInt;
    public Optional<String> symMaxChar;
    public Optional<String> symMinChar;
    public Optional<String> symMaxByte;
    public Optional<String> symMinByte;
    public Optional<String> symMaxLong;
    public Optional<String> symMinLong;
    public Optional<String> symMaxDouble;
    public Optional<String> symMinDouble;
    public Optional<String> symPrintDebug;
    public Optional<String> symDefaultValue;
    public Optional<String> symOptimizeChoices;
    public Optional<String> symListener;
    public Optional<String> symShadow;
    public String symArrays;
    public String symLazy;

    /* CFG */
    public Optional<String> symShadowCFG;
    public Optional<String> symShadowCFGDir;
    public Optional<String> symShadowCFGChangeAnnotationClass;
    public Optional<String> symShadowCFSkip;
    public Optional<String> symShadowCFGExportDir;
    public Optional<String> symShadowCFGImportDir;

    /* Analysis */
    public TrieAnalyzer trieAnalysisMethod;
    public ExplorationHeuristic explorationHeuristic;
    public Optional<StateBuilderFactory> stateBuilderFactory;
    public boolean useUserDefinedCost;

    /* Input Processing / Generation */
    public IOUtils ioUtils;
    public int[] inputSizes;
    public int initialId;

    /* Statistics */
    public boolean printStatistics;
    public String importStatisticsFile;
    public String generationStatisticsFile;
    public String exportStatisticsFile;
    public String trieStatisticsFile;
    public boolean printPC;
    public String pcMappingFile;

    /* Print Trie As Dot Files */
    public boolean printTrieAsDot;
    public Optional<Integer> printTrieMaxDepth;

    /* DNN Data */
    public Optional<String> dnnDataDir;

    public Optional<String> dataDir;

    public BadgerInput(Properties prop) {

        /* Check property file for missing mandatory keys. */
        List<BadgerInputKeys> missingKeys = checkForMissingMandatoryProperites(prop);
        if (!missingKeys.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            missingKeys.forEach(key -> sb.append(key.name + ","));
            throw new RuntimeException("Configuration misses mandatory keys: " + sb.toString());
        }

        /* Directories */
        this.initialInputDir = prop.getProperty(BadgerInputKeys.INITIAL_INPUT_DIR.name);
        this.syncInputdir = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYNC_INPUT_DIR.name));
        this.exportDir = prop.getProperty(BadgerInputKeys.EXPORT_DIR.name);
        this.tmpDir = prop.getProperty(BadgerInputKeys.TMP_DIR.name, "./tmp");

        /* Technical Parameters */
        try {
            this.cycleWaitingSec = NumberUtils.createInteger(prop.getProperty(BadgerInputKeys.CYCLE_WAITING_SEC.name));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Value of " + BadgerInputKeys.CYCLE_WAITING_SEC.name + " is not a number!");
        }
        try {
            this.maximumNumberOfSymExeIterations = NumberUtils
                    .createInteger((prop.getProperty(BadgerInputKeys.MAX_NUMBER_SYMEXE_ITERATIONS.name)));
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Value of " + BadgerInputKeys.MAX_NUMBER_SYMEXE_ITERATIONS.name + " is not a number!");
        }
        try {
            this.secUntilFirstCycle = NumberUtils
                    .createInteger(prop.getProperty(BadgerInputKeys.SEC_UNTIL_FIRST_CYCLE.name, "0"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Value of " + BadgerInputKeys.SEC_UNTIL_FIRST_CYCLE.name + " is not a number!");
        }
        try {
            this.numberOfAdditionalDecisions = NumberUtils
                    .createInteger((prop.getProperty(BadgerInputKeys.NUMBER_OF_ADDITIONAL_STEPS.name, "0")));
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Value of " + BadgerInputKeys.NUMBER_OF_ADDITIONAL_STEPS.name + " is not a number!");
        }

        /* JPF */
        this.jpf_classpath = prop.getProperty(BadgerInputKeys.APPLICATION_CLASSPATH.name);
        this.jpf_targetClass = prop.getProperty(BadgerInputKeys.APPLICATION_TARGET_CLASS.name);
        this.jpf_argument = prop.getProperty(BadgerInputKeys.APPLICATION_ARGUMENT.name, "@@");
        this.jpf_argument = jpf_argument.replaceAll(",", "#");
        this.jpf_argument = jpf_argument.replaceAll(" ", ",");

        /* SPF */
        this.spf_symbolicMethod = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYMBOLIC_METHOD.name));
        this.spf_dp = prop.getProperty(BadgerInputKeys.DECISION_PRCEDURE.name, "z3");
        this.symMaxInt = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_INT.name));
        this.symMinInt = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_INT.name));
        this.symMaxChar = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_CHAR.name));
        this.symMinChar = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_CHAR.name));
        this.symMaxByte = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_BYTE.name));
        this.symMinByte = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_BYTE.name));
        this.symMaxLong = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_LONG.name));
        this.symMinLong = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_LONG.name));
        this.symMaxDouble = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_DOUBLE.name));
        this.symMinDouble = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_DOUBLE.name));
        this.symPrintDebug = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_DEBUG_PRINT.name));
        this.symDefaultValue = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_DEFAULT_DONT_CARE_VALUE.name));
        this.symOptimizeChoices = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_OPTIMIZECHOICES.name));
        this.symListener = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_LISTENER.name));
        this.symShadow = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_SHADOW.name));
        if (this.symShadow.isPresent() && this.symShadow.get().equals("true")) {
            TriePrintToDot.printShadowInfo = true;
            this.symShadowCFG = Optional.of(prop.getProperty(BadgerInputKeys.SYM_SHADOW_CFG.name));
            if (this.symShadowCFG.isPresent() && this.symShadowCFG.get().equals("true")) {
                this.symShadowCFGDir = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_SHADOW_CFG_DIR.name));
                this.symShadowCFGImportDir = Optional
                        .ofNullable(prop.getProperty(BadgerInputKeys.SYM_SHADOW_CFG_IMPORT_DIR.name));
                if (!this.symShadowCFGDir.isPresent() && !this.symShadowCFGImportDir.isPresent()) {
                    throw new RuntimeException("You have set " + BadgerInputKeys.SYM_SHADOW_CFG.name
                            + "=true, then you also have to specfiy either a path to the class files ("
                            + BadgerInputKeys.SYM_SHADOW_CFG_DIR.name + "=<path>) or the path to the serialized cfg ("
                            + BadgerInputKeys.SYM_SHADOW_CFG_IMPORT_DIR.name + "=<path>)");
                }
                if (this.symShadowCFGDir.isPresent()) {
                    this.symShadowCFGChangeAnnotationClass = Optional
                            .ofNullable(prop.getProperty(BadgerInputKeys.SYM_SHADOW_CFG_CA_CLASS.name));
                    if (!this.symShadowCFGChangeAnnotationClass.isPresent()) {
                        throw new RuntimeException("Since you enabled CFG building by setting the attribute "
                                + BadgerInputKeys.SYM_SHADOW_CFG_DIR.name
                                + ", you will have to specify the path ot the ChangeAnnotation.class file ("
                                + BadgerInputKeys.SYM_SHADOW_CFG_CA_CLASS.name + "=/../ChangeAnnotation.class)");
                    }
                }
                this.symShadowCFSkip = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_SHADOW_CFG_SKIP.name));
                this.symShadowCFGExportDir = Optional
                        .ofNullable(prop.getProperty(BadgerInputKeys.SYM_SHADOW_CFG_EXPORT_DIR.name));
            }
        }
        this.symArrays = prop.getProperty(BadgerInputKeys.SYM_ARRAYS.name, "false");
        this.symLazy = prop.getProperty(BadgerInputKeys.SYM_LAZY.name, "off");

        /* Analysis */
        String analysisMethod = prop.getProperty(BadgerInputKeys.ANALYSIS_METHOD.name, WCAAnalyzer.ID);
        String selectedExplorationHeuristic = prop.getProperty(BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name);

        switch (analysisMethod) {
        case WCAAnalyzer.ID:
            if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.HIGHEST_COST_HIGHEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.HIGHEST_COST_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.HIGHEST_COST_LOWEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.HIGHEST_COST_LOWEST_NODE;
            } else if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.LOWEST_COST_HIGHEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.LOWEST_COST_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.LOWEST_COST_LOWEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.LOWEST_COST_LOWEST_NODE;
            } else {
                throw new RuntimeException("Unknown value for " + BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name
                        + ": " + selectedExplorationHeuristic + ". Check whether you have selected a suitable "
                        + WCAAnalyzer.ID + " heuristic.");
            }
            this.trieAnalysisMethod = new WCAAnalyzer(explorationHeuristic);
            break;
        case CoverageAnalyzer.ID:
            if (selectedExplorationHeuristic.equals(CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic.equals(CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE;
            } else if (selectedExplorationHeuristic
                    .equals(CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE_EXPORT_ALL.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE_EXPORT_ALL;
            } else if (selectedExplorationHeuristic
                    .equals(CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE_EXPORT_ALL.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE_EXPORT_ALL;
            } else {
                throw new RuntimeException("Unknown value for " + BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name
                        + ": " + selectedExplorationHeuristic + ". Check whether you have selected a suitable "
                        + CoverageAnalyzer.ID + " heuristic.");
            }
            this.trieAnalysisMethod = new CoverageAnalyzer(explorationHeuristic);
            break;
        case DiffAnalyzer.ID:
            if (selectedExplorationHeuristic.equals(DiffExplorationHeuristic.HIGHEST_COST_DIFF_HIGHEST_NODE.ID)) {
                explorationHeuristic = DiffExplorationHeuristic.HIGHEST_COST_DIFF_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic
                    .equals(DiffExplorationHeuristic.HIGHEST_COST_DIFF_SIDE_CHANNEL.ID)) {
                explorationHeuristic = DiffExplorationHeuristic.HIGHEST_COST_DIFF_SIDE_CHANNEL;
            } else {
                throw new RuntimeException("Unknown value for " + BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name
                        + ": " + selectedExplorationHeuristic + ". Check whether you have selected a suitable "
                        + DiffAnalyzer.ID + " heuristic.");
            }
            this.trieAnalysisMethod = new DiffAnalyzer(explorationHeuristic);
            break;
        default:
            throw new RuntimeException(
                    "Unknown value for " + BadgerInputKeys.ANALYSIS_METHOD.name + ": " + analysisMethod);
        }

        if (analysisMethod.equals(WCAAnalyzer.ID) || analysisMethod.equals(DiffAnalyzer.ID)) {
            String costMetric = prop.getProperty(BadgerInputKeys.ANALYSIS_COST_METRIC.name);
            if (costMetric != null) {
                switch (costMetric) {
                case BranchCountState.ID:
                    this.stateBuilderFactory = Optional.of(new BranchCountState.BranchBuilderFactory());
                    this.useUserDefinedCost = false;
                    break;
                case InstructionCountState.ID:
                    this.stateBuilderFactory = Optional.of(new InstructionCountState.InstructionBuilderFactory());
                    this.useUserDefinedCost = false;
                    break;
                case "userdefined":
                    this.stateBuilderFactory = Optional.empty(); // metric values will be user defined
                    this.useUserDefinedCost = true;
                    break;
                case "jumps-userdefined": // we want to measure jumps and use this information in userdefined costs.
                    this.stateBuilderFactory = Optional.of(new BranchCountState.BranchBuilderFactory());
                    this.useUserDefinedCost = true;
                    break;
                default:
                    throw new RuntimeException(
                            "Unkown value for " + BadgerInputKeys.ANALYSIS_COST_METRIC.name + ": " + costMetric);
                }
            } else {
                throw new RuntimeException(BadgerInputKeys.ANALYSIS_METHOD.name + "=" + analysisMethod
                        + " needs the existence of values for " + BadgerInputKeys.ANALYSIS_COST_METRIC.name);
            }
        } else {
            stateBuilderFactory = Optional.empty();
            this.useUserDefinedCost = false;
        }

        /* Input Processing / Generation */
        String ioUtilsSelection = prop.getProperty(BadgerInputKeys.IO_UTILS.name);
        String inputSizesString = prop.getProperty(BadgerInputKeys.IO_INPUT_SIZES.name);
        if (inputSizesString != null) {
            String[] inputSizesStringSplitted = inputSizesString.split(" ");
            this.inputSizes = new int[inputSizesStringSplitted.length];
            for (int i = 0; i < inputSizes.length; i++) {
                try {
                    this.inputSizes[i] = NumberUtils.createInteger(inputSizesStringSplitted[i]);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Values of " + BadgerInputKeys.IO_INPUT_SIZES.name + " are no numbers!");
                }
            }
        } else {
            this.inputSizes = null;
        }
        switch (ioUtilsSelection) {
        case ImageProcessorIOUtils.ID:
            this.ioUtils = new ImageProcessorIOUtils();
            break;
        case ByteImageProcessorIOUtils.ID:
            this.ioUtils = new ByteImageProcessorIOUtils();
            break;
        case IntArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + IntArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new IntArrayIOUtils(inputSizes[0]);
            break;
        case CharArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CharArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CharArrayIOUtils(inputSizes[0]);
            break;
        case CharArrayIOUtilsEngagement.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CharArrayIOUtilsEngagement.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CharArrayIOUtilsEngagement(inputSizes[0]);
            break;
        case ByteTextIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + ByteTextIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new ByteTextIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case FullIntArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + FullIntArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new FullIntArrayIOUtils(inputSizes[0]);
            break;
        case DynamicIntArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DynamicIntArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DynamicIntArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case DynamicByteArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DynamicByteArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DynamicByteArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case MultipleIntArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + MultipleIntArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new MultipleIntArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case DynamicCharArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DynamicCharArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DynamicCharArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case ImageByteDoubleIOUtils.ID:
            if (inputSizes.length != 3) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + ImageByteDoubleIOUtils.ID
                        + " needs three value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new ImageByteDoubleIOUtils(inputSizes[0], inputSizes[1], inputSizes[2]);
            break;
        case ImageDoubleDoubleIOUtils.ID:
            if (inputSizes.length != 3) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + ImageDoubleDoubleIOUtils.ID
                        + " needs three value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new ImageDoubleDoubleIOUtils(inputSizes[0], inputSizes[1], inputSizes[2]);
            break;
        case TCASMixedArray.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + TCASMixedArray.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new TCASMixedArray(inputSizes[0]);
            break;
        case MerArbiterMixedArray.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + MerArbiterMixedArray.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new MerArbiterMixedArray(inputSizes[0], inputSizes[1]);
            break;
        case Math_atan2_MixedArray.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + Math_atan2_MixedArray.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new Math_atan2_MixedArray(inputSizes[0]);
            break;
        case DiffImageDoubleDoubleIOUtils.ID:
            if (inputSizes.length != 4) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DiffImageDoubleDoubleIOUtils.ID
                        + " needs four value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DiffImageDoubleDoubleIOUtils(inputSizes[0], inputSizes[1], inputSizes[2], inputSizes[3]);
            break;
        case FullDoubleArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + FullDoubleArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new FullDoubleArrayIOUtils(inputSizes[0]);
            break;
        case Math_60_MixedArray.ID:
            if (inputSizes != null) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + Math_60_MixedArray.ID
                        + " needs no value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new Math_60_MixedArray();
            break;
        case Time_1_DynamicIntByteArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + Time_1_DynamicIntByteArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new Time_1_DynamicIntByteArrayIOUtils(inputSizes[0]);
            break;
        case DynamicByteArrayIOUtilsIbasys.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DynamicByteArrayIOUtilsIbasys.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DynamicByteArrayIOUtilsIbasys(inputSizes[0]);
            break;
        case CLI_MixedArray.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CLI_MixedArray.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CLI_MixedArray(inputSizes[0], inputSizes[1]);
            break;
        case CLI_MixedArray2.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CLI_MixedArray2.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CLI_MixedArray2(inputSizes[0], inputSizes[1]);
            break;
        case CLI_DynamicMixedArray.ID:
            if (inputSizes.length != 3) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CLI_DynamicMixedArray.ID
                        + " needs three value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CLI_DynamicMixedArray(inputSizes[0], inputSizes[1], inputSizes[2]);
            break;
        default:
            throw new RuntimeException("Unkown value for " + BadgerInputKeys.IO_UTILS.name + ": " + ioUtilsSelection);
        }
        try {
            this.initialId = NumberUtils.createInteger(prop.getProperty(BadgerInputKeys.IO_INITIAL_ID.name, "0"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Value of " + BadgerInputKeys.IO_INITIAL_ID.name + " is not a number!");
        }

        /* Statistics */
        this.printStatistics = Boolean.valueOf(prop.getProperty(BadgerInputKeys.PRINT_STATISTICS.name, "true"));
        this.importStatisticsFile = prop.getProperty(BadgerInputKeys.IMPORT_STATISTICS_FILE.name,
                "import-statistic.txt");
        this.generationStatisticsFile = prop.getProperty(BadgerInputKeys.GENERATION_STATISTICS_FILE.name,
                "generation-statistic.txt");
        this.exportStatisticsFile = prop.getProperty(BadgerInputKeys.EXPORT_STATISTICS_FILE.name,
                "export-statistic.txt");
        this.trieStatisticsFile = prop.getProperty(BadgerInputKeys.INTERNAL_TRIE_STATISTICS_FILE.name,
                "trie-statistic.txt");
        this.printPC = Boolean.valueOf(prop.getProperty(BadgerInputKeys.PRINT_PC_INFO.name, "false"));
        this.pcMappingFile = prop.getProperty(BadgerInputKeys.PC_MAPPING_FILE.name, "pcMap.txt");

        /* Print Trie As Dot Files */
        this.printTrieAsDot = Boolean.valueOf(prop.getProperty(BadgerInputKeys.PRINT_TRIE.name));
        this.printTrieMaxDepth = Optional
                .ofNullable(NumberUtils.createInteger(prop.getProperty(BadgerInputKeys.PRINT_TRIE_MAX_DEPTH.name)));

        /* DNN Data */
        this.dnnDataDir = Optional.ofNullable(prop.getProperty(BadgerInputKeys.DNN_DATA_DIR.name));

        this.dataDir = Optional.ofNullable(prop.getProperty(BadgerInputKeys.DATA_DIR.name));
    }

    private List<BadgerInputKeys> checkForMissingMandatoryProperites(Properties prop) {
        List<BadgerInputKeys> missingKeys = new ArrayList<>();
        for (BadgerInputKeys key : BadgerInputKeys.mandatoryKeys) {
            if (!prop.containsKey(key.name)) {
                missingKeys.add(key);
            }
        }
        return missingKeys;
    }

    public static BadgerInput loadFromConfigFile(String configFilePath) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(configFilePath);
            prop.load(input);
            return new BadgerInput(prop);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("[ERROR] Configuration file not found", e);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Error configuraiton file", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
