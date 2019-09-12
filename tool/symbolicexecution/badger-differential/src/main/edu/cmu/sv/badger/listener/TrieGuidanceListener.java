package edu.cmu.sv.badger.listener;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import edu.cmu.sv.badger.analysis.CFGAnalyzer;
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
import gov.nasa.jpf.util.Pair;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * This listener class guides symbolic execution along an annotated trie object: It follows only trie nodes that which
 * are enabled. It starts proper symbolic execution only for those nodes that are marked as "needsExploration".
 * 
 * Those annotations should be set a prior analysis.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 * 
 */
public class TrieGuidanceListener extends ListenerAdapter {
    Trie trie;
    TrieNode cur;
    public int numberOfAdditionalDecisionSteps;
    int decisionCounter = -1;
    int maximumNumberOfNewFilesPerRun;
    boolean finishedComplete = true;

    boolean stayAtNode = false;

    BlockingQueue<Pair<PathCondition, Map<String, Object>>> pcAndSolutionQueue;

    /**
     * Enumeration to represent the modes of this listener.
     */
    private enum Mode {
        REPLAY, // normal mode, follows the enabled trie nodes, no constraint solving
        BOUNDED_SYMBOLIC_EXECUTION // bounded symbolic execution mode
    };

    Mode executionMode; // Stores the current execution mode.

    /**
     * @param config
     *            - JPF Config
     * @param jpf
     *            - JPF object
     * @param trie
     *            - trie data structure that guides the execution
     * @param numberOfAdditionalDecisionSteps
     *            - maximum number of additional decisions after identifying new trie node
     */
    public TrieGuidanceListener(Config config, JPF jpf, Trie trie, int numberOfAdditionalDecisionSteps,
            BlockingQueue<Pair<PathCondition, Map<String, Object>>> pcAndSolutionQueue) {
        if (trie != null && trie.getRoot() != null) {
            this.trie = trie;
            this.cur = trie.getRoot();
            this.executionMode = Mode.REPLAY;
            PathCondition.setReplay(true);
            this.numberOfAdditionalDecisionSteps = numberOfAdditionalDecisionSteps;
            this.pcAndSolutionQueue = pcAndSolutionQueue;
        }
    }

    @Override
    public void propertyViolated(Search search) {
        if (executionMode.equals(Mode.BOUNDED_SYMBOLIC_EXECUTION)) {
            if (!cur.getType().equals(TrieNodeType.UNSAT_NODE) && !cur.getType().equals(TrieNodeType.PRUNED_NODE)) {
                cur.setPropertyViolated();
                ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
                if (cg != null && cg instanceof PCChoiceGenerator) {
                    PCChoiceGenerator pcCg = (PCChoiceGenerator) cg;
                    handleNewPathCondition(pcCg.getCurrentPC(), cur, pcCg.hasMoreChoices());
                }
            }
        }
    }

    // This functionality of this method is currently not used, because we assume
    // that each execution can be finished within the constraints. But the aborted
    // nodes get the type FRONITER_NODE.
    @Override
    public void searchConstraintHit(Search search) {
        if (cur.getType() == TrieNodeType.REGULAR_NODE) {
            cur.setType(TrieNodeType.FRONTIER_NODE);
        }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (executionMode.equals(Mode.REPLAY)) {
            if (cg instanceof ThreadChoiceGenerator) {
                return;
            }
            if (cg instanceof SequenceChoiceGenerator) {
                return;
            }

            if (cg instanceof PCChoiceGenerator) {
                int offset = ((PCChoiceGenerator) cg).getOffset();
                if (offset == 0) {
                    return;
                }
            }

            PCChoiceGenerator currentPCChoiceGenerator = (PCChoiceGenerator) cg;
            int nextChoice = cur.getGuidedChoice();
            if (nextChoice >= 0) {
                /*
                 * If this node has a guided choice value, then use it. Calling the select() method means, that this
                 * choice get selected and afterwards the choice generation is finished, i.e. with the current
                 * implementation it is only possible to select one customized choice.
                 */
                currentPCChoiceGenerator.selectGuidedChoice(nextChoice);
            }
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();

        if (cg instanceof ThreadChoiceGenerator) {
            return;
        }
        if (cg instanceof SequenceChoiceGenerator) {
            return;
        }

        if (cg instanceof PCChoiceGenerator) {
            int offset = ((PCChoiceGenerator) cg).getOffset();
            if (offset == 0) {
                return;
            }
        }

        PCChoiceGenerator currentPCChoiceGenerator = (PCChoiceGenerator) cg;
        int choice = currentPCChoiceGenerator.getNextChoice();
        int offset = currentPCChoiceGenerator.getOffset();
        String method = currentPCChoiceGenerator.getMethodName();
        Instruction currentInstruction = currentPCChoiceGenerator.getInsn();
        int lineNumber = (currentInstruction != null) ? currentInstruction.getLineNumber() : -1;

        stayAtNode = false;

        if (executionMode.equals(Mode.REPLAY)) {

            // Check if choice does already exist.
            TrieNode child = cur.getChild(choice);
            if (child != null) {
                cur = child;
                if (!cur.isEnabled()) {
                    // ignore if the node is not enabled
                    search.requestBacktrack();
                    return;
                }
                // ignore at unsatisfied and pruned nodes
                if (TrieNodeType.UNSAT_NODE.equals(cur.getType()) || TrieNodeType.PRUNED_NODE.equals(cur.getType())) {
                    search.requestBacktrack();
                }
            } else {
                // Check if the current node needs more exploration (depends on used metric for
                // analysis).
                if (cur.needsExploration()) {
                    PathCondition pc = currentPCChoiceGenerator.getCurrentPC();
                    TrieNode n = new TrieNode(trie, choice, offset, method, lineNumber, cur, currentInstruction, pc,
                            null, null, Observations.lastObservedInputSizes, pc.containsDiffExpr(), pc.isDiffPC(),
                            currentPCChoiceGenerator.getThreadInfo().getExecutionMode());
                    cur = n;
                    if (pc == null || search.getVM().getSystemState().isIgnored()) {
                        // unsatisfiable constraint
                        cur.setType(TrieNodeType.UNSAT_NODE);
                    } else if (!pc.containsDiffExpr() && !CFGAnalyzer.isPatchReachble(currentInstruction.getNext())) {
                        cur.setType(TrieNodeType.PRUNED_NODE);
                    }

                    if (numberOfAdditionalDecisionSteps > 0) {
                        executionMode = Mode.BOUNDED_SYMBOLIC_EXECUTION;
                        PathCondition.setReplay(false);
                        decisionCounter = 0;
                    } else {
                        // Decision Limit is reached. Store the current PC.
                        if (!cur.getType().equals(TrieNodeType.UNSAT_NODE) && !cur.getType().equals(TrieNodeType.PRUNED_NODE)) {
                            handleNewPathCondition(pc, cur, currentPCChoiceGenerator.hasMoreChoices());
                            search.requestBacktrack();
                        }
                    }
                } else {
                    stayAtNode = true;
                    search.requestBacktrack();
                }
            }
        } else if (executionMode.equals(Mode.BOUNDED_SYMBOLIC_EXECUTION)) {

            // create node, add it as cur's child, and update cur
            PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
            TrieNode n = new TrieNode(trie, choice, offset, method, lineNumber, cur, currentInstruction, pc, null, null,
                    Observations.lastObservedInputSizes, pc.containsDiffExpr(), pc.isDiffPC(),
                    currentPCChoiceGenerator.getThreadInfo().getExecutionMode());
            cur = n;

            if (pc == null || search.getVM().getSystemState().isIgnored()) {
                // unsatisfiable constraint
                cur.setType(TrieNodeType.UNSAT_NODE);
            } else if (!pc.containsDiffExpr() & !CFGAnalyzer.isPatchReachble(currentInstruction.getNext())) {
                cur.setType(TrieNodeType.PRUNED_NODE);
            }

            decisionCounter++;
        }
    }

    @Override
    public void stateBacktracked(Search search) {
        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();

        if (cg != null && cg instanceof PCChoiceGenerator) {
            PCChoiceGenerator currentChoiceGenerator = (PCChoiceGenerator) cg;
            int offset = currentChoiceGenerator.getOffset();
            if (offset == 0) {
                return;
            }

            if (executionMode.equals(Mode.BOUNDED_SYMBOLIC_EXECUTION)) {
                if (!cur.getType().equals(TrieNodeType.UNSAT_NODE) && !cur.getType().equals(TrieNodeType.PRUNED_NODE)) {
                    if (cur.getChildren().isEmpty()) {
                        // Only collect path conditions when we are in the mode SYMBOLIC_EXECUTION (i.e.
                        // we are exploring new nodes) and we backtracked from a node without children
                        // (final node) and this node is satisfiable. -> only if it is a "new" node
                        handleNewPathCondition(currentChoiceGenerator.getCurrentPC(), cur,
                                currentChoiceGenerator.hasMoreChoices());
                    }
                }
            }

            if (!stayAtNode) {
                if (cur.getParent() != null) {
                    cur = cur.getParent();
                }
            }

            // if backtrack from the last replayed node, then switch back to replay and
            // reset the old frontier to non-frontier
            if (executionMode.equals(Mode.BOUNDED_SYMBOLIC_EXECUTION)) {
                decisionCounter--;
                if (decisionCounter == -1) {
                    executionMode = Mode.REPLAY;
                    PathCondition.setReplay(true);
                }
            }

            if (cur.getChildren().size() == cur.getMaximumNumberOfChildren()) {
                // Then all children of current node were explored.
                cur.setExplorationNeeded(false);
            }
        }
    }

    private void handleNewPathCondition(PathCondition pc, TrieNode node, boolean choiceGeneratorIsNotFinished) {
        // Solve PC. Reset isReplay to false during satisfiability check, otherwise the
        // PathCondition will always return true.
        boolean isReplay = PathCondition.isReplay;
        PathCondition.setReplay(false);
        Map<String, Object> solution = pc.solveWithValuation();
        PathCondition.setReplay(isReplay);
        if (solution == null || solution.isEmpty()) {
            node.setType(TrieNodeType.UNSAT_NODE);
            return;
        }
        try {
            pcAndSolutionQueue.put(new Pair<PathCondition, Map<String, Object>>(pc, solution));

            /*
             * Since for the hashtable subject the solving takes very long it is better abort jpf if something was found
             * to directly try to export it.
             */
            // Put parent back in queue if not finished
            if (choiceGeneratorIsNotFinished) {
                node.getParent().resetComplete();
                JPF.exitQuietly(); // This "hard" exit is only done if the choice generator is not finished yet.
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
