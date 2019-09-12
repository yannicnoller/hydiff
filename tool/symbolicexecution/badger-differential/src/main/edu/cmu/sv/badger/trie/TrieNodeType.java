package edu.cmu.sv.badger.trie;

/**
 * Help class for different trie node types
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 */

public enum TrieNodeType {
	
	REGULAR_NODE, // regular node
	UNSAT_NODE, // unsatisfiable node
	FRONTIER_NODE, // frontier node
    LEAF_NODE, // end of exection trace;
    PRUNED_NODE; // pruned because target not reachable
	
}


