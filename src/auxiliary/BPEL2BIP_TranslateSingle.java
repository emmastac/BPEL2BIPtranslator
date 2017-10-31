package auxiliary;

import java.io.File;

import main.Preferences;
import translator.BPELTransformer;

public class BPEL2BIP_TranslateSingle {

	public static String BIP_PROJECTS_PATH = Preferences.getBIPprojectsPath();
	public static String BPEL_PROJECTS_PATH = System.getProperty( "user.dir" ) + "/BPELPrograms";
	public static int DEBUG = 0;
	public static boolean PerformanceExperiment = false;
	
	
	public static String BPELsourceProjectsPath = BPEL_PROJECTS_PATH+"/BuyBook_53";
	public static String BPELsingleProjectsPath = BPELsourceProjectsPath + "/BuyBook.bpel";
	

	//	public static void main( String [ ] args ) throws Exception {
	//
	//		singleTranslateMain();
	//	}

	public static void singleTranslateMain( ) throws Exception {

		BPELTransformer bpel2bip = new BPELTransformer( true , null );
		bpel2bip.transformFile( new File( BPELsingleProjectsPath ) , BPELsourceProjectsPath , BIP_PROJECTS_PATH );
	}


	

}
