package edu.cmu.sv.badger.analysis;

import edu.cmu.sv.badger.listener.IBehavior;
import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Defines all exploration heuristics for the coverage based analysis in Badger. Each heuristic defines how the nodes
 * are ordered in the priority queue.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public abstract class CoverageExplorationHeuristic extends ExplorationHeuristic {

    private CoverageExplorationHeuristic(String id) {
        super(id);
    }

    public static final CoverageExplorationHeuristic BRANCH_COV_HIGHEST_NODE = new CoverageExplorationHeuristic(
            "branch-higest") {
        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize lowest nodes in the tree.
                return o2.getDepth() - o1.getDepth();
            }
        }

        @Override
        public boolean didObserveNewBehavior(IBehavior behaviorListener) {
            return behaviorListener.didExposeNewBranch();
        }
    };
    
    public static final CoverageExplorationHeuristic BRANCH_COV_LOWEST_NODE = new CoverageExplorationHeuristic(
            "branch-lowest") {
        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize highest nodes in the tree.
                return o1.getDepth() - o2.getDepth();
            }
        }

        @Override
        public boolean didObserveNewBehavior(IBehavior behaviorListener) {
            return behaviorListener.didExposeNewBranch();
        }
    };

    public static final CoverageExplorationHeuristic BRANCH_COV_HIGHEST_NODE_EXPORT_ALL = new CoverageExplorationHeuristic(
            "branch-highest-all") {
        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize highest nodes in the tree.
                return o1.getDepth() - o2.getDepth();
            }
        }

        @Override
        public boolean didObserveNewBehavior(IBehavior behaviorListener) {
            return true;
        }
    };
    
    public static final CoverageExplorationHeuristic BRANCH_COV_LOWEST_NODE_EXPORT_ALL = new CoverageExplorationHeuristic(
            "branch-lowest-all") {
        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize lowest nodes in the tree.
                return o2.getDepth() - o1.getDepth();
            }
        }

        @Override
        public boolean didObserveNewBehavior(IBehavior behaviorListener) {
            return true;
        }
    };

}
