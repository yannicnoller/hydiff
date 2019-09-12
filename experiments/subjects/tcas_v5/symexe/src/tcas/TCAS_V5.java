package tcas;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class TCAS_V5 {

    private static final int OLEV = 600;/* in feets/minute */
    private static final int MAXALTDIFF = 600; /* max altitude difference in feet */
    private static final int MINSEP = 300; /* min separation in feet */
    private static final int NOZCROSS = 100; /* in feet */

	int Cur_Vertical_Sep;
	boolean High_Confidence;
	boolean Two_of_Three_Reports_Valid;

	int Own_Tracked_Alt;
	int Own_Tracked_Alt_Rate;
	int Other_Tracked_Alt;

	int Alt_Layer_Value;		/* 0, 1, 2, 3 */
	int[] Positive_RA_Alt_Thresh = new int[4];

	int Up_Separation;
	int Down_Separation;

					/* state variables */
	int Other_RAC;			/* NO_INTENT, DO_NOT_CLIMB, DO_NOT_DESCEND */
    private static final int NO_INTENT = 0;
    private static final int DO_NOT_CLIMB = 1;
    private static final int DO_NOT_DESCEND = 2;

	int Other_Capability;		/* TCAS_TA, OTHER */
	private static final int TCAS_TA = 1;
	private static final int OTHER = 2;

	boolean Climb_Inhibit;		/* true/false */

	private static final int UNRESOLVED = 0;
	private static final int UPWARD_RA = 1;
	private static final int DOWNWARD_RA = 2;

	void initialize(){
	    Positive_RA_Alt_Thresh[0] = 400;
	    Positive_RA_Alt_Thresh[1] = 500;
	    Positive_RA_Alt_Thresh[2] = 640;
	    Positive_RA_Alt_Thresh[3] = 740;
	}

	private int ALIM (){
	 return Positive_RA_Alt_Thresh[Alt_Layer_Value];
	}

	private int Inhibit_Biased_Climb (){
	    return (Climb_Inhibit ? Up_Separation + NOZCROSS : Up_Separation);
	}

	private boolean Non_Crossing_Biased_Climb(){
	    boolean upward_preferred;
	    int upward_crossing_situation;
	    boolean result;

	    upward_preferred = Inhibit_Biased_Climb() > Down_Separation;
	    if (upward_preferred){
	    	result = !(Own_Below_Threat()) || ((Own_Below_Threat()) && (!(Down_Separation >= ALIM())));
	    }else{	
	    	result = Own_Above_Threat() && (Cur_Vertical_Sep >= MINSEP) && (Up_Separation >= ALIM());
	    }
	    return result;
	}

	private boolean Non_Crossing_Biased_Descend(){
	    boolean upward_preferred;
	    int upward_crossing_situation;
	    boolean result;

	    upward_preferred = Inhibit_Biased_Climb() > Down_Separation;
	    if (upward_preferred){
	    	result = Own_Below_Threat() && (Cur_Vertical_Sep >= MINSEP) && (Down_Separation >= ALIM());
	    }else{
	    	result = !(Own_Above_Threat()) || ((Own_Above_Threat()) && (Up_Separation >= ALIM()));
	    }
	    return result;
	}
	
	private boolean Own_Below_Threat(){
	    return (Own_Tracked_Alt < Other_Tracked_Alt);
	}

	private boolean Own_Above_Threat(){
	    return (Other_Tracked_Alt < Own_Tracked_Alt);
	}

	int alt_sep_test(){
	    boolean enabled, tcas_equipped, intent_not_known;
	    boolean need_upward_RA, need_downward_RA;
	    int alt_sep;

	    boolean tmp = change((Cur_Vertical_Sep > MAXALTDIFF), true);
	    enabled = High_Confidence && (Own_Tracked_Alt_Rate <= OLEV) && tmp; /* && (Cur_Vertical_Sep > MAXALTDIFF); missing code change: code removed*/ 
	    tcas_equipped = Other_Capability == TCAS_TA;
	    intent_not_known = Two_of_Three_Reports_Valid && Other_RAC == NO_INTENT;
	    
	    alt_sep = UNRESOLVED;
	    
	    if (enabled && ((tcas_equipped && intent_not_known) || !tcas_equipped)){
			need_upward_RA = Non_Crossing_Biased_Climb() && Own_Below_Threat();
			need_downward_RA = Non_Crossing_Biased_Descend() && Own_Above_Threat();
			if (need_upward_RA && need_downward_RA)
		        /* unreachable: requires Own_Below_Threat and Own_Above_Threat
		           to both be true - that requires Own_Tracked_Alt < Other_Tracked_Alt
		           and Other_Tracked_Alt < Own_Tracked_Alt, which isn't possible */
			    alt_sep = UNRESOLVED;
			else if (need_upward_RA)
			    alt_sep = UPWARD_RA;
			else if (need_downward_RA)
			    alt_sep = DOWNWARD_RA;
			else
			    alt_sep = UNRESOLVED;
	    }
	    
	    return alt_sep;
	}
	
	//alternate entry point for test purposes
	public int startTcas(int cvs, boolean hc, boolean ttrv, int ota, int otar,
				int otTa, int alv, int upS, int dS, int oRAC, int oc, boolean ci){
		Cur_Vertical_Sep = cvs;
		High_Confidence = hc;
		Two_of_Three_Reports_Valid = ttrv;
		Own_Tracked_Alt = ota;
		Own_Tracked_Alt_Rate = otar;
		Other_Tracked_Alt = otTa;
		Alt_Layer_Value = alv;
		Up_Separation = upS;
		Down_Separation = dS;
		Other_RAC = oRAC;
		Other_Capability = oc;
		Climb_Inhibit = ci;
    	initialize();
        return alt_sep_test();
	}
	
    public static void main (String[] args) {

        TCAS_V5 tcas = new TCAS_V5();
        if (args.length == 12){
        	tcas.Cur_Vertical_Sep = Integer.parseInt(args[0]);
        	if (args[1].equalsIgnoreCase("0"))
        		tcas.High_Confidence = false;
        	else
        		tcas.High_Confidence = true;
        	if (args[2].equalsIgnoreCase("0"))
        		tcas.Two_of_Three_Reports_Valid = false;
        	else
        		tcas.Two_of_Three_Reports_Valid = true;
        	tcas.Own_Tracked_Alt = Integer.parseInt((args[3]));
        	tcas.Own_Tracked_Alt_Rate = Integer.parseInt(args[4]);
        	tcas.Other_Tracked_Alt = Integer.parseInt(args[5]);
        	tcas.Alt_Layer_Value = Integer.parseInt(args[6]);
        	tcas.Up_Separation = Integer.parseInt(args[7]);
        	tcas.Down_Separation = Integer.parseInt(args[8]);
        	tcas.Other_RAC = Integer.parseInt(args[9]);
        	tcas.Other_Capability = Integer.parseInt(args[10]);
        	if (args[11].equalsIgnoreCase("0"))
        		tcas.Climb_Inhibit = false;
        	else
        		tcas.Climb_Inhibit = true;
        }else if (args.length == 0){
        	
        }else{
        	System.out.println("Invalid number of args");
        }
        tcas.initialize();
        int res = tcas.alt_sep_test();
        System.out.println(">>>>>>results: " + res);
    }
/*
	main(argc, argv)
	int argc;
	char *argv[];
	{
	    if(argc < 13)
	    {
		fprintf(stdout, "Error: Command line arguments are\n");
		fprintf(stdout, "Cur_Vertical_Sep, High_Confidence, Two_of_Three_Reports_Valid\n");
		fprintf(stdout, "Own_Tracked_Alt, Own_Tracked_Alt_Rate, Other_Tracked_Alt\n");
		fprintf(stdout, "Alt_Layer_Value, Up_Separation, Down_Separation\n");
		fprintf(stdout, "Other_RAC, Other_Capability, Climb_Inhibit\n");
		exit(1);
	    }
	    initialize();
	    Cur_Vertical_Sep = atoi(argv[1]);
	    High_Confidence = atoi(argv[2]);
	    Two_of_Three_Reports_Valid = atoi(argv[3]);
	    Own_Tracked_Alt = atoi(argv[4]);
	    Own_Tracked_Alt_Rate = atoi(argv[5]);
	    Other_Tracked_Alt = atoi(argv[6]);
	    Alt_Layer_Value = atoi(argv[7]);
	    Up_Separation = atoi(argv[8]);
	    Down_Separation = atoi(argv[9]);
	    Other_RAC = atoi(argv[10]);
	    Other_Capability = atoi(argv[11]);
	    Climb_Inhibit = atoi(argv[12]);

	    fprintf(stdout, "%d\n", alt_sep_test());
	    exit(0);
	}*/

}
