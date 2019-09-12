package edu.cmu.sv.badger.trie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Help class to print a trie to a dot graph
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 *         extended for new elements of TrieNode by Yannic Noller <nolleryc@gmail.com> - YN
 * 
 */

public class TriePrintToDot {
    Trie trie;

    public static boolean printInputSizeInfo = false;
    public static boolean printShadowInfo = false;

    public TriePrintToDot() {

    }

    public TriePrintToDot(Trie trie) {
        this.trie = trie;
    }

    public void loadTrie(String trieName) {
        // de-serialize the stored trie from the disk
        try {
            FileInputStream fin = new FileInputStream(trieName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            trie = (Trie) ois.readObject();
            ois.close();
        } catch (Exception e) {
            System.err.println("something wrong with trie de-serializing");
            e.printStackTrace();
        }
    }

    public void print(String fileName, Integer maxDepth) {
        Writer output = null;
        File file = new File(fileName);
        try {
            file.createNewFile();
            output = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            System.err.println("error while creating the file to write");
            e.printStackTrace();
        }
        try {
            output.write("digraph \"\" { \n");
            if (null != trie.getRoot()) {
                printTrieNodesAndEdges(trie.getRoot(), output, maxDepth);
            }
            output.write("}");
        } catch (IOException e) {
            System.err.println("Error while writing to the XML file");
            e.printStackTrace();
        }

        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateNodeStringRepresentation(TrieNode node) {
        DecimalFormat fm = new DecimalFormat("#.00");
        String stringRepresentation = "";
        if (node.needsExploration()) {
            if (node.getDepth() == 0) {// root node
                stringRepresentation = (node.hashCode()
                        + "[ color=\"lightblue\" style=\"filled\" fillcolor=\"green\" label=\"id=" + node.getId() + ", "
                        + node.getMethodName() + ":" + node.getLineNumber() + ", \n bc=" + node.getBytcode()
                        + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            } else {
                stringRepresentation = (node.hashCode() + "[ color=\"green\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", choice=" + node.getChoice() + "\n\n" + node.getMethodName() + ":"
                        + node.getLineNumber() + ", offset=" + node.getOffset() + ",  \nbc="
                        + (node.getBytcode() == -1 ? "?" : node.getBytcode()) + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            }
        } else if (node.isEnabled()) {
            if (node.getDepth() == 0) {// root node
                stringRepresentation = (node.hashCode() + "[ color=\"lightblue\" style=\"filled\" label=\"id="
                        + node.getId() + ", " + node.getMethodName() + ":" + node.getLineNumber() + ", \n bc="
                        + node.getBytcode() + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            } else {
                stringRepresentation = (node.hashCode() + "[ color=\"red\" label=\"" + "id=" + node.getId()
                        + ", choice=" + node.getChoice() + "\n\n" + node.getMethodName() + ":" + node.getLineNumber()
                        + ", offset=" + node.getOffset() + ",  \nbc="
                        + (node.getBytcode() == -1 ? "?" : node.getBytcode()) + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            }
        } else {
            if (node.getDepth() == 0) {// root node
                stringRepresentation = (node.hashCode() + "[ color=\"lightblue\" style=\"filled\" label=\"id="
                        + node.getId() + ", " + node.getMethodName() + ":" + node.getLineNumber() + ", \n bc="
                        + node.getBytcode() + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            } else if (node.getType().equals(TrieNodeType.UNSAT_NODE)) {
                stringRepresentation = (node.hashCode() + "[ color=\"yellow\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", choice=" + node.getChoice());
                if (printShadowInfo) {
                    stringRepresentation += "\n\n" + "diffPath=" + node.isOnDiffPath() + ", \n shadowExecMode="
                            + node.getShadowExecutionMode();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            } else if (node.getType().equals(TrieNodeType.PRUNED_NODE)) {
                stringRepresentation = (node.hashCode() + "[ color=\"orange\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", choice=" + node.getChoice());
                if (printShadowInfo) {
                    stringRepresentation += "\n\n" + "diffPath=" + node.isOnDiffPath() + ", \n shadowExecMode="
                            + node.getShadowExecutionMode() + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            } else if (node.getType().equals(TrieNodeType.FRONTIER_NODE)) { /* search constraint hit */
                stringRepresentation = (node.hashCode() + "[ color=\"pink\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", choice=" + node.getChoice() + "\n\n" + node.getMethodName() + ":"
                        + node.getLineNumber() + ", offset=" + node.getOffset() + ",  \nbc="
                        + (node.getBytcode() == -1 ? "?" : node.getBytcode()) + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            } else if (node.getType().equals(TrieNodeType.LEAF_NODE)) {
                stringRepresentation = (node.hashCode() + "[ color=\"gray\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", choice=" + node.getChoice() + "\n\n" + "score="
                        + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue())) + "/"
                        + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue())));
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";

            } else {
                stringRepresentation = (node.hashCode() + "[ label=\"" + "id=" + node.getId() + ", choice="
                        + node.getChoice() + "\n\n" + node.getMethodName() + ":" + node.getLineNumber() + ", offset="
                        + node.getOffset() + ",  \nbc=" + (node.getBytcode() == -1 ? "?" : node.getBytcode())
                        + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getOldMetricValue() == null ? "?" : fm.format(node.getOldMetricValue()))
                        + "/" + (node.getNewMetricValue() == null ? "?" : fm.format(node.getNewMetricValue()))
                        + ", \n newBranches=" + node.canExposeNewBranches());
                if (printShadowInfo) {
                    stringRepresentation += ", \n diffExpr=" + node.containsDiffExpression() + ", \n diffPath="
                            + node.isOnDiffPath() + ", \n shadowExecMode=" + node.getShadowExecutionMode()
                            + ", \n patchDist=" + node.getPatchDistance();
                }
                if (printInputSizeInfo) {
                    stringRepresentation += ", \n inputSizes=" + Arrays.toString(node.getInputSizes());
                }
                stringRepresentation += (node.getPropertyViolation() ? "\n\n PROPERTY VIOLATED" : "") + "\"];\n";
            }
        }
        return stringRepresentation;
    }

    public void printTrieNodesAndEdges(TrieNode node, Writer output, Integer maxDepth) throws IOException {
        List<TrieNode> nodesToPrint = new ArrayList<>();
        nodesToPrint.add(node);
        while (!nodesToPrint.isEmpty()) {
            TrieNode currentNode = nodesToPrint.remove(0);

            /* Print current node. */
            output.write(generateNodeStringRepresentation(currentNode));

            /* Print all edges from this node to its children. */
            List<TrieNode> children = currentNode.getChildren();
            for (TrieNode child : children) {
                if (child.isEnabled()) {
                    output.write(currentNode.hashCode() + "->" + child.hashCode() + "[ color=\"red\"];\n");
                } else {
                    output.write(currentNode.hashCode() + "->" + child.hashCode() + ";\n");
                }
            }

            /* Add all children the be processed. */
            if (maxDepth == null || currentNode.getDepth() < maxDepth) {
                nodesToPrint.addAll(children);
            }
        }
    }
}
