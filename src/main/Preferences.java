package main;


public class Preferences {
	
	/* absolute input paths */
	public static String BIPprojectsPath = System.getProperty("user.dir")+"/translated2";
	public static String BPELprojectsPath = System.getProperty( "user.dir" ) + "/bpelHome";
	
	/* absolute paths for output files */
	public static String BPELprojectsCfgPath = BPELprojectsPath+"/prog_cfg.txt";
	public static String BIPprojectsCfgPath = BIPprojectsPath+"/prog_cfg_out.txt";
	
	/* relative paths for output */
	public static String BIexplorationOutputPath = "/exploreOut.txt";

}
