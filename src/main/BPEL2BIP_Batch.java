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

//	public static String BIP_PROJECTS_PATH = Preferences.getBIPprojectsPath();
//	public static String BPEL_PROJECTS_PATH = Preferences.getBPELprojectsPath();
//	
//	public static int DEBUG = 0;
//	public static boolean PerformanceExperiment = true;
//	
//	public static String filename = Preferences.getBPELprojectsCfgPath();
//	public static String outname = Preferences.getBIPprojectsCfgPath();
	

//	public static void main( String [ ] args ) throws Exception {
//
//		translateAllProjectsInFile( );
//	}


	
	public static void translateAllProjectsInFile( ) throws Exception {
		
		String filename = Preferences.getBPELprojectsCfgPath();
		String outname = Preferences.getBIPprojectsCfgPath();
		String BIP_PROJECTS_PATH = Preferences.getBIPprojectsPath();
		String BPEL_PROJECTS_PATH = Preferences.getBPELprojectsPath();

		if( !Files.exists( Paths.get( filename ) )) {
			System.out.println("Please provide a BPEL projects' configuration file, as"+filename+" doesn't exist.");
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
		
		

		BPELTransformer bpel2bip = new BPELTransformer( !Preferences.isForExecution() , bw );
		bpel2bip.transformBatchProjects( BIP_PROJECTS_PATH , BPEL_PROJECTS_PATH , projectPaths , -1 , -1 );
		
		bw.close( );
	}
	
	
	public static void translateProject( String bpelFile, String wsdlFile, String bipFile) throws Exception {

		Files.createDirectories(Paths.get( bipFile ).getParent());
		BufferedWriter bw = new BufferedWriter( new FileWriter( new File( bipFile ) ) );
		
		
		BPELTransformer bpel2bip = new BPELTransformer( !Preferences.isForExecution() , bw );
		bpel2bip.transformFile( new File(bpelFile) , "" , bipFile );
		
		bw.close( );
	}
	
	


}
