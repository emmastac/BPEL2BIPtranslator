package main;


public class Preferences {
	
	private static String HOME = System.getProperty("user.dir");
	
	/* absolute input paths */
	private static String BIPprojectsPath = HOME+"/translated2";
	private static String BPELprojectsPath = HOME + "/bpelHome";
	
	/* absolute paths for output files */
	private static String BPELprojectsCfgPath = BPELprojectsPath+"/prog_cfg.txt";
	private static String BIPprojectsCfgPath = BIPprojectsPath+"/prog_cfg_out.txt";
	
	/* relative paths for output */
	private static String BIPexplorationOutputPath = "/exploreOut.txt";
	
	private static boolean forExecution = false;
	

	
	public static String getBIPprojectsPath( ) {
	
		return BIPprojectsPath;
	}
	
	
	public static void setHOME( String home ) {
	
		HOME = home;
		BIPprojectsPath = HOME+"/translated2";
		BPELprojectsPath = HOME + "/bpelHome";
		
		BPELprojectsCfgPath = BPELprojectsPath+"/prog_cfg.txt";
		BIPprojectsCfgPath = BIPprojectsPath+"/prog_cfg_out.txt";
	}


	
	
	public static boolean isForExecution( ) {
	
		return forExecution;
	}


	
	public static void setForExecution( boolean forExecution ) {
	
		Preferences.forExecution = forExecution;
	}


	public static void setBIPprojectsPath( String bIPprojectsPath ) {
	
		BIPprojectsPath = bIPprojectsPath;
	}

	
	public static String getBPELprojectsPath( ) {
	
		return BPELprojectsPath;
	}

	
	public static void setBPELprojectsPath( String bPELprojectsPath ) {
	
		BPELprojectsPath = bPELprojectsPath;
	}

	
	public static String getBPELprojectsCfgPath( ) {
	
		return BPELprojectsCfgPath;
	}

	
	public static void setBPELprojectsCfgPath( String bPELprojectsCfgPath ) {
	
		BPELprojectsCfgPath = bPELprojectsCfgPath;
	}

	
	public static String getBIPprojectsCfgPath( ) {
	
		return BIPprojectsCfgPath;
	}

	
	public static void setBIPprojectsCfgPath( String bIPprojectsCfgPath ) {
	
		BIPprojectsCfgPath = bIPprojectsCfgPath;
	}

	
	public static String getBIPexplorationOutputPath( ) {
	
		return BIPexplorationOutputPath;
	}

	
	public static void setBIPexplorationOutputPath( String bIPexplorationOutputPath ) {
	
		BIPexplorationOutputPath = bIPexplorationOutputPath;
	}
	
	

}
