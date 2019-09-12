package edu.cmu.sv.badger.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public abstract class IOUtils {

    /**
     * Preprocesses the input if necessary.
     * 
     * @param inputFiles
     * @return key= original, value= processed
     */
    public Map<String, String> processInput(List<String> inputFiles) {
        // here we don't need to process anything
        Map<String, String> res = new HashMap<>();
        for (String inputfile : inputFiles) {
            res.put(inputfile, inputfile);
        }
        return res;
    }

    public abstract void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile);

}
