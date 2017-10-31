package translator;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.util.ArrayList;

public class BPELTransformer {

	public int NEW = 0;

	private boolean withObservers;
	private BufferedWriter bw;


	public BPELTransformer( boolean withObservers  , BufferedWriter logExperOutput  ) {

		this.withObservers = withObservers;
		this.bw = logExperOutput;
	}


	public void transformBatchProjects( String targetPath , String sourcePath , String [ ] projectNames , int initIndex , int endIndex )
			throws Exception {

		File translatedProjectsRoot = new File( targetPath );
		// Delete previously created BIP project
		if ( !translatedProjectsRoot.exists( ) ) {
			Files.createDirectory( translatedProjectsRoot.toPath( ) );
		}

		if ( projectNames.length > 0 ) {
			for ( int i = 0 ; i < projectNames.length ; i++ ) {

				String filename = sourcePath + "/" + projectNames[ i ];

				File sourceFile = getSourceFile( new File( filename ) );
				if ( sourceFile != null ) {
					transformFile( sourceFile , sourcePath , targetPath );
				} else {
					System.out.println( "Project was not found at : " + filename );
				}
			}

		} else {

			ArrayList < File > sourceFolders = new ArrayList < File >( );
			getSourceProjectFolders( new File( sourcePath ) , sourceFolders );

			int end = ( endIndex == -1 ) ? sourceFolders.size( ) : endIndex;

			for ( int i = initIndex ; i < end ; i++ ) {
				File sourceFile = getSourceFile( sourceFolders.get( i ) );
				if ( sourceFile != null ) {
					transformFile( sourceFile , sourcePath , targetPath );
				}
			}
		}
	}


	private File getSourceFile( File sourceProjectFolder ) throws Exception {

		File [ ] sourceProjectFiles = sourceProjectFolder.listFiles( );
		// assign the BPEL process file to SourceProjectFolder
		if ( sourceProjectFiles != null ) {
			for ( int j = 0 ; j < sourceProjectFiles.length ; j++ ) {
				if ( sourceProjectFiles[ j ].toPath( ).toString( ).endsWith( ".bpel" ) ) {
					return sourceProjectFiles[ j ];
				}
			}
		}
		return null;

	}


	private File getSourceProjectFolders( File currFolder , ArrayList < File > sourceFiles ) throws Exception {

		File [ ] childFiles = currFolder.listFiles( );

		// in a folder, search whether there is a .bpel file, else go down one
		// level

		File sourceProject = getSourceFile( currFolder );
		if ( sourceProject != null ) {
			sourceFiles.add( sourceProject.getParentFile( ) );
			return sourceProject;
		}

		for ( File child : childFiles ) {
			if ( child.isDirectory( ) ) {
				getSourceProjectFolders( child , sourceFiles );
			}
		}
		return null;

	}


	/**
	 * Creates and executes the BIP compiler for a BPEL program.
	 * 
	 * @param sourceProgramFile
	 * @param sourceHome
	 * @param targetProjectPath
	 * @throws Exception
	 */
	public void transformFile( File sourceProgramFile , String sourceHome , String targetProjectPath ) throws Exception {

		long start = 0;
		long end = 0;
		// get the name of the current project (concatenation of parent folders
		// fafter sourceProgramsPath)
		// String currentProjectId = sourceProgramFile.getPath( ).replaceFirst(
		// ".*\\/" , "" );
		String currentProjectId = sourceProgramFile.getPath( ).replace( sourceHome + "/" , "" );
		currentProjectId = currentProjectId.replaceAll( "\\/" , "_" );
		currentProjectId = currentProjectId.replace( ".bpel" , "" );
		targetProjectPath += "/" + currentProjectId;

		File outFolder = new File( targetProjectPath );

		if ( outFolder.exists( ) ) {
			cleanDirectory( outFolder );
		} else {
			Files.createDirectory( outFolder.toPath( ) );
			Files.createDirectory( new File( targetProjectPath + "/src-ext/" ).toPath( ) );
		}
		System.out.println( sourceProgramFile.getAbsolutePath( ) + " file is transformed" );

		BPELCompiler bpelCompiler = new BPELCompiler( );
		BPELCompiler.NEW = NEW;

		
		bpelCompiler.compile( sourceProgramFile.getAbsolutePath( ) , true , sourceHome , targetProjectPath , targetProjectPath ,
				withObservers );
		
		System.out.println( sourceProgramFile + ": ended" + '\n' );
	}


	public static long getCpuTime( ) {

		//ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		//return bean.isCurrentThreadCpuTimeSupported( ) ? ( long ) ( ( double ) bean.getCurrentThreadCpuTime( ) / 1000000.0 ) : 0L;
		return System.currentTimeMillis();
	}


	/**
	 * Remove all directories enclosed in dir.
	 * 
	 * @param dir
	 */
	private void cleanDirectory( File dir ) {

		if ( dir.isDirectory( ) ) {
			File [ ] files = dir.listFiles( );
			if ( files != null && files.length > 0 ) {
				for ( File aFile : files ) {
					if ( aFile.getName( ).equals( "results" ) ) {
						continue;
					}
					removeDirectory( aFile );
				}
			}
		}
	}


	private void removeDirectory( File dir ) {

		if ( dir.isDirectory( ) ) {
			File [ ] files = dir.listFiles( );
			if ( files != null && files.length > 0 ) {
				for ( File aFile : files ) {
					removeDirectory( aFile );
				}
			}
			dir.delete( );
		} else {
			dir.delete( );
		}
	}

}
