package edu.cmu.sv.badger.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;
import gov.nasa.jpf.vm.Instruction;

/**
 * Analyzes the trie with regards to coverage. So far only branch coverage is supported.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public class CoverageAnalyzer extends TrieAnalyzer {

    public final static String ID = "cov";

    /* Stores all TrieNodes of the Trie that are available for exploration in a prioritized order. */
    private Queue<TrieNode> queue;

    /* Stores the so far made choices for each observed Instruction. Used to determine branch new branches. */
    private Map<Integer, Set<Integer>> observedChoices;

    /*
     * Stores all TrieNodes that correspond to the same Instruction in order to update them faster if there is change in
     * the choices. Instruction is stored in hashcode representation. The complete Instruction object leads to memory
     * problems.
     */
    private Map<Integer, Set<TrieNode>> instruction2NodeMapping;

    public CoverageAnalyzer(ExplorationHeuristic heuristic) {
        this.queue = new PriorityQueue<>(heuristic);
        this.observedChoices = new HashMap<>();
        this.instruction2NodeMapping = new HashMap<>();
    }

    @Override
    public TrieNode analyze(Trie trie) {
        // Reset annotations in trie from previous runs.
        trie.resetAnnotation();

        // Select next node.
        TrieNode node = pickNextNodeForExploration();

        // Enable only the nodes that are on the way to the selected node.
        if (node == null) {
            return null;
        }
        node.setExplorationNeeded(true);
        enablePathToNode(node, -1);
        return node;
    }

    private TrieNode pickNextNodeForExploration() {
        if (this.queue.isEmpty()) {
            return null;
        }
        TrieNode nodeWithHighestPriority = this.queue.poll();

        // Mark node as completed, i.e. never added to the priority queue again.
        nodeWithHighestPriority.setCompleted();

        // Remove this node from the instruction2Node mapping, since this node does not need longer any update.
        instruction2NodeMapping.get(nodeWithHighestPriority.getNextInstruction()).remove(nodeWithHighestPriority);

        return nodeWithHighestPriority;
    }

    @Override
    public boolean updateNode(TrieNode node) {
        // To update a node we have to remove and re-add it.
        this.queue.remove(node);

        // Check whether this node should be added to the queue or not.
        if (!node.hasPotentialForExploration()) {
            return false;
        }

        // Update Instruction to TrieNode mapping.
        Set<TrieNode> nodes = instruction2NodeMapping.get(node.getNextInstruction());
        if (nodes == null) {
            nodes = new HashSet<>();
            instruction2NodeMapping.put(node.getNextInstruction(), nodes);
        }
        nodes.add(node);

        /*
         * Add node to priority queue if metric value is known AND if this node has potential for exploration, i.e. if
         * there are potential children for this node.
         */
        this.queue.add(node);

        return true;
    }

    @Override
    public String getStatistics() {
        long instrCount = 0;
        for (Entry<Integer, Set<TrieNode>> entry : instruction2NodeMapping.entrySet()) {
            instrCount += entry.getValue().size();
        }
        return "" + queue.size() + "," + instrCount;
    }

    @Override
    public boolean isNodeLeftforAnalysis() {
        return !queue.isEmpty();
    }

    @Override
    public boolean addObservedChoice(Instruction instr, int choice) {
        boolean addedNewChoice = false;
        if (instr != null) {
            int instrHashCode = instr.hashCode();
            Set<Integer> choices = observedChoices.get(instrHashCode);
            if (choices == null) {
                choices = new HashSet<>();
                observedChoices.put(instrHashCode, choices);
            }
            addedNewChoice = choices.add(choice);

            /* If new choice then update prio queue. */
            if (addedNewChoice) {
                Set<TrieNode> correspondingNodesForInstruction = instruction2NodeMapping.get(instrHashCode);
                /* Might be null in the beginning, then there is no node that needs any update. */
                if (correspondingNodesForInstruction != null) {
                    for (TrieNode node : correspondingNodesForInstruction) {
                        updateNode(node);
                    }
                }
            }
        }
        return addedNewChoice;
    }

    @Override
    public Set<Integer> getObservedChoices(int instructionHashCode) {
        Set<Integer> choices = observedChoices.get(instructionHashCode);
        if (choices == null) {
            return new HashSet<>();
        } else {
            return choices;
        }
    }

}
