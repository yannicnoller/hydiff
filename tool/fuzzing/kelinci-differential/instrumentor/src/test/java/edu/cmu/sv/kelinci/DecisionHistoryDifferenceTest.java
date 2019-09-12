package edu.cmu.sv.kelinci;

import org.junit.Assert;
import org.junit.Test;

import edu.cmu.sv.kelinci.regression.DecisionHistoryDifference;

public class DecisionHistoryDifferenceTest {

	@Test
	public void testCreateDecisionHistoryDifference() {

		boolean[] a_1 = {};
		boolean[] b_1 = {};
		DecisionHistoryDifference diff_1 = DecisionHistoryDifference.createDecisionHistoryDifference(a_1, b_1);
		Assert.assertTrue(diff_1.mergedHistory.isEmpty());
		Assert.assertEquals(0, diff_1.getDistance());

		boolean[] a_2 = { true };
		boolean[] b_2 = {};
		DecisionHistoryDifference diff_2 = DecisionHistoryDifference.createDecisionHistoryDifference(a_2, b_2);
		Assert.assertTrue(diff_2.mergedHistory.isEmpty());
		Assert.assertEquals(1, diff_2.getDistance());

		boolean[] a_3 = { true };
		boolean[] b_3 = { false };
		DecisionHistoryDifference diff_3 = DecisionHistoryDifference.createDecisionHistoryDifference(a_3, b_3);
		Assert.assertEquals("3", diff_3.mergedHistory);
		Assert.assertEquals(1, diff_3.getDistance());

		boolean[] a_4 = { true, false, true };
		boolean[] b_4 = { false, false, true };
		DecisionHistoryDifference diff_4 = DecisionHistoryDifference.createDecisionHistoryDifference(a_4, b_4);
		Assert.assertEquals("301", diff_4.mergedHistory);
		Assert.assertEquals(1, diff_4.getDistance());

		boolean[] a_5 = { true, false, true };
		boolean[] b_5 = { true, true, false };
		DecisionHistoryDifference diff_5 = DecisionHistoryDifference.createDecisionHistoryDifference(a_5, b_5);
		Assert.assertEquals("123", diff_5.mergedHistory);
		Assert.assertEquals(2, diff_5.getDistance());

		boolean[] a_6 = { true, true, false, true };
		boolean[] b_6 = { true, false, true };
		DecisionHistoryDifference diff_6 = DecisionHistoryDifference.createDecisionHistoryDifference(a_6, b_6);
		Assert.assertEquals("132", diff_6.mergedHistory);
		Assert.assertEquals(3, diff_6.getDistance());

	}

}
