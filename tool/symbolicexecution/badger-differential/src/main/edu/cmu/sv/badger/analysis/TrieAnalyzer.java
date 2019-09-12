package edu.cmu.sv.badger.analysis;

import java.util.Set;

import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;
import gov.nasa.jpf.vm.Instruction;

/**
 * Abstract class to provide a common interface for the analysis of trie data structures.
 * 
 * @author nolleryc
 *
 */
public abstract class TrieAnalyzer { // TODO think about having only one and everything depends on the chosen heuristic

    /**
     * Returns most promising node for further exploration, and enables the path to this node in the given trie.
     * 
     * @param trie
     * @return TrieNode
     */
    public abstract TrieNode analyze(Trie trie);

    /**
     * Adds, updates or removes node in priority queue.
     * 
     * @param node
     *            - TrieNode
     * @return boolean - was update necessary?
     */
    public abstract boolean updateNode(TrieNode node);

    /**
     * Generate a String that includes several statistics about the current run.
     * 
     * @return String
     */
    public abstract String getStatistics();

    /**
     * Checks whether there is any node left that is available for exploration.
     * 
     * @return true for yes, otherwise false
     */
    public abstract boolean isNodeLeftforAnalysis();

    public abstract boolean addObservedChoice(Instruction instr, int choice);

    /**
     * Returns all choices observed so far for this instruction, i.e. returns which branches from this condition already
     * occurred during execution.
     * 
     * @param instr
     *            - int hashcode
     * @return Set of int values.
     */
    public abstract Set<Integer> getObservedChoices(int instructionHashCode);

    /**
     * Adds a new observed choice, and updates all necessary data structures.
     * 
     * @param instr
     *            - Instruction object
     * @param choice
     *            - choice integer value
     * @return true if choice was new, false otherwise.
     */
    protected void enablePathToNode(TrieNode node, int nextChoice) {
        if (node == null) {
            return;
        }

        node.setEnabled();

        if (nextChoice >= 0) {
            node.setGuidedChoice(nextChoice);
        }

        enablePathToNode(node.getParent(), node.getChoice());
    }

}
