package edu.cmu.sv.badger.analysis;

import java.util.Comparator;

import edu.cmu.sv.badger.listener.IBehavior;
import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Exploration heuristic for symbolic execution search.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public abstract class ExplorationHeuristic implements Comparator<TrieNode> {

    /**
     * Unique id for each exploration heuristic.
     */
    public final String ID;

    protected ExplorationHeuristic(String id) {
        this.ID = id;
    }

    /**
     * Determines meaning of "new" behavior.
     * 
     * @param behaviorListener
     *            - IBehavior
     * @return true or false
     */
    public abstract boolean didObserveNewBehavior(IBehavior behaviorListener);

}
