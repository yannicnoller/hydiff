package edu.cmu.sv.badger.analysis;

import edu.cmu.sv.badger.listener.IBehavior;
import edu.cmu.sv.badger.trie.TrieNode;

public abstract class DiffExplorationHeuristic extends ExplorationHeuristic {

    private DiffExplorationHeuristic(String id) {
        super(id);
    }

    @Override
    public boolean didObserveNewBehavior(IBehavior behaviorListener) {
        return behaviorListener.didExposeNewBranch() || behaviorListener.didObserveBetterScore()
                || behaviorListener.didFindNewDiffPath() || behaviorListener.didImprovePatchDistance();
    }

    public static final DiffExplorationHeuristic HIGHEST_COST_DIFF_HIGHEST_NODE = new DiffExplorationHeuristic(
            "new-diff-new-branch-closer-patch-highest-cost-diff-highest-node") {

        @Override
        public int compare(TrieNode o1, TrieNode o2) {

            // 1. prioritize nodes that contain a DiffExpression, but are not yet a DiffPath.
            boolean o1newDiffPossible = o1.containsDiffExpression() && !o1.isOnDiffPath();
            boolean o2newDiffPossible = o2.containsDiffExpression() && !o2.isOnDiffPath();
            if (o1newDiffPossible && !o2newDiffPossible) {
                return -1;
            } else if (o2newDiffPossible && !o1newDiffPossible) {
                return +1;
            }
            
            // 2. Prioritize no diff expression before a already diff path. Note: we only have here nodes that can reach the patch.
            if (!o1.containsDiffExpression() && o2.isOnDiffPath()) {
                return -1;
            } else if (!o2.containsDiffExpression() && o1.isOnDiffPath()) {
                return +1;
            }
            
            // 3. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            }
            
            // 4. If both patch distances are greater zero (i.e. did not touch the patch yet), then prioritize the
            // smaller distance.
            Integer o1PatchDistance = o1.getPatchDistance();
            Integer o2PatchDistance = o2.getPatchDistance();
            if (o1PatchDistance != null && o1PatchDistance > 0 && o2PatchDistance != null && o2PatchDistance > 0
                    && o1PatchDistance != o2PatchDistance) {
                return o1PatchDistance - o2PatchDistance;
            }

            // 5. prioritize higher cost difference between old and new version
            if (!o1.isOnDiffPath() && !o2.isOnDiffPath()) {
                int o1CostDiff = (int) Math.abs(o1.getOldMetricValue() - o1.getNewMetricValue());
                int o2CostDiff = (int) Math.abs(o2.getOldMetricValue() - o2.getNewMetricValue());
                int metricComp = o2CostDiff - o1CostDiff;
                if (metricComp != 0) {
                    return metricComp;
                }
            } else if (o1.isOnDiffPath() && o2.isOnDiffPath()) {
                // Maximize cost of new version
                int metricComp = (int) (o2.getNewMetricValue() - o1.getNewMetricValue());
                if (metricComp != 0) {
                    return metricComp;
                }
            }

            // 6. prioritize highest nodes in the tree.
            return o1.getDepth() - o2.getDepth();
        }

    };
    
    public static final DiffExplorationHeuristic HIGHEST_COST_DIFF_SIDE_CHANNEL = new DiffExplorationHeuristic(
            "high-cost-diff-side-channel") {

        @Override
        public int compare(TrieNode o1, TrieNode o2) {

            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            }
            
            // 2. prioritize higher cost difference between old and new version
            if (!o1.isOnDiffPath() && !o2.isOnDiffPath()) {
                int o1CostDiff = (int) Math.abs(o1.getOldMetricValue() - o1.getNewMetricValue());
                int o2CostDiff = (int) Math.abs(o2.getOldMetricValue() - o2.getNewMetricValue());
                int metricComp = o2CostDiff - o1CostDiff;
                if (metricComp != 0) {
                    return metricComp;
                }
            } else if (o1.isOnDiffPath() || o2.isOnDiffPath()) {
                // Maximize cost of new version
                int metricComp = (int) (o2.getNewMetricValue() - o1.getNewMetricValue());
                if (metricComp != 0) {
                    return metricComp;
                }
            }

            // 6. prioritize highest nodes in the tree.
            return o1.getDepth() - o2.getDepth();
        }

    };
}
