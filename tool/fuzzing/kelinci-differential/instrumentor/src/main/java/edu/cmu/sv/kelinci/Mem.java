package edu.cmu.sv.kelinci;

/**
 * Class to record branching, analogous to the shared memory in AFL.
 * 
 * Because we measure inside a particular target method, we need a way to
 * start/stop measuring. Therefore, the array can be cleared.
 * 
 * @author rodykers
 *
 */
public class Mem {
	public static final int BUFFER_SIZE = 65536; // the measurement data block
	public static int PARAM_SIZE = 4 * Long.BYTES + 4 * Integer.BYTES + 3 * Byte.BYTES; // 12 params = 59 bits
	public static int NUMBER_OF_DISTANCE_TARGETS = 0;
	public static int offset = BUFFER_SIZE;
	int s = Character.BYTES;

	public static byte[] mem = new byte[BUFFER_SIZE + PARAM_SIZE];
	
	public static void resetMemorySize(int numberOfDistanceTargets) {
		NUMBER_OF_DISTANCE_TARGETS = numberOfDistanceTargets;
		PARAM_SIZE = 4 * Long.BYTES + 4 * Integer.BYTES + 3 * Byte.BYTES + NUMBER_OF_DISTANCE_TARGETS * Integer.BYTES;
		mem = new byte[BUFFER_SIZE + PARAM_SIZE];
		clear();
	}
	
	public static int prev_location = 0;

	/**
	 * Holds the current cost measured by the configured instrumentation mode, e.g.,
	 * by counting every bytecode jump instruction (see
	 * {@link edu.cmu.sv.kelinci.instrumentor.Options.InstrumentationMode}).
	 */
	public static long instrCost = 0;

	/**
	 * Clears the current measurements.
	 */
	public static void clear() {
		for (int i = 0; i < BUFFER_SIZE + PARAM_SIZE; i++)
			mem[i] = 0;
		instrCost = 0L;
		offset = BUFFER_SIZE;
	}

	public static void appendLong(long value) {
		for (int i = 0; i < Long.BYTES; i++) {
			mem[offset + i] = (byte) ((value >> i * Long.BYTES) & 255);
		}
		offset += Long.BYTES;
	}

	public static void appendInteger(int value) {
		for (int i=0; i < Integer.BYTES; i++) {
			mem[offset + i] = (byte) ((value >> i * Long.BYTES) & 255);; 
		}
		offset += Integer.BYTES;
	}

	public static void appendBoolean(boolean value) {
		mem[offset] = (byte) (((value ? 1 : 0) >> 0) & 255);
		offset += 1;
	}

	public static void printtest() {
		int localOffset = BUFFER_SIZE;

		for (int i = 0; i < 4; i++) {
			String name;
			switch (i) {
			case 1:
				name = "max_heap:   ";
				break;
			case 2:
				name = "instr:      ";
				break;
			case 3:
				name = "user:       ";
				break;
			default:
				name = "runtime:    ";
				break;
			}
			System.out.print(name);
			for (int j = 0; j < Long.BYTES; j++) {
				System.out.print(String.format("%02X", mem[localOffset + j]));
			}
			System.out.println();
			localOffset += Long.BYTES;
		}

		System.out.print("outputDiff: ");
		System.out.println(String.format("%02X", mem[localOffset]));
		localOffset += Byte.BYTES;
		
		System.out.print("newCrash:   ");
		System.out.println(String.format("%02X", mem[localOffset]));
		localOffset += Byte.BYTES;

		System.out.print("out1Enc:    ");
		for (int j = 0; j < Integer.BYTES; j++) {
			System.out.print(String.format("%02X", mem[localOffset + j]));
		}
		System.out.println();
		localOffset += Integer.BYTES;
		
		System.out.print("out2Enc:    ");
		for (int j = 0; j < Integer.BYTES; j++) {
			System.out.print(String.format("%02X", mem[localOffset + j]));
		}
		System.out.println();
		localOffset += Integer.BYTES;
		
		System.out.print("decDiffDist:");
		for (int j = 0; j < Integer.BYTES; j++) {
			System.out.print(String.format("%02X", mem[localOffset + j]));
		}
		System.out.println();
		localOffset += Integer.BYTES;
		
		System.out.print("decDiffEnc: ");
		for (int j = 0; j < Integer.BYTES; j++) {
			System.out.print(String.format("%02X", mem[localOffset + j]));
		}
		System.out.println();
		localOffset += Integer.BYTES;
		
		System.out.print("patchTouch: ");
		System.out.println(String.format("%02X", mem[localOffset]));
		localOffset += Byte.BYTES;

		for (int i=0; i<NUMBER_OF_DISTANCE_TARGETS; i++) {
			System.out.print("distance" + i + ":  ");
			for (int j = 0; j < Integer.BYTES; j++) {
				System.out.print(String.format("%02X", mem[localOffset + j]));
			}
			System.out.println();
			localOffset += Integer.BYTES;
		}

	}

	/**
	 * Prints to stdout any cell that contains a non-zero value.
	 */
	public static void print() {
		for (int i = 0; i < BUFFER_SIZE; i++) {
			if (mem[i] != 0) {
				System.out.println(i + " -> " + mem[i]);
			}
		}
	}
}
