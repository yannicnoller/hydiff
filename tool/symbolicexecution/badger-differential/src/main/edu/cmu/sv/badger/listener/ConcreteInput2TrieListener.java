package edu.cmu.sv.badger.listener;

import edu.cmu.sv.badger.analysis.CFGAnalyzer;
import edu.cmu.sv.badger.analysis.DiffAnalyzer;
import edu.cmu.sv.badger.analysis.StateBuilder;
import edu.cmu.sv.badger.analysis.WCAAnalyzer;
import edu.cmu.sv.badger.analysis.DiffAnalyzer.DiffCostStrategy;
import edu.cmu.sv.badger.analysis.WCAAnalyzer.WCACostStrategy;
import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;
import edu.cmu.sv.badger.trie.TrieNodeType;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.sequences.SequenceChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * This listener class builds a trie during dynamic symbolic execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */

public class ConcreteInput2TrieListener extends ListenerAdapter implements IBehavior {
    Trie trie;
    TrieNode cur;

    static boolean DEBUG = false;

    StateBuilder metricBuilder;
    String currentInput;
    boolean useUserDefinedCost;

    private boolean observedBetterScore = false;
    private Double observedFinalOldCost = null;
    private Double observedFinalNewCost = null;
    private boolean exposedNewBranch = false;
    private boolean isOnDiffPath = false;
    private boolean finishedWithCrash = false;
    private Integer closestPatchDistance = null;
    private boolean improvedPatchDistance = false;

    public ConcreteInput2TrieListener(Config config, JPF jpf, Trie trie, StateBuilder metricBuilder,
            String currentInput, boolean useUserDefinedCost) {
        if (DEBUG) {
            System.out.println("Building the trie ...");
        }

        this.trie = trie;
        TrieNode root = trie.getRoot();
        if (root != null) {
            cur = root;
        }

        this.metricBuilder = metricBuilder;
        this.currentInput = currentInput;
        this.useUserDefinedCost = useUserDefinedCost;
    }

    public Trie getResultingTrie() {
        return this.trie;
    }

    public Double getObservedOldCostForLeafNode() {
        return this.observedFinalOldCost;
    }

    public Double getObservedNewCostForLeafNode() {
        return this.observedFinalNewCost;
    }

    @Override
    public boolean didObserveBetterScore() { // what exactly a better score menas, depends on the cost target
        return this.observedBetterScore;
    }

    @Override
    public boolean didExposeNewBranch() {
        return this.exposedNewBranch;
    }

    @Override
    public boolean didFindNewDiffPath() {
        return isOnDiffPath;
    }

    @Override
    public boolean didImprovePatchDistance() {
        return improvedPatchDistance;
    }

    @Override
    public boolean finishedWithCrash() {
        return finishedWithCrash;
    }

    @Override
    public void propertyViolated(Search search) {
        if (cur != null) {
            cur.setPropertyViolated();
        }
    }

    @Override
    public void searchConstraintHit(Search search) {
        if (DEBUG) {
            System.out.print("search limit");
        }
        if (cur.getType().equals(TrieNodeType.REGULAR_NODE)) {
            cur.setType(TrieNodeType.FRONTIER_NODE); // set frontier
        }
        if (DEBUG) {
            System.out.print(" " + search.getStateId());
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (DEBUG) {
            System.out.println(">>> stateAdvanced");
        }
        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
        if (DEBUG) {
            System.out.println("cg: " + cg);
        }

        // thread choice instead of pc choice
        if (cg instanceof ThreadChoiceGenerator) {
            return;
        }
        if (cg instanceof SequenceChoiceGenerator) {
            return;
        }

        if (cg instanceof PCChoiceGenerator) {
            PCChoiceGenerator pcCg = (PCChoiceGenerator) cg;
            int offset = pcCg.getOffset();
            if (offset == 0) {
                return;
            }

            // create node, add it as cur's child, and update cur
            int choice = pcCg.getNextChoice();
            String method = pcCg.getMethodName();
            Instruction currentInstruction = pcCg.getInsn();
            int lineNumber = (currentInstruction != null) ? currentInstruction.getLineNumber() : -1;
            PathCondition pc = pcCg.getCurrentPC();
            
            if (trie.getRoot() == null) { // create the root node
                if (DEBUG) {
                    System.out.println(">>> create root node");
                }
                TrieNode root = new TrieNode(trie, -1, -1, null, -1, null, pc.containsDiffExpr(), pc.isDiffPC(),
                        pcCg.getExecutionMode());
                trie.setRoot(root);
                cur = root;
            }
            
            /* Check if target is still reachable. */
            if (!pc.containsDiffExpr() && !CFGAnalyzer.isPatchReachble(currentInstruction.getNext())) {
                if (DEBUG) {
                    System.out.println(">>> pruned node");
                }
                cur.setType(TrieNodeType.PRUNED_NODE);
                search.requestBacktrack();
                return;
            }

            // check if current node already contains this choice
            TrieNode child = cur.getChild(choice);
            
            if (child != null) {
                cur = child;

                /*
                 * Check if metric value of this is the initial null value, then update if with the current value form
                 * the metric builder. The null value is used to initialize nodes during the symbolic exploration phase
                 * because we don't use an metric listener there. Normally this null value happens somewhere in the
                 * middle of the tree and then the null value is overridden in the backpropagation. But it also might
                 * happen that the new explored node is a leaf node in the tree. So it is better to override this value
                 * right here, and it might be overridden again during backpropagation.
                 */
                if (cur.getOldMetricValue() == null || cur.getNewMetricValue() == null) {
                    if (!useUserDefinedCost && metricBuilder != null) {
                        cur.updateMetricValues(metricBuilder.build(pc).getOldWC(), metricBuilder.build(pc).getNewWC());
                    } else if (useUserDefinedCost) {
                        cur.updateMetricValues(Observations.lastObservedUserdefinedCostOldVersion,
                                Observations.lastObservedUserdefinedCostNewVersion);
                    }
                }

            } else {
                // create node, add it as cur's child, and update cur

                double oldCost, newCost;
                if (!useUserDefinedCost && metricBuilder != null) {
                    oldCost = metricBuilder.build(pc).getOldWC();
                    newCost = metricBuilder.build(pc).getNewWC();
                } else if (useUserDefinedCost) {
                    oldCost = Observations.lastObservedUserdefinedCostOldVersion;
                    newCost = Observations.lastObservedUserdefinedCostNewVersion;
                } else {
                    oldCost = 0.0;
                    newCost = 0.0;
                }

                TrieNode n = new TrieNode(trie, choice, offset, method, lineNumber, cur, currentInstruction, pc,
                        oldCost, newCost, Observations.lastObservedInputSizes, pc.containsDiffExpr(), pc.isDiffPC(),
                        pcCg.getExecutionMode());
                
                if (trie.getAnalyzer().addObservedChoice(currentInstruction, choice)) {
                    exposedNewBranch = true;
                }

                cur = n;
            }
        }
    }

    @Override
    public void stateBacktracked(Search search) {
        if (DEBUG) {
            System.out.println(">>> stateBacktracked");
        }

        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
        if (DEBUG) {
            System.out.println("cg: " + cg);
        }

        if (cg != null && cg instanceof PCChoiceGenerator) {
            int offset = ((PCChoiceGenerator) cg).getOffset();
            if (offset == 0) {
                return;
            }

            if (cur == null) {
                if (DEBUG) {
                    System.err.println("backtracked from root node; no action needed for now");
                }
                return;
            }

            /* Save the cost for the lowest node and mark it if it is a new best score. */
            if (cur.getChildren().isEmpty() && !cur.getType().equals(TrieNodeType.PRUNED_NODE)) {
                cur.setType(TrieNodeType.LEAF_NODE);
                observedFinalOldCost = cur.getOldMetricValue();
                observedFinalNewCost = cur.getNewMetricValue();
                isOnDiffPath = cur.isOnDiffPath();
                finishedWithCrash = cur.getPropertyViolation();
                if (trie.getAnalyzer() instanceof WCAAnalyzer) {
                    WCAAnalyzer analyzer = (WCAAnalyzer) trie.getAnalyzer();
                    if (observedFinalNewCost != null && (analyzer.getCostStrategy().equals(WCACostStrategy.MAXIMIZE)
                            ? observedFinalNewCost > analyzer.currentBestCostValue
                            : observedFinalNewCost < analyzer.currentBestCostValue)) {
                        analyzer.currentBestCostValue = observedFinalNewCost;
                        observedBetterScore = true;
                    }
                    improvedPatchDistance = false;
                } else if (trie.getAnalyzer() instanceof DiffAnalyzer) {
                    DiffAnalyzer analyzer = (DiffAnalyzer) trie.getAnalyzer();
                    if (observedFinalNewCost != null && observedFinalOldCost != null
                            & (analyzer.getCostStrategy().equals(DiffCostStrategy.MAXIMIZE_DIFF))) {
                        double currentDiffScore = Math.abs(observedFinalNewCost - observedFinalOldCost);
                        if (currentDiffScore > analyzer.globalCurrentBestCostValue) {
                            analyzer.globalCurrentBestCostValue = currentDiffScore;
                            observedBetterScore = true;
                        }
                    }

                    if (closestPatchDistance != null) {
                        if (analyzer.globalCurrentClosestPatchDistance == null
                                || closestPatchDistance < analyzer.globalCurrentClosestPatchDistance) {
                            analyzer.globalCurrentClosestPatchDistance = closestPatchDistance;
                            improvedPatchDistance = true;
                        }
                    }
                }
            }

            /* Backpropagate metric value */
            if (cur.getParent() != null) {
                if (cur.getType().equals(TrieNodeType.PRUNED_NODE)) {
                    // Special case: parent not null but node was pruned. if the parent still has unexplored other
                    // children, then we still want to put it back into the queue.
                    TrieNode parentNode = cur.getParent();
                    if (!parentNode.isCompleted()) {
                        parentNode.updateMetricValues(parentNode.getOldMetricValue(), parentNode.getNewMetricValue());
                    }
                } else {
                    // node has a parent and was not pruned, do normal backpropagation.
                    int numberOfChildren = cur.getParent().getChildren().size();
                    double newOldMetricValueForParent;
                    double newNewMetricValueForParent;
                    if (numberOfChildren == 1) {
                        // this is the first children, then just reuse this number
                        newOldMetricValueForParent = cur.getOldMetricValue();
                        newNewMetricValueForParent = cur.getNewMetricValue();
                    } else {
                        // if there are other children, then update the average
                        // Double oldMetricValueForParent = cur.getParent().getMetricValue();
                        // newMetricValueForParent = oldMetricValueForParent
                        // + (cur.getMetricValue() - oldMetricValueForParent) / numberOfChildren;
                        double sumOld = 0.0;
                        double sumNew = 0.0;
                        int numberOfSATChildren = 0;
                        for (TrieNode child : cur.getParent().getChildren()) {

                            /* Skip unsat nodes because they do not have a metric value. */
                            if (child.getType().equals(TrieNodeType.UNSAT_NODE)) {
                                continue;
                            }

                            /* Also skip pruned nodes because they do not have a metric value. */
                            if (child.getType().equals(TrieNodeType.PRUNED_NODE)) {
                                continue;
                            }

                            if (child.getOldMetricValue() != null || child.getNewMetricValue() != null) {
                                numberOfSATChildren++;
                            }

                            if (child.getOldMetricValue() != null) {
                                // Otherwise execution probably ended up in exception..
                                sumOld += child.getOldMetricValue();
                            }
                            if (child.getNewMetricValue() != null) {
                                // Otherwise execution probably ended up in exception..
                                sumNew += child.getNewMetricValue();
                            }

                        }
                        newOldMetricValueForParent = sumOld / numberOfSATChildren;
                        newNewMetricValueForParent = sumNew / numberOfSATChildren;
                    }
                    cur.getParent().updateMetricValues(newOldMetricValueForParent, newNewMetricValueForParent);
                }
            }

            if (DEBUG) {
                if (cur.getParent() == null) {
                    System.out.println("backtracked to root.");
                }
            }
            cur = cur.getParent();
        }
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
            Instruction executedInstruction) {
        /* Update the closest patch distance. */
        Integer currentDistance = CFGAnalyzer.getClosestDistanceToPatch(executedInstruction);
        if (currentDistance != null) {
            if (closestPatchDistance == null || currentDistance < closestPatchDistance) {
                closestPatchDistance = currentDistance;
            }
        }
    }

}
