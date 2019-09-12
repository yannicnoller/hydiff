package edu.cmu.sv.kelinci.regression;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and manages the decisions made during execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 * 
 */
public class DecisionHistory {

	/* --- Handling and collecting of decision points --- */
	
	/**
	 * The idea is that initial each decision is false and they can be later flipped
	 * to true if necessary.
	 */
	public static class DecisionPoint {

		public int targetLabelHashCode;
		public boolean decision;

		public DecisionPoint(int targetLabelHashCode) {
			this.targetLabelHashCode = targetLabelHashCode;
			this.decision = false;
		}

		public void flip() {
			this.decision = true;
		}
	}
	
	private static List<DecisionPoint> decisions = new ArrayList<>();

	/**
	 * Adds a new decision for the given label and stores false as boolean value.
	 * 
	 * @param targetLabel - int hashcode of Label object
	 */
	public static void addDecision(int labelHashCode) {
		decisions.add(new DecisionPoint(labelHashCode));
	}

	/**
	 * Changes the last decision to true.
	 * 
	 * @param targetLabelHashCode - int hashcode of Label object
	 */
	public static void flipLastDecision(int targetLabelHashCode) {
		if (!decisions.isEmpty()) {
			DecisionPoint dp = decisions.get(decisions.size() - 1);
			if (dp.targetLabelHashCode == targetLabelHashCode) {
				dp.flip();
			}
		}
	}

	/**
	 * Returns all boolean values of the stored decisions.
	 * 
	 * @return Boolean[]
	 */
	public static boolean[] getDecisions() {
		boolean[] result = new boolean[decisions.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = decisions.get(i).decision;
		}
		return result;
	}

	public static void clear() {
		decisions = new ArrayList<>();
	}
	
}
