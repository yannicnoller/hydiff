package gov.nasa.jpf.symbc.numeric;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;

public class ShadowPCChoiceGenerator extends PCChoiceGenerator{
	private Instruction endOfBlock;
	private MethodInfo methodInfo;
	
	public ShadowPCChoiceGenerator(int min, int max, int delta, Instruction instr, MethodInfo mi){
		super(min,max,delta);
		this.endOfBlock = instr;
		this.methodInfo = mi;
	}
	
	public ShadowPCChoiceGenerator(int min, int max, Instruction instr, MethodInfo mi){
		super(min,max,1);
		this.endOfBlock = instr;
		this.methodInfo = mi;
	}
	
	public ShadowPCChoiceGenerator(int size, Instruction instr, MethodInfo mi){
		super(0,size-1);
		this.endOfBlock = instr;
		this.methodInfo = mi;
	}
	
	public Instruction getEndInstruction(){
		return this.endOfBlock;
	}	
	
	public MethodInfo getMethodInfo(){
		return this.methodInfo;
	}
}
