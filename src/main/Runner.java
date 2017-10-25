package main;

import org.apache.commons.cli.*;

public class Runner {

	public static void main( String [ ] args ) throws Exception {

		// TODO: replace apache.commons.cli with JCommander at some point
		Options options = new Options( );

		Option help = new Option( "help" , "print this message" );
		options.addOption( help );

		Option goalTranslate = new Option( "translate" , "translate from BPEL to BIP" );
		goalTranslate.setRequired( false );
		options.addOption( goalTranslate );

		Option goalAnalyzeOutput = new Option( "analyze" , "analyze BIP output" );
		goalAnalyzeOutput.setRequired( false );
		options.addOption( goalAnalyzeOutput );
		
		
		Option goalForExecution = new Option( "forExecution" , "translate from BPEL to BIP for execution" );
		goalForExecution.setRequired( false );
		options.addOption( goalForExecution );

		Option goalHomeInput = new Option( "home" , "where is the Runner's home?" );
		goalHomeInput.setRequired( false );
		goalHomeInput.setArgs(1);
		options.addOption( goalHomeInput );

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
		
		if(cmd.hasOption( "home" )){
			String home = cmd.getOptionValue( "home" );
			Preferences.setHOME(home);
		}
		if(cmd.hasOption( "forExecution" )){
			Preferences.setForExecution(true);
		}else{
			Preferences.setForExecution(false);
		}
		
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

	}

}
