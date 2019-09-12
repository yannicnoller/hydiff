package edu.cmu.sv.kelinci.regression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hub.se.cfg.CFGTarget;

/**
 * TODO
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public class CFGSummary {

	/* Resulting distance map. */
	private static Map<CFGTarget, Integer> targetDistances = new HashMap<>();
	private static Set<CFGTarget> targetTouched = new HashSet<>();

	public static void clear() {
		targetDistances = new HashMap<>();
		targetTouched = new HashSet<>();
	}

	public static void updateDistance(String targetMethodName, int targetSourceLineNumber, int distance) {
		CFGTarget target = new CFGTarget(targetMethodName, targetSourceLineNumber);
		
		/* Update observed distance to target if increased, i.e. decreased. */
		Integer previousDistance = targetDistances.get(target);
		if (previousDistance == null) {
			targetDistances.put(target, distance);
		} else {
			if (previousDistance.intValue() > distance) {
				targetDistances.put(target, distance);
			}
		}
		
		/* Record separately whether patch was touched */
		if (distance == 0) {
			targetTouched.add(target);
		}
	}
	
	public static Map<CFGTarget, Integer> getMinimumDistances() {
		return targetDistances;
	}

	public static Set<CFGTarget> getPatchesTouched() {
		return targetTouched;
	}
	
	public static boolean patchTouched() {
		return !targetTouched.isEmpty();
	}

}
