package gov.nasa.jpf.symbc.bytecode.shadow.util;

import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.symbc.bytecode.shadow.LCMP;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;

public class IFInstrSymbHelper {

	public static Instruction getNextInstructionAndSetPCChoiceLong(ThreadInfo ti, 
			   LCMP instr, 
			   Object op_v1,
			   Object op_v2,
			   Comparator firstComparator,
			   Comparator secondComparator,
			   Comparator thirdComparator) {
	
			throw new UnsupportedOperationException();
	}
	
	public static Instruction getNextInstructionAndSetPCChoiceReal(ThreadInfo ti, 
			   Instruction instr, 
			   Object op_v1,
			   Object op_v2,
			   Comparator firstComparator,
			   Comparator secondComparator,
			   Comparator thirdComparator) {
			throw new UnsupportedOperationException(instr.getSourceLine());
	}

	//handles symbolic integer if-instructions with a single operand
	public static Instruction getNextInstructionAndSetPCChoice(ThreadInfo ti,
															   IfInstruction instr,
															   Object op_v,
															   Comparator trueComparator,
															   Comparator falseComparator){
		throw new UnsupportedOperationException();

	}

}
