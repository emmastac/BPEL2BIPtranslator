package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import translator.BPELTransformer;

public class BPEL2BIP_Batch {

	public static String BIP_PROJECTS_PATH = Preferences.BIPprojectsPath;
	public static String BPEL_PROJECTS_PATH = Preferences.BPELprojectsPath;
	
	public static int DEBUG = 0;
	public static boolean PerformanceExperiment = true;
	
	public static String filename = Preferences.BPELprojectsCfgPath;
	public static String outname = Preferences.BIPprojectsCfgPath;
	

	public static void main( String [ ] args ) throws Exception {

		translateAllProjectsInFile( );
	}

	public static void translateAllProjectsInPath( ) throws Exception {

		String [ ] projectNames = new String [ ] { };
		int INIT = 0; // check INIT:6 , syntax
		// / bUY_BOOK 54: too few states
		int END = -1;

		BPELTransformer bpel2bip = new BPELTransformer( PerformanceExperiment , DEBUG, true, null );
		bpel2bip.NEW = 1;
		bpel2bip.transformBatchProjects( BIP_PROJECTS_PATH , BPEL_PROJECTS_PATH , projectNames , INIT , END );
	}
	
	public static void translateAllProjectsInFile( ) throws Exception {

		if( !Files.exists( Paths.get( filename ) )) {
			System.out.println("Please provide a BPEL projects' configuration file.");
			System.out.println("Exit tool.");
			
			System.exit(1);
            return;
		}
		
		ArrayList<String> BPELprojectPaths = new ArrayList<String>( );

		Files.createDirectories(Paths.get( outname ).getParent());
		BufferedWriter bw = new BufferedWriter( new FileWriter( new File( outname ) ) );
		
		
		BufferedReader br = new BufferedReader( new FileReader( new File( filename ) ) );
		String line;
		while( (line= br.readLine( )) != null ){
			if( line.equals( "" ) ){
				break;
			}
			BPELprojectPaths.add( line.trim( ) );
		}
		br.close( );
		
		String[] projectPaths = BPELprojectPaths.toArray( new String[BPELprojectPaths.size( )]  );

		BPELTransformer bpel2bip = new BPELTransformer( PerformanceExperiment , DEBUG, true, bw );
		bpel2bip.transformBatchProjects( BIP_PROJECTS_PATH , BPEL_PROJECTS_PATH , projectPaths , -1 , -1 );
		
		bw.close( );
	}
	
	


}
