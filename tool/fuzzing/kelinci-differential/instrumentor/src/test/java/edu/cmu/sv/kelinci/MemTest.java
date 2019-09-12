package edu.cmu.sv.kelinci;

import org.junit.Test;

public class MemTest {

	@Test
	public void testAppend() {
		
		Mem.appendLong(1);
		Mem.appendLong(2);
		Mem.appendLong(3);
		Mem.appendLong(4);
		
		Mem.appendBoolean(false);
		Mem.appendBoolean(true);
		Mem.appendBoolean(true);
		
		Mem.printtest();
	}

}
