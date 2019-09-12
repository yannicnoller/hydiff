package edu.cmu.sv.badger.listener;

/**
 * Defines methods that are necessary to decide whether an input, i.e. the execution driven by this input, exposed a new
 * behavior, for which for example the symbolic execution part of Badger should export the input to the fuzzer.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public interface IBehavior {

    public boolean didExposeNewBranch();

    public boolean didObserveBetterScore();
    
    public boolean didFindNewDiffPath();
    
    public boolean didImprovePatchDistance();
    
    public boolean finishedWithCrash();
    
}
