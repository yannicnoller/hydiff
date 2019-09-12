package gov.nasa.jpf.symbc.numeric;

/*
 * Object which stores an integer representing the execution mode
 * 0 -> execution mode: new
 * 1 -> execution mode: old
 * 
 * used for if(execute(old)) and if(execute(new)) annotation
 */

public class ExecExpression {
	int executionMode;
	
	public ExecExpression(int em){
		assert(em==0 || em==1);
		executionMode = em;
	}
	
	public int getExecutionMode(){
		return executionMode;
	}
}
