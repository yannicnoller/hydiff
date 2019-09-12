package edu.cmu.sv.badger.trie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.sv.badger.analysis.TrieAnalyzer;

/**
 * Trie data structure with the following info stored: methodName, bytecode offset, and choice.
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 *         Modified for WCA by Yannic Noller <nolleryc@gmail.com> - YN
 */

public class Trie implements Serializable {

    private static final long serialVersionUID = 7526472295622776147L;

    private AtomicInteger idGenerator = new AtomicInteger(-1);

    public int getNextId() {
        return idGenerator.incrementAndGet();
    }

    private TrieNode root;

    private Set<TrieNode> enabledNodes;

    private TrieAnalyzer trieAnalyzer;

    public Trie(TrieAnalyzer trieAnalyzer) {
        this.enabledNodes = new HashSet<>();
        this.trieAnalyzer = trieAnalyzer;
    }

    public TrieNode getRoot() {
        return root;
    }

    public void setRoot(TrieNode root) {
        this.root = root;
    }

    public void addEnabledNode(TrieNode node) {
        this.enabledNodes.add(node);
    }

    /**
     * Adds, updates or removes node in priority queue.
     * 
     * @param node
     *            - TrieNode
     */
    public void updateNode(TrieNode node) {
        trieAnalyzer.updateNode(node);
    }

    /**
     * Compact the trie based on enabled nodes, i.e. removes all disabled nodes.
     */
    public void compact() {
        root.compact();
    }

    public void resetAnnotation() {
        for (TrieNode node : this.enabledNodes) {
            node.resetAnnotation();
        }
        enabledNodes = new HashSet<>();
    }

    public static boolean storeTrie(Trie trie, String filePath) {
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(trie);
            oos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void storeTrieAsDot(Trie trie, String filePath, Integer maxDepth) {
        TriePrintToDot tp = new TriePrintToDot(trie);
        tp.print(filePath, maxDepth);
    }

    public static Trie loadTrie(String filePath) {
        Trie trie = null;
        try {
            FileInputStream fin = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fin);
            trie = (Trie) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e1) {
            return null;
        } catch (Exception e) {
            System.err.println("something wrong with trie de-serializing");
            e.printStackTrace();
        }
        return trie;
    }

    public String getStatistics() {
        return idGenerator.get() + "," + trieAnalyzer.getStatistics();
    }

    public int getNumberOfPaths() {
        return countNumberOfPaths(root);

    }

    private int countNumberOfPaths(TrieNode node) {
        if (node.getType().equals(TrieNodeType.LEAF_NODE)) {
            return 1;
        } else {
            int numberOfPaths = 0;
            for (TrieNode child : node.getChildren()) {
                numberOfPaths += countNumberOfPaths(child);
            }
            return numberOfPaths;
        }
    }

    public TrieAnalyzer getAnalyzer() {
        return this.trieAnalyzer;
    }

}
