package edu.cmu.sv.kelinci;

import org.junit.Assert;
import org.junit.Test;

import edu.cmu.sv.kelinci.regression.OutputSummary;

public class OutputSummaryTest {

	@Test
	public void testIsDifferent() {
		
		OutputSummary os1 = new OutputSummary(0, 0);
		Assert.assertFalse(os1.isDifferent());
		
		OutputSummary os2 = new OutputSummary(0, 1);
		Assert.assertTrue(os2.isDifferent());
		
		OutputSummary os3 = new OutputSummary(1, 123);
		Assert.assertTrue(os3.isDifferent());
		
		OutputSummary os4 = new OutputSummary(1, 0);
		Assert.assertTrue(os4.isDifferent());
		
	}

}
