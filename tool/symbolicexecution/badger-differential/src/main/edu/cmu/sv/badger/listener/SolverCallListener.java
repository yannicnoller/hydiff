package edu.cmu.sv.badger.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.sequences.SequenceChoiceGenerator;

/**
 * This listener class records the number of solver calls 
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 */

public class SolverCallListener extends ListenerAdapter {

	int count; // number of solver calls, assuming one solver call 
			   // is made whenever state is advanced because of PCChoiceGenerator advancing
	
	private static final boolean DEBUG = false;

	public SolverCallListener(Config config, JPF jpf) throws Exception {
		count=0;
	}

	public void searchFinished(Search search) {
		if (DEBUG) {
			System.out.println(">>> searchFinished");
		}
		System.out.println("# solver calls: " + count);
	}


	public void stateAdvanced(Search search) {
		if (DEBUG) {
			System.out.println(">>> stateAdvanced");
		}

		ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
		if (DEBUG) {
			System.out.println("cg: " + cg);
		}

		// thread choice instead of pc choice
		if (cg instanceof ThreadChoiceGenerator) {
			return;
		}
		if (cg instanceof SequenceChoiceGenerator) {
			return;
		}

		if (cg instanceof PCChoiceGenerator) {
			int offset = ((PCChoiceGenerator) cg).getOffset();
			if (offset == 0) {
				return;
			}
		}

		count++;
	}
}
