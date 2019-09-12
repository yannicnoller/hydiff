package edu.cmu.sv.badger.analysis;

import edu.cmu.sv.badger.listener.IBehavior;
import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Defines all exploration heuristics for the worst-case analysis in Badger. Each heuristic defines how the nodes are
 * ordered in the priority queue.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public abstract class WCAExplorationHeuristic extends ExplorationHeuristic  {

    private WCAExplorationHeuristic(String id) {
       super(id);
    }
    
    @Override
    public boolean didObserveNewBehavior(IBehavior behaviorListener) {
        return behaviorListener.didExposeNewBranch() || behaviorListener.didObserveBetterScore(); 
    }
    
    public static final WCAExplorationHeuristic HIGHEST_COST_HIGHEST_NODE = new WCAExplorationHeuristic(
            "highest-cost-highest-node") {

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
                // 2. prioritize higher metric value
                int metricComp = (int) (o2.getNewMetricValue() - o1.getNewMetricValue());
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize highest nodes in the tree.
                    return o1.getDepth() - o2.getDepth();
                }
            }
        }

    };

    public static final WCAExplorationHeuristic HIGHEST_COST_LOWEST_NODE = new WCAExplorationHeuristic(
            "highest-cost-lowest-node") {

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
                // 2. prioritize higher metric value
                int metricComp = (int) (o2.getNewMetricValue() - o1.getNewMetricValue());
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize lowest nodes in the tree.
                    return o2.getDepth() - o1.getDepth();
                }
            }
        }

    };

    public static final WCAExplorationHeuristic LOWEST_COST_HIGHEST_NODE = new WCAExplorationHeuristic(
            "lowest-cost-highest-node") {

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
                // 2. prioritize lower metric value
                int metricComp = (int) (o2.getNewMetricValue() - o1.getNewMetricValue()) * (-1);
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize highest nodes in the tree.
                    return o1.getDepth() - o2.getDepth();
                }
            }
        }
    };

    public static final WCAExplorationHeuristic LOWEST_COST_LOWEST_NODE = new WCAExplorationHeuristic(
            "lowest-cost-lowest-node") {

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
                // 2. prioritize lower metric value
                int metricComp = (int) (o2.getNewMetricValue() - o1.getNewMetricValue()) * (-1);
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize lowest nodes in the tree.
                    return o2.getDepth() - o1.getDepth();
                }
            }
        }
    };

}
