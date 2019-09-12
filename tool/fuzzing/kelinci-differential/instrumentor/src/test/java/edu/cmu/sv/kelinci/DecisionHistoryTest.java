package edu.cmu.sv.kelinci;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;

import edu.cmu.sv.kelinci.regression.DecisionHistory;

public class DecisionHistoryTest {

	@Test
	public void testSimpleWorkflow() {
		Label l1 = new Label();
		DecisionHistory.addDecision(l1.hashCode());
		boolean[] is_1 = DecisionHistory.getDecisions();
		boolean[] shall_1 = { false };
		Assert.assertArrayEquals(shall_1, is_1);

		DecisionHistory.flipLastDecision(l1.hashCode());
		boolean[] is_2 = DecisionHistory.getDecisions();
		boolean[] shall_2 = { true };
		Assert.assertArrayEquals(shall_2, is_2);

		Label l2 = new Label();
		DecisionHistory.addDecision(l2.hashCode());
		boolean[] is_3 = DecisionHistory.getDecisions();
		boolean[] shall_3 = { true, false };
		Assert.assertArrayEquals(shall_3, is_3);

		Label l3 = new Label();
		DecisionHistory.addDecision(l3.hashCode());
		boolean[] is_4 = DecisionHistory.getDecisions();
		boolean[] shall_4 = { true, false, false };
		Assert.assertArrayEquals(shall_4, is_4);

		DecisionHistory.flipLastDecision(l2.hashCode());
		boolean[] is_5 = DecisionHistory.getDecisions();
		boolean[] shall_5 = { true, false, false };
		Assert.assertArrayEquals(shall_5, is_5);

		DecisionHistory.flipLastDecision(l3.hashCode());
		boolean[] is_6 = DecisionHistory.getDecisions();
		boolean[] shall_6 = { true, false, true };
		Assert.assertArrayEquals(shall_6, is_6);

		DecisionHistory.clear();
		boolean[] is_7 = DecisionHistory.getDecisions();
		boolean[] shall_7 = {};
		Assert.assertArrayEquals(shall_7, is_7);
	}

	@Test
	public void testEmptyDecisions() {
		/*
		 * Internal structure of DecisionHistory might lead to
		 * ArrayIndexOutOfBoundsException if it checks the "last" element, which is
		 * non-existent for an empty decision history. So we expect here no exception;
		 * if there is one, then there is an error.
		 */
		DecisionHistory.clear();
		DecisionHistory.flipLastDecision(0);
	}
	
}
