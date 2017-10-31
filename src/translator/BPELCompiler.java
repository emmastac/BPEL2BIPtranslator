package translator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;

import observers.BIPPropertyObserver;
import observers.DeadlockObserver;
import observers.MessageObserverIn;
import observers.MessageObserverOut;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import stUtils.MapKeys;
import stUtils.MapUtils;
import bipmodel.BIPHeader;
import bpelUtils.BPELFile;
import bpelUtils.NodeName;
import bpelUtils.WSDLFile;

public class BPELCompiler {

	public static int NEW = 0;

	private static final String BPELNamespaceURI_2_0 = "http://docs.oasis-open.org/wsbpel/2.0/process/executable/";
	private static final String BPELNamespaceURI_1_0 = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";

	private static String buildFile = "/src/resources/build.sh";
	private static String [ ] extSourceFiles = { "/src/resources/Standard.cpp" , "/src/resources/Standard.hpp" };

	private static String BPEL_version = BPELNamespaceURI_2_0;
	private static final String [ ] dependencyExtensions = new String [ ] { "wsdl" };
	private ArrayList < BIPPropertyObserver > bipObservers = new ArrayList < BIPPropertyObserver >( );

	private HashMap < String , ArrayList < String > > localNspace2locs = new HashMap < String , ArrayList < String > >( );
	// e.g. BPELProjectsCustom/Airline_12/Airline.wsdl -> WSDLfileX
	public HashMap < String , WSDLFile > locsToParsedFile = new HashMap < String , WSDLFile >( );
	public BPELFile bpelFile;

	private TemplateMaker tm;
	private String bpelHome;
	private File baseDir;

	private BufferedWriter bipw , hppw , cppw;
	private boolean withOutput;


	public static BufferedReader resourceReader( String path ) {

		return new BufferedReader( new InputStreamReader( BPELCompiler.class.getResourceAsStream( path ) ) );

	}


	


	public void compile( String bpelFileName  , boolean withOutput  , String sourceProjectsPath  , String BIPprojectPath  , String BIPprojectName  ,
			boolean withObservers  ) throws Exception {

		this.withOutput = withOutput;
		try {
			
			bpelFile = new BPELFile( bpelFileName );
			Document bpelDoc = bpelFile.getDoc( );
			if ( bpelDoc == null ) {
				throw new IllegalArgumentException( "Null bpel File" );
			}

			baseDir = new File( bpelFileName ).getParentFile( );

			// record the targetNamespaces of all dependent files in baseDir
			List < File > localfiles = new ArrayList < File >( );
			collectFiles( baseDir , null , localfiles , dependencyExtensions );
			List < File > remotefiles = new ArrayList < File >( );
			collectFiles( new File( sourceProjectsPath ) , baseDir , remotefiles , dependencyExtensions );

			for ( File f : localfiles ) {
				WSDLFile wsdl = new WSDLFile( f.getPath( ) );
				if ( !localNspace2locs.containsKey( wsdl.getTargetNamespace( ) ) ) {
					localNspace2locs.put( wsdl.getTargetNamespace( ) , new ArrayList < String >( ) );
				}
				localNspace2locs.get( wsdl.getTargetNamespace( ) ).add( f.getPath( ) );
			}

			for ( File f : remotefiles ) {
				WSDLFile wsdl = new WSDLFile( f.getPath( ) );
				boolean sameExists = false;

				if ( !localNspace2locs.containsKey( wsdl.getTargetNamespace( ) ) ) {
					localNspace2locs.put( wsdl.getTargetNamespace( ) , new ArrayList < String >( ) );
				} else {
					for ( String path : localNspace2locs.get( wsdl.getTargetNamespace( ) ) ) {
						if ( path.endsWith( f.getName( ) ) ) {
							sameExists = true;
							break;
						}
					}
				}
				if ( !sameExists ) {
					localNspace2locs.get( wsdl.getTargetNamespace( ) ).add( f.getPath( ) );
				}
			}

			if ( BIPprojectName.equals( "" ) ) {
				BIPprojectName = baseDir.getName( );
			}

			if ( withOutput ) {
				Files.createDirectories( Paths.get( BIPprojectName + "/src-ext" ) );
				bipw = new BufferedWriter( new FileWriter( BIPprojectName + "/check.bip" ) );
				hppw = new BufferedWriter( new FileWriter( BIPprojectName + "/src-ext/Check.hpp" ) );
				cppw = new BufferedWriter( new FileWriter( BIPprojectName + "/src-ext/Check.cpp" ) );

				if ( ! ( Paths.get( BIPprojectName ).resolve( buildFile ) ).toFile( ).exists( ) ) {
					String buildFileOut = Paths.get( buildFile ).getParent( ).relativize( Paths.get( buildFile ) ).toString( );
					copyFiles( buildFile , Paths.get( BIPprojectName ).toString( ) + "/" + buildFileOut );
				}
				for ( int i = 0 ; i < extSourceFiles.length ; i++ ) {
					Path filename = Paths.get( extSourceFiles[ i ] ).getParent( ).relativize( Paths.get( extSourceFiles[ i ] ) );
					Path destFile = Paths.get( BIPprojectName + "/src-ext" , filename.toString( ) );
					if ( !destFile.toFile( ).exists( ) ) {

						copyFiles( extSourceFiles[ i ] , destFile.toString( ) );
					}
				}

				File source = new File( buildFile.toString( ) );
				source.setExecutable( true );
				source.setWritable( true );
			}

			tm = new TemplateMaker( new File( bpelFileName ) , this );
			
			

			// translation options, hardcoded for now
			if ( withOutput ) {
				hppw.write( tm.applyHeaderHPP( ) );
				cppw.write( tm.applyHeaderCPP( ) );

				bipw.write( tm.applyHeaderBIP( "check" ) );
			}

			// configure observers
			if ( withObservers ) {
				// declare all observers
				MessageObserverOut allReceivedObs = new MessageObserverOut( "allReceived" , tm );
				if ( allReceivedObs.configure( bpelFileName , sourceProjectsPath ) ) {
					bipObservers.add( allReceivedObs );
				}

				MessageObserverIn allSentObs = new MessageObserverIn( "allSent" , tm );
				if ( allSentObs.configure( bpelFileName , sourceProjectsPath ) ) {
					bipObservers.add( allSentObs );
				}

				DeadlockObserver deadlockObs = new DeadlockObserver( "deadlock" , tm );
				if ( deadlockObs.configure( bpelFileName , sourceProjectsPath ) ) {
					bipObservers.add( deadlockObs );
				}

			}

			// transform
			HashMap < String , ArrayList > ret = transform( bpelDoc.getDocumentElement( ) , new ArrayDeque < HashMap < String , ArrayList >>( ) );

			if ( withOutput ) {
				bipw.write( tm.applyFooter( ret ) );
			}
			if ( withOutput ) {
				bipw.close( );
				hppw.close( );
				cppw.close( );
			}
		} catch ( IOException e ) {

			e.printStackTrace( );
		} finally {
			if ( bipw != null ) {
				bipw.close( );
			}

			if ( cppw != null ) {
				cppw.close( );
			}

			if ( hppw != null ) {
				hppw.close( );
			}
		}

	}


	public HashMap < String , ArrayList > transform( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		// add info to be inherited, variables
		stack = initialize( elem , stack );
		// get all children to be visited (only those in NodeNames)
		ArrayList < Element > al = bpelFile.getBPELChildren( elem );

		// if component needs duplicate (i.e. it is a parallel forEach)
		if ( tm.needsDuplicateComponent( elem ) ) {
			al.addAll( bpelFile.getBPELChildren( elem ) );
		}
		HashMap < String , ArrayList > hm = transformChildren( elem , al , stack );

		for ( String key : hm.keySet( ) ) {
			stack.peek( ).put( key , hm.get( key ) );
		}
		HashMap < String , ArrayList > ret = applyTemplate( elem , stack );
		if ( withOutput ) {
			writeCode( ret );
		}
		stack.pop( );
		return ret;
	}


	/**
	 * Visits each child and gets its resulting hashMap. In the end, all
	 * hashMaps' entries are inserted in one hashMap, where each key points at
	 * an arrayList whose i-th position contains the value of the i-th child
	 * node of the same key.
	 * 
	 * @param elem
	 *            the parent element
	 * @param elementsList
	 *            the list of child elements to be visited
	 * @param stack
	 *            the list of useful information on ancestors, like inherited
	 *            attributes
	 * @return a hashMap of entries <PropertyName,
	 *         ArrayList<ChildrenProperties>>
	 * @throws Exception
	 */
	private HashMap < String , ArrayList > transformChildren( Element elem , ArrayList < Element > elementsList ,
			ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		// create a general hasmMap with the entries of the children's hashMaps
		HashMap < String , ArrayList > general = new HashMap < String , ArrayList >( );
		int count = 0; /* keeps the order of each child */
		for ( Element el : elementsList ) {
			/* each child's transformation returns a hashMap */
			HashMap < String , ArrayList > translatedElementsInfo = transform( el , stack );

			tm.addAsChild( general , translatedElementsInfo );

			count++;

		}

		return general;
	}


	private void writeCode( HashMap < String , ArrayList > ret ) {

		String code = ( String ) MapUtils.getSingleEntry( ret , MapKeys.BIP_CODE );
		// fragments.add(res);
		try {
			if ( code != null ) {
				// System.out.println(code);
				bipw.write( code );
				ret.remove( MapKeys.BIP_CODE );
			}

			code = ( String ) MapUtils.getSingleEntry( ret , MapKeys.HPP_CODE );
			if ( code != null ) {
				hppw.write( code );
				ret.remove( MapKeys.HPP_CODE );
			}

			code = ( String ) MapUtils.getSingleEntry( ret , MapKeys.CPP_CODE );
			if ( code != null ) {
				cppw.write( code );
				ret.remove( MapKeys.CPP_CODE );
			}
		} catch ( IOException e ) {
			try {
				bipw.close( );
				hppw.close( );
				cppw.close( );
			} catch ( IOException eo ) {

				eo.printStackTrace( );
			}
		}
	}


	private void recordElementsNameSpaces( Element rootElement , HashMap < String , ArrayList > hm ) throws Exception {

		hm.put( MapKeys.NSPACEL_TO_PATHS , new ArrayList < Object >( 1 ) );
		HashMap < String , ArrayList < String > > nspaceL_to_paths = new HashMap < String , ArrayList < String > >( );
		hm.get( MapKeys.NSPACEL_TO_PATHS ).add( nspaceL_to_paths );

		// ArrayList<Node> implicitImports = new ArrayList<Node>();
		NamedNodeMap implicitImports = rootElement.getAttributes( );

		for ( int i = 0 ; i < implicitImports.getLength( ) ; i++ ) {

			Attr attr = ( Attr ) implicitImports.item( i );
			String s = attr.getName( );
			// if the attribute is not a namespaced file, continue
			if ( s == null || !s.startsWith( "xmlns" ) ) {
				continue;
			}
			String namespaceURL = XMLDomParser.transPath( attr.getValue( ) );
			if ( attr.getNodeName( ).equals( "xmlns" ) ) {
				// if the namespace for BPEL executable process is found
				if ( XMLDomParser.pathEquals( BPELNamespaceURI_2_0 , namespaceURL ) || XMLDomParser.pathEquals( BPELNamespaceURI_1_0 , namespaceURL ) ) {
					if ( XMLDomParser.pathEquals( BPELNamespaceURI_1_0 , namespaceURL ) ) {
						BPEL_version = BPELNamespaceURI_1_0;
					}
					namespaceURL = XMLDomParser.transPath( namespaceURL );
					QName qname = new QName( namespaceURL , "" , "xmlns" );
					bpelFile.setBPELnamespace( qname );
				}
			} else {

				String localPart = attr.getNodeName( ).split( ":" )[ 1 ];
				// record nspace
				addNameSpacedImport( namespaceURL , localPart , nspaceL_to_paths );

			}
		}

	}


	private void addNameSpacedImport( String namespaceURL , String nspaceL , HashMap < String , ArrayList < String >> nspaceL_to_paths )
			throws Exception {

		ArrayList < String > locations = new ArrayList < String >( );
		if ( localNspace2locs.containsKey( namespaceURL ) ) {
			// System.out.println( "URL of the namespace " + namespaceURL );
			locations.addAll( localNspace2locs.get( namespaceURL ) );

		} else {

			// if ( NEW == 1 ) {
			System.out.println( "Warning: URL of the namespace " + namespaceURL + " was not found; There will be errors if it is a used wsdl file." );
			return;
			// } else {
			// // turn url to a document path (custom)
			// String path = namespaceURL.replace( "://" , "_" ).replace( "/" ,
			// "_" );
			// String parentFolder = nspacedSourceFiles.toString( ) + "/" +
			// path;
			//
			// File f = new File( parentFolder );
			// if ( !f.exists( ) ) {
			// return;
			// }
			// // so far find wsdls, xsd
			//
			// List < File > files = ( List < File > ) FileUtils.listFiles( f ,
			// dependencyExtensions , false );
			// if ( files.size( ) == 1 ) {
			// locations.add( files.get( 0 ).getPath( ) );
			// }
			// }

		}

		nspaceL_to_paths.put( nspaceL , locations );
		for ( String location : locations ) {
			// if the file is not parsed, parse it
			if ( !locsToParsedFile.containsKey( location ) ) {
				WSDLFile wsdl = new WSDLFile( location );
				wsdl.parseWSDL( );
				locsToParsedFile.put( location , wsdl );
			}

		}

	}


	/**
	 * Adds info to vars that is needed to descendants (e.g. inherited
	 * attributes, declared variables etc)
	 * 
	 * @param el
	 * @throws Exception
	 */
	private ArrayDeque < HashMap < String , ArrayList >> initialize( Element el , ArrayDeque < HashMap < String , ArrayList >> stack )
			throws Exception {

		NodeName tag = NodeName.getNodeName( el.getTagName( ) );

		HashMap < String , ArrayList > hm = new HashMap < String , ArrayList >( );
		stack.push( hm );
		// add standard-attributes
		hm = standardInitialize( el , hm , tag );

		if ( NodeName.tags2nodeNames.values( ).contains( tag ) ) {
			for ( int i = 0 ; i < bipObservers.size( ) ; i++ ) {
				bipObservers.get( i ).pre_observeActivity( el , stack );
			}
		}

		switch ( tag ) {
		case EVENT_HANDLERS :
			hm.put( MapKeys.INSIDE_EVENT_HANDLER , null );
			return stack;
		case PROCESS :

			tm.templateList.addInstance( "e0b0port" );
			// tm.tmplInst.add("e1b0port");

			String targetNspace = ( el.hasAttribute( "targetNamespace" ) ) ? el.getAttribute( "targetNamespace" ) : "";
			targetNspace = XMLDomParser.transPath( targetNspace );
			bpelFile.setTargetNamespace( targetNspace );

			// readWSDLs(el, nspace2locs, loc2nspaces, importedDocuments);

			// fill messages with wsdl message types
			bpelHome = baseDir.getAbsolutePath( ).toString( );
			if ( bpelHome.endsWith( "/" ) ) {
				bpelHome = bpelHome.substring( 0 , bpelHome.length( ) - 1 );
			}

			// after process initialize as scope
			// return vars;
		case SCOPE :
			// extract scope variables
			HashMap < String , ArrayList < String >> var2parts = new HashMap < String , ArrayList < String >>( );

			ArrayList < Object > msgType = new ArrayList < Object >( );
			ArrayList < Object > scopeIds = new ArrayList < Object >( );
			scopeIds.add( new HashMap < String , Integer >( ) );
			hm.put( MapKeys.SCOPE_IDS , scopeIds );
			hm.put( MapKeys.VARIABLES , new ArrayList < Object >( 1 ) );
			hm.get( MapKeys.VARIABLES ).add( var2parts );
			hm.put( MapKeys.VARIABLES_MSG , msgType );

			for ( Element variable : bpelFile.getChildrenInWrapper( el , "variables" , "variable" ) ) {
				String key = variable.getAttribute( "name" );
				/* if variable is message type, add parts to the map ... */
				if ( variable.hasAttribute( "messageType" ) ) {
					String type = variable.getAttribute( "messageType" );
					msgType.add( type );
					ArrayList < String > parts = new ArrayList < String >( );
					parts.addAll( tm.getMessageParts( stack , type ) );
					for ( int j = 0 ; j < parts.size( ) ; j++ ) {
						String part = parts.get( j );
						String newpart = key + "_" + part;
						parts.remove( j );
						parts.add( j , newpart );
					}
					var2parts.put( key , parts );
				} /* ... else, add an empty parts list to the map */
				else {
					var2parts.put( key , new ArrayList( 0 ) );

				}

			}

			// add loops counterName (if any) at the scope's variables list

			String counterName = ( String ) MapUtils.getSingleEntry( stack.peek( ) , MapKeys.COUNTER_NAME );
			if ( counterName != null ) {
				var2parts.put( counterName , new ArrayList( 0 ) );
			}

			ArrayList < Element > msgExchElems = bpelFile.getChildrenInWrapper( el , "messageExchanges" , "messageExchange" );
			hm.put( MapKeys.MESSAGE_EXCHANGES , new ArrayList < Object >( msgExchElems.size( ) ) );
			for ( Element msgExchangeEl : msgExchElems ) {
				hm.get( MapKeys.MESSAGE_EXCHANGES ).add( msgExchangeEl.getAttribute( "name" ) );
			}

			ArrayList al;
			ArrayList < Element > partnerLinkElems = bpelFile.getChildrenInWrapper( el , "partnerLinks" , "partnerLink" );
			if ( partnerLinkElems.size( ) > 0 ) {
				hm.put( MapKeys.PARTNER_LINKS , new ArrayList( 1 ) );
				HashMap < String , ArrayList > pl2pLinks = new HashMap < String , ArrayList >( );
				for ( Element partnerLinkEl : partnerLinkElems ) {
					al = new ArrayList( );
					// 0: partnerLinkType
					al.add( partnerLinkEl.getAttribute( "partnerLinkType" ) );
					// 1: partnerLinkRole
					al.add( partnerLinkEl.getAttribute( "partnerRole" ) );
					// 2: initializePartnerRole
					if ( partnerLinkEl.hasAttribute( "initializePartnerRole" ) ) {
						al.add( partnerLinkEl.getAttribute( "initializePartnerRole" ) );
					} else {
						al.add( "yes" );
					}
					String key = partnerLinkEl.getAttribute( "name" );
					pl2pLinks.put( key , al );
				}
				hm.get( MapKeys.PARTNER_LINKS ).add( pl2pLinks );

			}

			// read the correlationSet elements
			ArrayList < Element > corrSetElems = bpelFile.getChildrenInWrapper( el , "correlationSets" , "correlationSet" );
			HashMap < String , ArrayList > cs2properties = new HashMap < String , ArrayList >( );
			hm.put( MapKeys.CORRELATION_SETS , new ArrayList( corrSetElems.size( ) ) );
			for ( Element cs : corrSetElems ) {
				String name = ( cs.hasAttribute( "name" ) ) ? cs.getAttribute( "name" ) : "";
				String properties = ( cs.hasAttribute( "properties" ) ) ? cs.getAttribute( "properties" ) : "";
				String [ ] propEntries = properties.split( "," );
				if ( propEntries.length > 0 ) {
					ArrayList propAl = new ArrayList( propEntries.length );
					propAl.addAll( Arrays.asList( propEntries ) );
					cs2properties.put( name , propAl );
				} else {
					ArrayList propAl = new ArrayList( 1 );
					propAl.add( properties );
					cs2properties.put( name , propAl );
				}
			}

			hm.get( MapKeys.CORRELATION_SETS ).add( cs2properties );

			// add the declared attributes (or default)
			if ( !el.hasAttribute( "suppressJoinFailure" ) ) {
				al = new ArrayList < Object >( 1 );
				al.add( "no" );
				hm.put( MapKeys.SUPPRESS_JOIN_FAILURE , al );
			} else {
				al = new ArrayList < Object >( 1 );
				al.add( el.getAttribute( "suppressJoinFailure" ) );
				hm.put( MapKeys.SUPPRESS_JOIN_FAILURE , al );
			}
			if ( !el.hasAttribute( "exitOnStandardFault" ) ) {
				al = new ArrayList < Object >( 1 );
				al.add( "no" );
				hm.put( MapKeys.EXIT_ON_STANDARD_FAULT , al );
			} else {
				al = new ArrayList < Object >( 1 );
				al.add( el.getAttribute( "exitOnStandardFault" ) );
				hm.put( MapKeys.EXIT_ON_STANDARD_FAULT , al );
			}

			String templateId = String.valueOf( TemplateMaker.templateList.getSize( ) + 1 );
			TemplateMaker.templateList.addInstance( templateId );
			// add the declared attributes (or default)
			ArrayList aList = new ArrayList < Object >( 1 );
			aList.add( templateId );
			hm.put( MapKeys.ID , aList );

			return stack;
		case FOR_EACH :
			// omit successfulBranchesOnly attribute
			String counterName2 = el.getAttribute( "counterName" );
			ArrayList singleEntry = new ArrayList( 1 );
			singleEntry.add( counterName2 );
			hm.put( MapKeys.COUNTER_NAME , singleEntry );

			return stack;
		case FLOW :
			// tm.flows++;
			/* find links */
			ArrayList < Element > linkElems = bpelFile.getChildrenInWrapper( el , "links" , "link" );
			HashSet < String > linksList = new HashSet < String >( linkElems.size( ) );
			for ( Element linkEl : linkElems ) {
				linksList.add( tm.sanitize( ( String ) linkEl.getAttribute( "name" ) ) );
			}
			al = new ArrayList < Object >( 1 );
			al.add( linksList );
			hm.put( MapKeys.LINKS , al );

			return stack;
		case BPELX_FLOWN :
			linkElems = bpelFile.getChildrenInWrapper( el , "links" , "link" );
			linksList = new HashSet < String >( linkElems.size( ) );
			for ( Element linkEl : linkElems ) {
				linksList.add( tm.sanitize( ( String ) linkEl.getAttribute( "name" ) ) );
			}
			al = new ArrayList < Object >( 1 );
			al.add( linksList );
			hm.put( MapKeys.LINKS , al );
			// add variables entry (i.e. index)
			HashMap < String , ArrayList > variablesEntry = new HashMap < String , ArrayList >( 1 );
			variablesEntry.put( el.getAttribute( "indexVariable" ) , new ArrayList <>( 0 ) );
			al = new ArrayList < Object >( 1 );
			al.add( variablesEntry );
			hm.put( MapKeys.VARIABLES , al );

			return stack;
		case CATCH :
			if ( el.hasAttribute( "faultVariable" ) ) {
				// get the name of faultVariable
				String key = el.getAttribute( "faultVariable" );
				String mtype = el.getAttribute( "faultMessageType" );
				// make a map from var to its parts
				HashMap < String , ArrayList > var2parts2 = new HashMap < String , ArrayList >( );
				// find variable's parts as list
				ArrayList < String > parts;
				if ( !mtype.equals( "" ) ) {
					parts = tm.getMessageParts( stack , mtype );
				} else {
					parts = MapUtils.getVariableParts( stack , key );
				}
				var2parts2.put( key , parts ); /* put the entry to the map */

				ArrayList < Object > tmp = new ArrayList < Object >( 1 );
				tmp.add( var2parts2 ); /* add the map to a one position list */
				hm.put( MapKeys.VARIABLES , tmp ); /* put the list to the map */
			}
			return stack;

		default :

			return stack;
		}
	}


	private HashMap < String , ArrayList > standardInitialize( Element el , HashMap < String , ArrayList > hm , NodeName tag ) throws Exception {

		ArrayList < Object > wrapper = new ArrayList < Object >( 1 );
		ArrayList < Object > actual = new ArrayList < Object >( 1 );
		actual.add( tag );
		wrapper.add( actual );
		hm.put( MapKeys.TAG , wrapper );

		if ( el.hasAttribute( "suppressJoinFailure" ) ) {
			ArrayList < Object > al = new ArrayList < Object >( 1 );
			al.add( el.getAttribute( "suppressJoinFailure" ) );
			hm.put( MapKeys.SUPPRESS_JOIN_FAILURE , al );
		}
		recordElementsNameSpaces( el , hm );

		return hm;
	}


	/**
	 * Called only on nodes that have an associated template (those that are in
	 * NodeNames)
	 * 
	 * @param el
	 * @param stack
	 *            the hashMap of the information gathered by the children needed
	 * @return
	 * @throws Exception
	 */
	private HashMap < String , ArrayList > applyTemplate( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		// NodeName tag = NodeName.getNodeName(bpelFile.getDOM1LocalName(el));
		NodeName tag = NodeName.getNodeName( el.getTagName( ) );
		if ( tag.equals( NodeName.UNKNOWN ) ) {
			System.out.println( tag + ":" + el.getTagName( ) );
		} else {
			System.out.println( tag );
		}

		HashMap < String , ArrayList > ret = null;
		switch ( tag ) {
		case TERMINATE :
			ret = tm.applyExit( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case EXIT :
			ret = tm.applyExit( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case THROW :
			ret = tm.applyThrow( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case RETHROW :
			ret = tm.applyRethrow( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case ELSE :
			ret = tm.applyTransparent( el , stack );
			break;
		case ELSE_IF :
			ret = tm.applyTransparent( el , stack );
			break;
		case IF :
			ret = tm.applyCOND( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case SWITCH :
			ret = tm.applySWITCH( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case OTHERWISE :
			ret = tm.applyTransparent( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case CASE :
			ret = tm.applyTransparent( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case FOR_EACH :
			ret = tm.applyLOOP( el , stack , "forEach" );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case REPEAT_UNTIL :
			ret = tm.applyLOOP( el , stack , "repeatUntil" );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case WHILE :
			ret = tm.applyLOOP( el , stack , "while" );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case EVENT_HANDLERS :
			ret = tm.applyEH( el , stack );
			break;
		case PROCESS :
			ret = tm.applySCOPE( el , stack , true );
			break;
		case CATCH :
			ret = tm.applyFHW( el , stack );
			break;
		case CATCHALL :
			ret = tm.applyFHW( el , stack );
			break;
		case ON_EVENT :
			ret = tm.applyONMESSAGE( el , stack );
			break;
		case ON_MESSAGE :
			ret = tm.applyONMESSAGE( el , stack );
			break;
		case ON_ALARM :
			ret = tm.applyONALARM( el , stack );
			break;
		case PICK :
			ret = tm.applyPICK( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case COMPENSATION_HANDLER :
			ret = tm.applyCTH( el , stack , "CH" );
			break;
		case COMPENSATE_SCOPE :
			ret = tm.applyCOMPENSATE( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case COMPENSATE :
			ret = tm.applyCOMPENSATE( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case TERMINATION_HANDLER :
			ret = tm.applyCTH( el , stack , "TH" );
			break;
		case FAULT_HANDLERS :
			ret = tm.applyFH( el , stack );
			break;
		case REPLY :
			ret = tm.applyREPLY( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case SCOPE :
			ret = tm.applySCOPE( el , stack , false );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case RECEIVE :
			ret = tm.applyRECEIVE( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case SEQUENCE :
			ret = tm.applySEQ( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case EMPTY :
			ret = tm.applyEMPTYACT( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case ASSIGN :
			ret = tm.applyASSIGN( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case COPY :
			ret = tm.applyCOPY( el , stack );
			break;
		case BPELX_APPEND :
			ret = tm.applyCOPY( el , stack );
			break;
		case INVOKE :
			ret = tm.applyINVOKE( el , stack );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case FLOW :
			ret = tm.applyFLOW( el , stack , -1 );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case BPELX_FLOWN :
			ret = tm.applyFLOW( el , stack , 2 );
			ret = tm.applyACTW( el , stack , ret );
			break;
		case SOURCES :
			ret = tm.applyLS( el , stack );
			break;
		case SOURCE :
			ret = tm.applyLSW( el , stack );
			break;
		case TARGETS :
			ret = tm.applyLT( el , stack );
			break;
		default :
			System.out.println( "No template for: " + tag );
		}

		if ( ret == null ) {
			ret = new HashMap < String , ArrayList >( 1 );
		}

		if ( NodeName.tags2nodeNames.values( ).contains( tag ) ) {
			for ( int i = 0 ; i < bipObservers.size( ) ; i++ ) {
				bipObservers.get( i ).post_observeActivity( el , stack , ret );
			}
		}
		return ret;
	}
	
	private void copyFiles( String from , String to ) throws IOException , URISyntaxException {

		Path source = Paths.get( System.getProperty( "user.dir" ) + from );
		Path target = FileSystems.getDefault( ).getPath( to );

		Files.copy( source , target );

	}


	private void collectFiles( File parent , File omitFolder , List < File > files , String [ ] extensions ) {

		File [ ] list = parent.listFiles( );

		if ( list == null )
			return;

		for ( File f : list ) {
			if ( f.equals( omitFolder ) ) {
				continue;
			} else if ( f.isDirectory( ) ) {
				collectFiles( f.getAbsoluteFile( ) , omitFolder , files , extensions );
			} else {
				for ( String ext : extensions ) {
					if ( f.toString( ).endsWith( "." + ext ) ) {
						files.add( f.getAbsoluteFile( ) );
					}
				}
			}
		}
	}

}