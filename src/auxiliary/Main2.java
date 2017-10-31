package auxiliary;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import translator.BPELCompiler;

/**
 * Alternative Main class
 * @author mania
 *
 */
public class Main2 {

	public static final String BIPprojectPath = System.getProperty( "user.home" ) + "/Tools/BIP/examples";
	public static final String BIPprojectName = "BPELPick";
	public static final String BPELFileName = System.getProperty( "user.home" ) + "/Dropbox/eclipse_workspace/TestingBPEL/bpelContent/TestPick.bpel";


	public static void main( String [ ] args ) {

		/*
		 * String BPELprogram = "docs/BPELPick.bpel";
		 */
		boolean PerformanceExperiment = false;
		BPELCompiler compiler;

		int tries = 0;
		long timesum = 0;
		long start = 0;
		long end = 0;

		while ( ( PerformanceExperiment && tries < 10 ) || ( !PerformanceExperiment && tries == 0 ) ) {
			if ( PerformanceExperiment ) {
				System.out.print( "." );
			}

			tries++;
			// start = System.currentTimeMillis();
			start = getCpuTime( );

			compiler = new BPELCompiler( );
			try {
				String sourceProgramFolder = System.getProperty( "user.home" ) + "/Dropbox/eclipse_workspace/TestingBPEL/";
				compiler.compile( BPELFileName , ( !PerformanceExperiment ) , sourceProgramFolder , BIPprojectPath , BIPprojectName , false );

			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}

		}
		// end = System.currentTimeMillis();
		end = getCpuTime( );
		timesum += ( end - start );

		System.out.println( '\n' + ( ( ( double ) timesum ) / ( 1000 * tries ) ) );
	}


	public static long getCpuTime( ) {

		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		return bean.isCurrentThreadCpuTimeSupported( ) ? ( long ) ( ( double ) bean.getCurrentThreadCpuTime( ) / 1000000.0 ) : 0L;
	}

}