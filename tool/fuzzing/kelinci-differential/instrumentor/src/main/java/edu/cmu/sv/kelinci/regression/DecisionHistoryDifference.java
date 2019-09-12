package edu.cmu.sv.kelinci.regression;

/**
 * Stores the merged decision histories as a tuple of distance value as integer
 * and the actual merged histories encoded as String.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 * 
 */
public class DecisionHistoryDifference {
	
	public int distance;
	public String mergedHistory;

	public DecisionHistoryDifference(int distance, String mergedHistory) {
		this.distance = distance;
		this.mergedHistory = mergedHistory;
		
		mergedHistory.hashCode();
	}
	
	public int getDistance() {
		return distance;
	}
	
	public int getEncodedValue() {
		return mergedHistory.hashCode();
	}


	/**
	 * Merges two decision histories encoded in a String.
	 * 
	 * @param dec1
	 *            - boolean[]
	 * @param dec2
	 *            - boolean[]
	 * @return DecisionDifference (distance, StringMerge)
	 */
	public static DecisionHistoryDifference createDecisionHistoryDifference(boolean[] dec1, boolean[] dec2) {
		String mergedDecisions = "";

		int smallerLength = Math.min(dec1.length, dec2.length);
		int distance = 0;

		for (int i = 0; i < smallerLength; i++) {
			if (!dec1[i] & !dec2[i]) {
				mergedDecisions += "0"; // both false
			} else if (dec1[i] & dec2[i]) {
				mergedDecisions += "1"; // both true
			} else if (!dec1[i] & dec2[i]) {
				mergedDecisions += "2"; // false -> true
				distance++;
			} else {
				mergedDecisions += "3"; // true -> false
				distance++;
			}
		}

		distance += Math.abs(dec1.length - dec2.length);

		return new DecisionHistoryDifference(distance, mergedDecisions);
	}
	
}
