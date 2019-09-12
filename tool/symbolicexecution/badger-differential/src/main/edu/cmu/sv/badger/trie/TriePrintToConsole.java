package edu.cmu.sv.badger.trie;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * Help class to print a trie to the console
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 */

public class TriePrintToConsole {
	Trie trie;
	
	public void loadTrie(){
		// de-serialize the stored trie from the disk
		try {
			FileInputStream fin = new FileInputStream("trie_ex.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			trie = (Trie) ois.readObject();
			ois.close();
		} catch (Exception e) {
			System.err.println("something wrong with trie de-serializing");
			e.printStackTrace();
		}

	}
	
	public void print(){
		assert(trie!=null);
		TrieNode n = trie.getRoot();
		if(n == null){
			System.out.println("Trie is null");
			return;
		}
		printNode(n);
	}
	
	static void printNode(TrieNode n){
		System.out.println("\n Node " + n.hashCode());
		System.out.println(">>> choice: " + n.getChoice());
		System.out.println(">>> offset: " + n.getOffset());
		System.out.println(">>> methodName: " + n.getMethodName());
		System.out.println(">>> type: " + n.getType());
		
		System.out.println("");
		
		//print the children's children recursively
		for(TrieNode child: n.getChildren()){
			printNode(child);			
		}
	}
	
	public static void main(String[] args){
		TriePrintToConsole tp = new TriePrintToConsole();
		tp.loadTrie();
		tp.print();
	}

	
}
