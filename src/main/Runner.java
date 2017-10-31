package main;


import org.apache.commons.cli.*;

public class Runner {

	public static void main( String [ ] args ) throws Exception {

		Runner r = new Runner( );
		Options opt = r.addCommandLineOptions( );
		r.parseCommandLineOptions(opt, args);
	}
	
	private Options addCommandLineOptions( ) {
		Options options = new Options( );
		
		addCommandOption( options, "help" , "print this message", false);
		addCommandOption( options, "translate" , "translate from BPEL to BIP", false);
		addCommandOption( options, "analyze" , "analyze BIP output", false);
		addCommandOption( options, "forExecution" , "translate from BPEL to BIP for execution", false);
		addCommandWithArgsOption( options, "home" , "where is the Runner's home?", false, 1);
		
		return options;
	}
	
	private void parseCommandLineOptions( Options options, String [ ] args ) {
		
		CommandLineParser parser = new DefaultParser( );
		HelpFormatter formatter = new HelpFormatter( );
		CommandLine cmd;

		try {
			cmd = parser.parse( options , args );
		} catch ( ParseException e ) {
			System.out.println( e.getMessage( ) );
			formatter.printHelp( "utility-name" , options );

			System.exit( 1 );
			return;
		}

		if ( cmd.hasOption( "home" ) ) {
			String home = cmd.getOptionValue( "home" );
			Preferences.setHOME( home );
		}
		if ( cmd.hasOption( "forExecution" ) ) {
			Preferences.setForExecution( true );
		} else {
			Preferences.setForExecution( false );
		}

		try {

			if ( cmd.hasOption( "help" ) ) {
				formatter.printHelp( "utility-name" , options );
			} else if ( cmd.hasOption( "analyze" ) ) {

				AnalyzeOutput.analyzeBatch( );

			} else if ( cmd.hasOption( "translate" ) ) {

				BPEL2BIP_Batch.translateAllProjectsInFile( );

			} else {
				System.out.println( "Please select execution option." );
				formatter.printHelp( "utility-name" , options );
			}

		} catch ( Exception e ) {
			e.printStackTrace( );
		}
	}


	
	private void addCommandOption(Options options, String title, String desc, boolean isRequired){
		Option goal = new Option( title , desc );
		goal.setRequired( isRequired );
		options.addOption( goal );
	}
	
	private void addCommandWithArgsOption(Options options, String title, String desc, boolean isRequired, int num){
		Option goal = new Option( title , desc );
		goal.setRequired( isRequired );
		goal.setArgs( num );
		options.addOption( goal );
	}

}
