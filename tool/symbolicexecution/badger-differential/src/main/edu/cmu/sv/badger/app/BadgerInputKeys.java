package edu.cmu.sv.badger.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Includes all parameters that can/should be included in the configuration property file for Badger.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public enum BadgerInputKeys {
    
    /* ID                             NAME                             IS_MANDATORY*/
    
    /* Directories */
    INITIAL_INPUT_DIR               ("dir.initial.input",              true),
    SYNC_INPUT_DIR                  ("dir.sync.input",                 false),
    EXPORT_DIR                      ("dir.export",                     true),
    TMP_DIR                         ("dir.tmp",                        false),
    
    /* Technical Parameters */
    CYCLE_WAITING_SEC               ("symexe.wait.sec",                true),
    MAX_NUMBER_SYMEXE_ITERATIONS    ("symexe.iterations",              true),
    SEC_UNTIL_FIRST_CYCLE           ("symexe.delay.sec",               false),
    NUMBER_OF_ADDITIONAL_STEPS      ("symexe.bse.steps",               false),
    
    /* JPF */
    APPLICATION_CLASSPATH           ("jpf.classpath",                  true),
    APPLICATION_TARGET_CLASS        ("jpf.target",                     true),
    APPLICATION_ARGUMENT            ("jpf.argument",                   false),
    
    /* SPF */
    SYMBOLIC_METHOD                 ("symbolic.method",                false),
    DECISION_PRCEDURE               ("symbolic.dp",                    false),
    SYM_MAX_INT                     ("symbolic.max_int",               false),
    SYM_MIN_INT                     ("symbolic.min_int",               false),
    SYM_MAX_CHAR                    ("symbolic.max_char",              false),
    SYM_MIN_CHAR                    ("symbolic.min_char",              false),
    SYM_MAX_BYTE                    ("symbolic.max_byte",              false),
    SYM_MIN_BYTE                    ("symbolic.min_byte",              false),
    SYM_MAX_LONG                    ("symbolic.max_long",              false),
    SYM_MIN_LONG                    ("symbolic.min_long",              false),
    SYM_MAX_DOUBLE                  ("symbolic.max_double",            false),
    SYM_MIN_DOUBLE                  ("symbolic.min_double",            false),
    SYM_DEBUG_PRINT                 ("symbolic.debug",                 false),
    SYM_DEFAULT_DONT_CARE_VALUE     ("symbolic.undefined",             false),
    SYM_OPTIMIZECHOICES             ("symbolic.optimizechoices",       false),
    SYM_LISTENER                    ("listener",                       false),
    SYM_SHADOW						("symbolic.shadow", 		       false),
    SYM_ARRAYS                      ("symbolic.arrays",                false),
    SYM_LAZY                        ("symbolic.lazy",                false),
    
    /* Shadow Specific CFG */
    SYM_SHADOW_CFG                  ("symbolic.shadow.cfg",            false),
    SYM_SHADOW_CFG_DIR              ("symbolic.shadow.cfg.dir",        false),
    SYM_SHADOW_CFG_CA_CLASS         ("symbolic.shadow.cfg.ca.class",   false),
    SYM_SHADOW_CFG_SKIP             ("symbolic.shadow.cfg.skip",       false),
    SYM_SHADOW_CFG_EXPORT_DIR       ("symbolic.shadow.cfg.export.dir", false),
    SYM_SHADOW_CFG_IMPORT_DIR       ("symbolic.shadow.cfg.import.dir", false),
    
    /* Analysis */
    ANALYSIS_METHOD                 ("analysis.method",                true),
    ANALYSIS_EXPLORATION_HEURISTIC  ("analysis.heuristic",             true),
    ANALYSIS_COST_METRIC            ("analysis.cost.metric",           false),
    ANALYSIS_COV_METRIC             ("analysis.cov.metric",            false),

    /* Input Processing / Generation */
    IO_UTILS                        ("io.utils",                       true),
    IO_INPUT_SIZES                  ("io.input.sizes",                 false),
    IO_INITIAL_ID                   ("io.initial.id",                  false),

    /* Statistics */
    PRINT_STATISTICS                ("stat.print",                     false),
    IMPORT_STATISTICS_FILE          ("stat.file.import",               false),
    GENERATION_STATISTICS_FILE      ("stat.file.generation",           false),
    EXPORT_STATISTICS_FILE          ("stat.file.export",               false),
    INTERNAL_TRIE_STATISTICS_FILE   ("stat.file.trie",                 false),
    PRINT_PC_INFO                   ("stat.print.pc",                  false),
    PC_MAPPING_FILE                 ("stat.file.pc.mapping",           false),

    /* Print Trie As Dot Files */
    PRINT_TRIE                      ("trie.print",                     false),
    PRINT_TRIE_MAX_DEPTH            ("trie.print.maxdepth",            false),
    
    DNN_DATA_DIR                    ("dir.dnn.internal.data",          false),
    DATA_DIR                        ("dir.internal.data",              false);
    
    public String name;
    public boolean isMandatory;
    
    private BadgerInputKeys(String name, boolean isMandatory) {
        this.name = name;
        this.isMandatory = isMandatory; 
    }
    
    public static final List<BadgerInputKeys> mandatoryKeys;
    static {
        mandatoryKeys = new ArrayList<>();
        for (BadgerInputKeys key : BadgerInputKeys.values()) {
            if (key.isMandatory) {
                mandatoryKeys.add(key);
            }
        }
    }
    
}
