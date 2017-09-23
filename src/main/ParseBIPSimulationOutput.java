package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseBIPSimulationOutput {

	public static boolean printStructure = true; // read from args
	public static String BIPprojectPath = "/...."; // read from args

	public static String simulationOutputPath = "/BIPbpelOutput.txt";
	public static String parsedOutputPath = "/BIPbpelTrace.txt";
	public static String BIPmodelStructurePath = "/ModelStructure.txt";

	public static final boolean skipInternal = false; // do not include internal
														// transitions in the
														// output trace
	public static final boolean skipOutput = false; // do not include text
													// printed by components in
													// the output trace
	public static final boolean endASAP = true; // end as soon as possible, when
												// some string is found is in
												// the trace
	public static String endASAPString1 = "ROOT.fin"; // if endASAP, when this
														// string is found, the
														// trace output stops
	public static String endASAPString2 = "ROOT.termed"; // same as above
	public static String endASAPString3 = "ROOT.done"; // same as above
	public static List < String > blacklist = new ArrayList < String >( );


	public static void main( String [ ] args ) throws Exception {

		blacklist.add( "allow" );
		blacklist.add( "done" );

		FileReader in = null;
		FileWriter out = null;

		Map < String , ArrayList < String >> type2childNames = new HashMap < String , ArrayList < String >>( );
		Map < String , String > name2type = new HashMap < String , String >( );

		readModelStructure( Preferences.BPELprojectsPath + "/" + BIPprojectPath + "/check.bip" , type2childNames , name2type );
		printModelStructure( Preferences.BPELprojectsPath + "/" + BIPprojectPath + "/ModelStructure.txt" , type2childNames , name2type );

		BufferedReader br = new BufferedReader( new FileReader( Preferences.BPELprojectsPath + "/" + BIPprojectPath + simulationOutputPath ) );
		BufferedWriter bw = new BufferedWriter( new FileWriter( Preferences.BPELprojectsPath + "/" + BIPprojectPath + parsedOutputPath ) );

		String strLine;
		String c = "";
		int b = 0;
		while ( ( strLine = br.readLine( ) ) != null ) {
			b++;

			if ( !skipOutput && !strLine.contains( "[BIP ENGINE]:" ) ) {
				bw.write( strLine + "\n" );
			} else if ( strLine.contains( "state #" ) ) {
				strLine = strLine.trim( ).split( "state #" )[ 1 ];
				strLine = strLine.split( ":" )[ 0 ];
				c = strLine;
			} else if ( strLine.contains( "choose" ) ) {
				// if (skipInternal && strLine.endsWith(".cont")) {
				//
				// System.out.println(strLine);
				// continue;
				// }
				boolean inBlacklist = false;
				for ( String black : blacklist ) {
					if ( strLine.contains( "." + black ) ) {
						inBlacklist = true;
						break;
					}
				}
				if ( inBlacklist ) {
					// System.out.println(strLine);
					continue;
				}
				strLine = strLine.split( "choose" )[ 1 ];
				strLine = strLine.replaceFirst( "\\s\\[\\d+\\]\\s" , "" );

				bw.write( c + ":" + strLine + "\n" );
				if ( endASAP && ( strLine.contains( endASAPString1 ) || strLine.contains( endASAPString2 ) || strLine.contains( endASAPString3 ) ) ) {
					break;
				}
			} else {
				// System.out.println(strLine);
			}

		}
		System.out.println( b );
		br.close( );
		bw.close( );

	}


	private static void printComponentsStructure( PrintWriter bw , String rootComponent , int indent ,
			Map < String , ArrayList < String >> type2childNames , Map < String , String > name2type ) {

		ArrayList < String > children;
		indent++;
		if ( ( children = type2childNames.get( rootComponent ) ) == null ) {
			return;
		}
		for ( String child_name : children ) {
			// print the child
			String child_type = name2type.get( child_name );

			// exclude empty types
			if ( child_type.contains( "empty" ) ) {
				continue;
			}
			String indentation = "";
			for ( int i = 1 ; i < indent ; i++ ) {
				indentation += "|--";
			}
			bw.printf( "%-52.60s  %-70.40s%n" , indentation + child_type , child_name );
			// visit its children
			printComponentsStructure( bw , child_type , indent , type2childNames , name2type );

		}

	}


	private static void printModelStructure( String OutFile , Map < String , ArrayList < String >> type2childNames , Map < String , String > name2type ) {

		try {
			PrintWriter bw = new PrintWriter( new FileWriter( OutFile ) );
			String rootComponent = "bpelProcess";
			int indent = 0;
			printComponentsStructure( bw , rootComponent , indent , type2childNames , name2type );
			bw.close( );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		} // specify a filepath
			// for input
	}


	private static void readModelStructure( String BIPfile , Map < String , ArrayList < String >> type2childNames , Map < String , String > name2type ) {

		try {
			BufferedReader br = new BufferedReader( new FileReader( BIPfile ) );
			// compType -> [ compName1, compName2, etc]

			String strLine;
			String current_compType = null;
			while ( ( strLine = br.readLine( ) ) != null ) {

				if ( strLine.contains( "compound " ) ) {
					strLine = strLine.split( "\\(" )[ 0 ];
					strLine = strLine.trim( ).replaceAll( " +" , " " );
					strLine = strLine.replace( "compound type " , "" );
					current_compType = strLine;
					type2childNames.put( current_compType , new ArrayList < String >( ) );
				} else if ( strLine.contains( "component " ) ) {
					strLine = strLine.trim( ).replaceAll( " +" , " " );
					strLine = strLine.trim( ).replace( "()" , "" );
					strLine = strLine.replace( "component " , "" );
					String [ ] compTypeName = strLine.split( " " );
					name2type.put( compTypeName[ 1 ] , compTypeName[ 0 ] );
					type2childNames.get( current_compType ).add( compTypeName[ 1 ] );
				}
			}
			br.close( );

		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

	}

}
