package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalyzeOutput {


	public static String BIP_PROJECTS_PATH = Preferences.BIPprojectsPath;
	public static String BIP_PROJECTS_CFG_PATH = Preferences.BIPprojectsCfgPath;
	public static String BIP_EXPLORATION_OUTPUT = Preferences.BIexplorationOutputPath;


	
	private static String analyzeFile( String path ) throws IOException {


		String contents = new String( Files.readAllBytes( Paths.get( BIP_PROJECTS_PATH + path + BIP_EXPLORATION_OUTPUT ) ) );

		// String line = "";
		int msgIns = 0;
		HashSet < String > msgInViol = new HashSet < String >( );

		int msgOuts = 0;
		HashSet < String > msgOutViol = new HashSet < String >( );
		int deadInitsCount = 0;
		HashSet < String > deadInits = new HashSet < String >( );

		int termInitsCount = 0;
		HashSet < String > termInits = new HashSet < String >( );

		Pattern p;
		Matcher m;
		// ///////////////////////////
		p = Pattern.compile( "OBS_TERM: initial (^\\s+)" );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			String s = m.group( 1 );
			termInits.remove( s );
		}
		termInitsCount = termInits.size( );
		// //////////////////////////
		p = Pattern.compile( "msgin: initial," );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			msgIns++;
		}
		// //////////////////////////
		p = Pattern.compile( "msgout: initial," );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			msgOuts++;
		}
		// //////////////////////////
		p = Pattern.compile( "deadcode: initial (\\d+)" );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			String s = m.group( 1 );
			deadInits.add( s );
		}
		deadInitsCount = deadInits.size( );
		// ///////////////////////////////////
		p = Pattern.compile( "OBS_VIOLATION:allSent (\\w+\\d+)" );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			String s = m.group( 1 );
			msgInViol.add( s );
		}
		// /////////////////////////
		p = Pattern.compile( "OBS_VIOLATION:allReceived (^\\s+)" );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			String s = m.group( 1 );
			msgOutViol.add( s );
		}
		// /////////////////////
		p = Pattern.compile( "OBS_TERM violated by (^\\s+)" );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			String s = m.group( 1 );
			termInits.remove( s );
		}
		// /////////////////////////
		p = Pattern.compile( "for deadcode observer: started (\\d+)" );
		m = p.matcher( contents );

		while ( m.find( ) ) {
			String s = m.group( 1 );
			deadInits.remove( s );
		}

		// /////////////////////////
		p = Pattern.compile( "found (\\d+) reachable states, (\\d+) deadlock" );
		m = p.matcher( contents );
		String states = "";
		String deadlocks = "";
		if ( m.find( ) ) {
			states = m.group( 1 );
			deadlocks = m.group( 2 );
		}

		String out = path + ",0,(" + termInits.size( ) + "/" + termInitsCount + "),(" + deadInits.size( ) + "/" + deadInitsCount + "),(" + ( msgOutViol.size( ) + msgInViol.size( ) ) + "/"
				+ ( msgOuts + msgIns ) + "), " + states + "," + deadlocks;
		// System.out.println( out );
		return out;

	}


	public static void analyzeBatch( ) throws IOException {

		BufferedWriter bw = new BufferedWriter( new FileWriter( BIP_PROJECTS_PATH + "/analysisOut.txt" ) );

		BufferedReader br = new BufferedReader( new FileReader( BIP_PROJECTS_CFG_PATH ) );
		String line = "";
		while ( ( line = br.readLine( ) ) != null && !line.trim( ).isEmpty( ) ) {
			String [ ] pathParts = line.split( "," )[ 2 ].split( "\\/" );
			String file = pathParts[ pathParts.length - 1 ];
			bw.write( analyzeFile( file ) + '\n' );

		}

		bw.close( );
		br.close( );

	}
	
	private static void readArgs( String [ ] args ){
		if(args.length>0 ){
			BIP_PROJECTS_PATH = args[0].trim();
		}
		if(args.length>1 ){
			BIP_PROJECTS_PATH = args[1].trim();
		}
	}

	
	public static void main( String [ ] args ) throws IOException {
		readArgs( args );

		analyzeBatch( );
		// analyzeFile( "bpelPrograms_AmericanAirline_59_AmericanAirline/" );
	}

}
