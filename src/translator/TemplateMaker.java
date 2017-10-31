package translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import main.Preferences;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import stUtils.AttrFiller;
import stUtils.ExpressionParsingUtils;
import stUtils.MapKeys;
import stUtils.MapUtils;
import bipUtils.CompenPort;
import bipUtils.ConflIMAPort;
import bipUtils.IOMsgPort;
import bipUtils.Port;
import bipUtils.RvrsPort;
import bipUtils.RwPort;
import bipmodel.Assign;
import bipmodel.BIPHeader;
import bipmodel.Listen;
import bipmodel.PortTypeDeclaration;
import bipmodel.Receive;
import bpelUtils.BPELFile;
import bpelUtils.Faults;
import bpelUtils.NodeName;
import bpelUtils.WSDLFile;
import bpelUtils.XMLFile;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

public class TemplateMaker {

	public static final String bip4VerifTemplate = "/resources/bip.stg";
	public static final String bip4ExecTemplate = "/resources/bip4Exec.stg";

	private static Faults faults;

	public static TemplateList templateList;
	private static AttrFiller filler;
	private static BPELCompiler bcompiler;
	private static String templateFile = null;


	public TemplateMaker( File bpelFile , BPELCompiler bcompiler ) {

		this.filler = new AttrFiller( );
		this.bcompiler = bcompiler;
		templateList = new TemplateList( );

		if ( Preferences.isForExecution( ) ) {
			templateFile = bip4ExecTemplate;
		} else {
			templateFile = bip4VerifTemplate;
		}

		this.faults = new Faults( );

	}


	/**
	 * 
	 * @param scopeRoles
	 *            maps each index to the scopeRole (e.g. ma, fh etc)
	 * @param faultsList
	 *            list of fault names that are raised by scope components and
	 *            are handled in scope: (faultName, occur, compList)
	 * @param expPorts
	 *            list of fault names that are raised by scope components (e.g.
	 *            TH, CH, FH) and are not handled in scope
	 * @param intWPorts
	 *            list of assign activities and their var_parts in the scope
	 *            (here_parts_count, cont_parts_count, assignId, comp) where
	 *            here_parts_count is the number of var_parts declared in scope
	 *            and cont_parts_count is the number of var_parts declared in
	 *            enclosing scopes
	 * @param intRPorts
	 *            list of copy activities and their var_parts in the scope in
	 *            the format (compNum, copyId, comp list)
	 * @param ima
	 *            list of ima activities and their var_parts in the scope in the
	 *            format (ima_name, compNum, comp list) where ima_name =
	 *            pl_op_cs_me
	 * @param oma
	 *            list of oma activities (oma_name, comp)
	 * @return
	 * @throws FileNotFoundException
	 */

	// public <T extends BIPtemplate> void applyTemplate(T aTemp, Class<T>
	// clazz){
	// try {
	// T t = clazz.newInstance();
	// StringTemplate template = AttrFiller.getTemplate( templateFile ,
	// t.getTemplateName( ) );
	//
	// } catch ( Exception e ) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public String applyHeaderBIP( String packageName ) throws Exception {

		BIPHeader bipTemplate = new BIPHeader( );
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		bipTemplate.toTemplate( null , ret , null , templateFile , templateList );
		return ( String ) MapUtils.getSingleEntry( ret , MapKeys.BIP_CODE );
	}


	public static String applyPortTypeDecl( int N , int B ) {

		PortTypeDeclaration bipTemplate = new PortTypeDeclaration( );
		bipTemplate.boolVar = B;
		bipTemplate.intVar = N;
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		bipTemplate.toTemplate( null , ret , null , templateFile , templateList );
		String code = ( String ) MapUtils.getSingleEntry( ret , MapKeys.BIP_CODE );
		if ( code == null ) {
			code = "";
		}
		return code;
	}


	/* String[] intLists, String fault */
	public HashMap < String , ArrayList > applyRECEIVE( Element element , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		Receive bipTemplate;
		if ( NodeName.getNodeName( element.getTagName( ) ).equals( NodeName.RECEIVE ) ) {
			bipTemplate = new Receive( );
		} else {
			bipTemplate = new Listen( );
		}

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		bipTemplate.toTemplate( stack , ret , element , templateFile , templateList );

		return ret;

	}


	public HashMap < String , ArrayList > applyASSIGN( Element element , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		Assign bipTemplate = new Assign( );

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		bipTemplate.toTemplate( stack , ret , element , templateFile , templateList );

		return ret;

	}


	public String applyHeaderHPP( ) throws Exception {

		return AttrFiller.getTemplate( templateFile , "header_HPP" ).toString( );
	}


	public String applyHeaderCPP( ) throws Exception {

		return AttrFiller.getTemplate( templateFile , "header_CPP" ).toString( );
	}


	public String applyFooter( HashMap < String , ArrayList > ret ) throws Exception {

		StringTemplate template = AttrFiller.getTemplate( templateFile , "footer" );

		template.setAttribute( "compName" , MapUtils.getSingleEntry( ret , MapKeys.CHILD_COMP ) );

		HashMap < String , ArrayList > vars = new HashMap < String , ArrayList >( );
		addAsChild( vars , ret );
		HashMap templatePortsMap = new HashMap < String , ArrayList < Object >>( );
		ret = new HashMap < String , ArrayList >( );
		exportPorts( vars , templatePortsMap , template , ret , true );

		return template.toString( );
	}


	/**
	 * Returns the wsdl files which is associated with the namespace
	 * 
	 * @param stack
	 * @param nspaceL
	 * @return
	 */
	public static ArrayList < WSDLFile > getWSDLs_in_nspaceL( ArrayDeque < HashMap < String , ArrayList >> stack , String nspaceL ,
			HashMap < String , ArrayList < String > > nspaceL_to_paths ) {

		if ( nspaceL_to_paths == null ) {
			Iterator it = stack.iterator( );
			while ( it.hasNext( ) ) {
				HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
				nspaceL_to_paths = ( HashMap < String , ArrayList < String > > ) MapUtils.getSingleEntry( vars , MapKeys.NSPACEL_TO_PATHS );
				if ( nspaceL_to_paths != null && nspaceL_to_paths.containsKey( nspaceL ) ) {
					break;
				}
			}
		}

		ArrayList < String > locations = nspaceL_to_paths.get( nspaceL );

		if ( locations == null ) {
			return null;
		}
		ArrayList < WSDLFile > files = new ArrayList < WSDLFile >( );
		for ( String location : locations ) {
			files.add( bcompiler.locsToParsedFile.get( location ) );
		}

		return files;
	}


	public ArrayList < String > getMessageParts( ArrayDeque < HashMap < String , ArrayList >> stack , String msgName ) {

		String nspaceL = msgName.split( ":" )[ 0 ];
		String localName = msgName.split( ":" )[ 1 ];

		// search all wsdls for this msgPart
		ArrayList < WSDLFile > wsdls = getWSDLs_in_nspaceL( stack , nspaceL , null );
		if ( wsdls == null ) {
			System.out.println( "null wsdls in " + nspaceL );
		}
		// check if some wsdl has the message
		for ( WSDLFile wsdl : wsdls ) {
			HashMap < String , ArrayList < String > > messages = wsdl.getMessages( );

			if ( !messages.containsKey( localName ) ) {
				continue;
			}
			return ( ArrayList < String > ) messages.get( localName );
		}
		return new ArrayList < String >( 0 );

	}


	private String getMessageOfPart( ArrayDeque < HashMap < String , ArrayList >> stack , String partName ) throws Exception {

		throw new Exception( "ERROR getMessageOfPart" );
	}


	private void createLiteralAssignPort( String compName , StringTemplate template , ArrayList < String > litValues ) throws Exception {

		for ( String litValue : litValues ) {
			template.setAttribute( "literalValue" , litValue ); /* partNames */
		}
	}


	private RwPort createReadPort( String compName , Element elem , StringTemplate template , ArrayDeque < HashMap < String , ArrayList >> stack ,
			HashMap < String , ArrayList > ret ) throws Exception {

		RwPort readPort = new RwPort( compName , "" );

		ArrayList < String > parts = new ArrayList < String >( );
		ArrayList < Element > nodes = null;

		boolean variablesNeedSorting = false;
		/* reply will either have a variable attribute or a toParts element */
		if ( elem.hasAttribute( "variable" ) ) {
			if ( elem.hasAttribute( "part" ) ) {
				parts.add( elem.getAttribute( "variable" ) + "_" + elem.getAttribute( "part" ) );
			} else if ( elem.hasAttribute( "property" ) ) {
				parts.add( elem.getAttribute( "variable" ) + "_" + elem.getAttribute( "property" ) );
			} else {

				parts.addAll( MapUtils.getVariableParts( stack , elem.getAttribute( "variable" ) ) );
			}
		} else if ( elem.hasAttribute( "inputVariable" ) ) {
			/* get var_parts, they are in form variable_part */
			parts.addAll( MapUtils.getVariableParts( stack , elem.getAttribute( "inputVariable" ) ) );

		} else if ( elem.hasAttribute( "partnerLink" ) ) { // in 'copy'
			String partnerLink = TemplateMaker.partnerLinkToBIP( elem.getAttribute( "partnerLink" ) );// TODO

			ArrayList < String > partnerLinkList = ( ArrayList < String > ) findInMapOfEnclosingScopes( stack , partnerLink , MapKeys.PARTNER_LINKS );

			if ( partnerLinkList == null ) {
				throw new Exception( "partner link not found" );
			}
			// String partnerRole = ( String ) partnerLinkList.get( 1 );
			parts.add( partnerLink + "_pRole" );
		} else if ( elem.hasAttribute( "expression" ) || elem.hasAttribute( "condition" ) ) {

			String exprText = null;
			if ( elem.hasAttribute( "expression" ) ) {
				exprText = elem.getAttribute( "expression" );
			} else if ( elem.hasAttribute( "condition" ) ) {
				exprText = elem.getAttribute( "condition" );
			}

			ArrayList < String > varParts = ExpressionParsingUtils.processExpression( elem , exprText , stack , null , template , null , "1" );
			parts.addAll( varParts );
			if ( varParts.size( ) > 0 ) {
				template.setAttribute( "invalidExpressionValue" , 1 );
				template.setAttribute( "subLanguageExecutionFault" , 1 );
				MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "invalidExpressionValue" , "subLanguageExecutionFault" );
			}

			variablesNeedSorting = true;
		} else if ( ( nodes = BPELFile.getChildrenByTagLocalName( elem , "expression" ) ) != null && nodes.size( ) > 0 ) {

			String exprText = nodes.get( 0 ).getTextContent( );
			ArrayList < String > varParts = ExpressionParsingUtils.processExpression( elem , exprText , stack , null , template , null , "1" );
			parts.addAll( varParts );
			variablesNeedSorting = true;
		} else if ( ( nodes = BPELFile.getChildrenByTagLocalName( elem , "condition" ) ) != null && nodes.size( ) > 0 ) {

			String exprText = nodes.get( 0 ).getTextContent( );
			ArrayList < String > varParts = ExpressionParsingUtils.processExpression( elem , exprText , stack , null , template , null , "1" );
			parts.addAll( varParts );
			variablesNeedSorting = true;
		} else if ( elem.getTextContent( ) != null && !elem.getTextContent( ).trim( ).equals( "" ) ) {

			// the variableName might be a message type or a part stated in
			// wsdl:
			// TODO: assign the literal (as is) to the to part or message named
			// as variableName

			return null;

		} else if ( XMLFile.getChildrenByTagLocalName( elem , "literal" ).size( ) > 0 ) {

			// create an internal port that assigns the value to the
			// fromVariable
			return null;
		} else {
			// get the 'toParts' element (in DOM)
			Element toPartsEl = null;
			if ( elem.getChildNodes( ).getLength( ) > 0 ) {
				ArrayList < Element > toParts = XMLFile.getChildrenByTagLocalName( elem , "toParts" );
				if ( toParts.size( ) > 0 ) {
					// get the indices of the 'toPart' elements (in DOM)
					for ( Element toPart : XMLFile.getChildrenByTagLocalName( toPartsEl , "toPart" ) ) {
						String part = toPart.getAttribute( "fromVariable" ) + "_" + toPart.getAttribute( "part" );
						parts.add( part );

					}
				} else {
					// handle it as literal

				}
			} else {
				// handle it as literal

			}
		}

		if ( parts.size( ) == 0 ) {
			return readPort;
		}
		MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "uninitializedVariable" );

		if ( variablesNeedSorting ) {

			int hereNums;
			Iterator it = stack.iterator( );
			while ( it.hasNext( ) ) {
				hereNums = 0;
				HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
				HashMap < String , ArrayList < String >> variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.VARIABLES );
				HashMap < String , Object > partnerLinks = ( HashMap < String , Object > ) MapUtils.getSingleEntry( vars , MapKeys.PARTNER_LINKS );

				for ( String part : parts ) {
					if ( readPort.parts( ).contains( part ) ) {
						continue;
					}

					String varName = part.split( "_" )[ 0 ];

					if ( ( variables != null && variables.containsKey( varName ) ) || ( partnerLinks != null && partnerLinks.containsKey( varName ) ) ) {
						readPort.parts( ).add( 0 , part );
						hereNums++;
					}
				}
				if ( readPort.parts( ).size( ) == parts.size( ) ) {
					break;
				}

			}

		} else {
			readPort.parts( ).addAll( parts );
		}

		parts = readPort.parts( );
		// assign the sorted partList to parts
		template.setAttribute( "varsRPorts" , parts.size( ) ); /* partNames */
		for ( String part : parts ) {
			template.setAttribute( "varsRPorts" , part ); /* partNames */
		}
		// MapUtils.addToBIPCode(applyPortTypeDecl(var_parts, 1), ret);
		MapUtils.addToBIPCode( ret , applyPortTypeDecl( parts.size( ) , 0 ) );
		// add to ret only if there are variables to read
		MapUtils.addToMapEntry( ret , MapKeys.READ_PORTS , readPort );

		return readPort;
	}


	public static String partnerLinkToBIP( String partnerLink ) {

		return partnerLink; // ideally add a suffix to avoid duplicate BIP
							// variables
	}


	public static TreeMap < String , String > extractCorrSets( Element elem ) throws XPathExpressionException {

		TreeMap < String , String > extractedCSs = new TreeMap < String , String >( );
		// read the correlationSet elements
		for ( Element cs : bcompiler.bpelFile.getChildrenInWrapper( elem , "correlations" , "correlation" ) ) {
			String initiate = "no";
			if ( cs.hasAttribute( "initiate" ) ) {
				initiate = cs.getAttribute( "initiate" );
			}
			extractedCSs.put( cs.getAttribute( "set" ) , initiate ); //
		}
		return extractedCSs;
	}


	public HashMap < String , ArrayList > applyEH( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		HashMap < String , ArrayList > vars = stack.peek( );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "EH" );
		template.setAttribute( "id" , templateList.getSize( ) + 1 );
		String compName = "eh_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "EH" , -1 );

		// add childComp to template
		ArrayList childComp = ( ArrayList ) vars.get( MapKeys.CHILD_COMP );
		for ( int i = 0 ; i < childComp.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp.get( i ) );
		}
		int count = childComp.size( );
		template.setAttribute( "count" , count );

		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );

		MapUtils.addToBIPCode( ret , applyRDV( 2 , false ) );
		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;

	}


	/**
	 * This works only in elements which shall have one child activities.
	 * 
	 * @param el
	 * @param stack
	 * @return
	 */
	public HashMap < String , ArrayList > applyTransparent( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		HashMap < String , ArrayList > vars = stack.peek( );

		int index = 0;
		while ( vars.get( MapKeys.CHILD_COMP ).get( index ) == null ) {
			index++;
		}

		// export everything from vars to ret
		for ( String key : vars.keySet( ) ) {
			if ( key.equals( MapKeys.BIP_CODE ) ) {
				continue;
			}
			if ( MapKeys.isHashMapEntry( key ) ) {
				ret.put( key , vars.get( key ) );
			} else if ( MapKeys.isSoloMapEntry( key ) ) {
				ret.put( key , ( ArrayList ) vars.get( key ).get( 0 ) );
			} else {
				ret.put( key , ( ArrayList ) vars.get( key ).get( index ) );
			}
		}
		return ret;
	}


	private HashMap < String , ArrayList > createConditionalController( Element el , HashMap < String , ArrayList > globalRet ) throws Exception {

		// create a conditional controller component
		HashMap < String , ArrayList > retLoopCtrl = new HashMap < String , ArrayList >( );
		String compName = "if_" + ( templateList.getSize( ) + 1 ); // e.g.
																	// "while_"
		StringTemplate template = prepareTemplate( "IF" , compName , "MA" , retLoopCtrl );
		MapUtils.addToMapEntry( retLoopCtrl , MapKeys.FAULTS_LIST , "default" );

		template.setAttribute( "invalidExpressionValue" , 1 );
		template.setAttribute( "subLanguageExecutionFault" , 1 );
		template.setAttribute( "uninitializedVariable" , 1 );
		template.setAttribute( "faults" , "default" );

		MapUtils.addToMapEntry( globalRet , MapKeys.FAULTS_LIST , "invalidExpressionValue" , "subLanguageExecutionFault" , "uninitializedVariable" );

		int count = 1;
		int elseIfCount = BPELFile.getChildrenByTagLocalName( el , "elseif" ).size( );
		if ( elseIfCount > 0 ) {
			count += elseIfCount;
		}

		String [ ] startdone;
		for ( int i = 0 ; i < count ; i++ ) {
			startdone = new String [ 2 ];
			startdone[ 0 ] = String.valueOf( i + 1 );
			if ( i + 2 <= count ) {
				startdone[ 1 ] = String.valueOf( i + 2 );
			}
			AttrFiller.addToTemplate( startdone , template , "startdone" );
		}

		if ( BPELFile.getChildrenByTagLocalName( el , "else" ).size( ) > 0 ) {
			count += 1;
			template.setAttribute( "hasElse" , count );
		}

		MapUtils.addToBIPCode( globalRet , template.toString( ) );
		return retLoopCtrl;
	}


	public HashMap < String , ArrayList > applyCOND( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > vars = stack.peek( );
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		HashMap < String , ArrayList > retLoopCtrl = createConditionalController( el , ret );
		addAsChild( vars , retLoopCtrl ); // adds the ret as the last component

		// place the last child to the front
		changeChildIndex( vars.get( MapKeys.CHILD_COMP ).size( ) - 1 , 0 , vars );

		// then add the compound component
		String templateName = "COND";
		String compName = "cond_" + ( templateList.getSize( ) + 1 );
		StringTemplate template = prepareTemplate( templateName , compName , "MA" , ret );

		// add childComp to template
		ArrayList childComp = ( ArrayList ) vars.get( MapKeys.CHILD_COMP );
		for ( int i = 0 ; i < childComp.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp.get( i ) );
		}

		exportPorts( vars , new HashMap( ) , template , ret , false );

		// //////
		int count = BPELFile.getChildrenByTagLocalName( el , "elseif" ).size( ) + BPELFile.getChildrenByTagLocalName( el , "else" ).size( ) + 1; // plus
																																					// one
																																					// since
																																					// at
																																					// least
																																					// one
																																					// if
		for ( int i = 0 ; i < count ; i++ ) {
			ArrayList < String > startDisPort = new ArrayList < String >( count );
			startDisPort.add( String.valueOf( i + 2 ) );
			for ( int disIndex = 0 ; disIndex < count ; disIndex++ ) {
				if ( disIndex == i ) {
					continue;
				}
				// +2 to skip controller
				startDisPort.add( String.valueOf( disIndex + 2 ) );
			}
			AttrFiller.addToTemplate( startDisPort , template , "startDis" );
		}

		template.setAttribute( "countless" , count );
		template.setAttribute( "count" , count + 1 );

		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		MapUtils.addToBIPCode( ret , applyRDV( count + 1 , true ) );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;

	}


	public HashMap < String , ArrayList > applySWITCH( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		HashMap < String , ArrayList > vars = stack.peek( );
		String templateName = "COND2";
		String compName = "cond2_" + ( templateList.getSize( ) + 1 );
		String returnedControllersName = "switch_" + ( templateList.getSize( ) + 1 );
		StringTemplate template = prepareTemplate( templateName , compName , "MA" , ret );

		// Controller is created through the same template as COND2
		HashMap < String , ArrayList > returnedController = new HashMap < String , ArrayList >( );
		MapUtils.addToMapEntry( returnedController , MapKeys.CHILD_COMP , returnedControllersName );

		addAsChild( vars , returnedController ); // adds the returnedController
													// as last component
		// place the last child to the front
		changeChildIndex( vars.get( MapKeys.CHILD_COMP ).size( ) - 1 , 0 , vars );

		// add the EVAL components
		ArrayList < Element > caseElements = BPELFile.getChildrenByTagLocalName( el , "case" );
		for ( Element elem : caseElements ) {
			HashMap < String , ArrayList > evalRet = applyBOOLEVAL( elem , stack );
			MapUtils.addToBIPCode( ret , ( String ) evalRet.get( MapKeys.BIP_CODE ).get( 0 ) );
			addAsChild( vars , evalRet );
		}
		int caseElementsSize = caseElements.size( );
		ArrayList < Integer > caseChildrenOrders = new ArrayList < Integer >( caseElementsSize );
		int otherwiseChildOrder = -1;

		ArrayList childComp = vars.get( MapKeys.CHILD_COMP );
		// ArrayList < Integer > caseElementIds = new ArrayList < Integer >( );
		for ( int i = 0 ; i < childComp.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp.get( i ) );
			if ( vars.get( MapKeys.TAG ).get( i ) == null || ( ( ArrayList ) vars.get( MapKeys.TAG ).get( i ) ).size( ) == 0 ) {
				continue;
			}
			if ( ( ( ArrayList ) vars.get( MapKeys.TAG ).get( i ) ).get( 0 ).equals( NodeName.OTHERWISE ) ) {
				otherwiseChildOrder = i + 1;
			} else if ( ( ( ArrayList ) vars.get( MapKeys.TAG ).get( i ) ).get( 0 ).equals( NodeName.CASE ) ) {
				caseChildrenOrders.add( i + 1 ); // TODO: it seems that the
													// order is not used, to be
													// removed
			} else if ( ( ( ArrayList ) vars.get( MapKeys.TAG ).get( i ) ).get( 0 ).equals( "booleval" ) ) {
				template.setAttribute( "evalComps" , i + 1 );
			}
		}

		int count = childComp.size( ); // count one for controller (always first
		// component)
		// for all evaluable case components add a 'startdone' attribute (i.e.
		// [last,next] pair)
		int triggersCount = caseChildrenOrders.size( );
		for ( int i = 0 ; i < triggersCount ; i++ ) {
			String [ ] startdone = new String [ 2 ];
			startdone[ 0 ] = String.valueOf( i + 1 );
			// except for last item
			if ( i < triggersCount - 1 ) {
				startdone[ 1 ] = String.valueOf( i + 2 );
			}
			AttrFiller.addToTemplate( startdone , template , "startdone" );
		}
		if ( otherwiseChildOrder != -1 ) {
			template.setAttribute( "hasElse" , triggersCount + 1 );
		}
		// for all case components a 'startDis' attribute (i.e.
		// [start,disab1,disab2,..] pair)
		triggersCount = ( otherwiseChildOrder != -1 ) ? caseChildrenOrders.size( ) + 1 : caseChildrenOrders.size( );
		for ( int i = 0 ; i < triggersCount ; i++ ) {
			ArrayList < String > startDisPort = new ArrayList < String >( count - 1 );
			startDisPort.add( String.valueOf( i + 2 ) );
			for ( int disIndex = 0 ; disIndex < count - 1 ; disIndex++ ) {
				if ( disIndex == i ) {
					continue;
				}
				startDisPort.add( String.valueOf( disIndex + 2 ) );
			}
			AttrFiller.addToTemplate( startDisPort , template , "startDis" );
		}

		exportPorts( vars , new HashMap( ) , template , ret , false );

		template.setAttribute( "count" , count );
		template.setAttribute( "countless" , count - 1 );

		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		MapUtils.addToBIPCode( ret , applyCP2EB( 0 , 1 ) );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , applyANY( count - 1 , true ) );
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;

	}


	public HashMap < String , ArrayList > applyLOOP( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , String type ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		HashMap < String , ArrayList > vars = stack.peek( );

		// create a loop controller component
		HashMap < String , ArrayList > retLoopCtrl = new HashMap < String , ArrayList >( );
		String compName = type + "_" + ( templateList.getSize( ) + 1 ); // e.g.
		// "while_"
		StringTemplate template = prepareTemplate( type.toUpperCase( ) , compName , "MA" , retLoopCtrl );

		if ( type.equals( "forEach" ) ) {
			String parallel = el.getAttribute( "parallel" );
			if ( parallel.equals( "" ) ) {
				parallel = "no";
			}
			String startCount = "1", finalCount = "2";
			if ( parallel.equals( "yes" ) ) {
				finalCount = "3";
				template.setAttribute( "parallel" , 1 );
			}
			ExpressionParsingUtils.processExpression( el , "startCounterValue" , stack , retLoopCtrl , template , "expression1" , startCount );
			ExpressionParsingUtils.processExpression( el , "finalCounterValue" , stack , retLoopCtrl , template , "expression2" , finalCount );
			ArrayList < Element > completionConditionElems = BPELFile.getChildrenByTagLocalName( el , "completionCondition" );

			if ( completionConditionElems.size( ) > 0 ) {
				Element condEl = completionConditionElems.get( 0 );
				ExpressionParsingUtils.processExpression( condEl , "branches" , stack , retLoopCtrl , template , "completionCondition" , "1" );
				MapUtils.addToMapEntry( retLoopCtrl , MapKeys.FAULTS_LIST , "invalidBranchCondition" );
				template.setAttribute( "invalidBranchCondition" , 1 );

				String successOnly = condEl.getAttribute( "successfulBranchesOnly" );
				if ( successOnly.isEmpty( ) ) {
					successOnly = "no";
				}
				if ( !successOnly.equals( "no" ) ) {
					template.setAttribute( "successfulBranchesOnly" , 1 );
					template.setAttribute( "completionConditionFailure" , 1 );
					MapUtils.addToMapEntry( retLoopCtrl , MapKeys.FAULTS_LIST , "completionConditionFailure" );

				}
			}

		} else {
			if ( type.equals( "while" ) ) {
				RwPort readPort = createReadPort( compName , el , template , stack , retLoopCtrl );

			}

		}

		MapUtils.addToBIPCode( ret , template.toString( ) );

		// add the loop controller component to the children
		addAsChild( vars , retLoopCtrl ); // adds the ret as the last component

		// place the last child to the front
		changeChildIndex( vars.get( MapKeys.CHILD_COMP ).size( ) - 1 , 0 , vars );

		// then add the loop component(compound)
		String templateName = "LOOP";
		compName = "loop_" + ( templateList.getSize( ) + 1 );
		template = prepareTemplate( "LOOP" , compName , "MA" , ret );

		// add childComp to template
		ArrayList childComp = ( ArrayList ) vars.get( MapKeys.CHILD_COMP );
		for ( int i = 0 ; i < childComp.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp.get( i ) );
		}

		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );

		// //////
		int count = childComp.size( );
		if ( type.equals( "forEach" ) ) {
			template.setAttribute( "forEach" , 1 );
			String parallel = el.getAttribute( "parallel" );
			if ( parallel.equals( "" ) ) {
				parallel = "no";
			}
			if ( parallel.equals( "no" ) ) {
				count = 2;
			} else {
				template.setAttribute( "parallel" , 1 );
			}
		}
		template.setAttribute( "count" , count );

		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , applyBRDCAST( 3 , 1 , false ) );
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;

	}


	private StringTemplate prepareTemplate( String tmplName , String compName , String scopeRole , HashMap < String , ArrayList > ret )
			throws Exception {

		StringTemplate template = AttrFiller.getTemplate( templateFile , tmplName );
		template.setAttribute( "id" , templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , scopeRole , -1 );
		return template;
	}


	private HashMap < String , ArrayList > applyALARM( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		String compName = "alarm_" + ( templateList.getSize( ) + 1 );
		String tmplName = "ALARM";
		StringTemplate template = prepareTemplate( tmplName , compName , "MA" , ret );
		MapUtils.addToBIPCode( ret , applyPortTypeDecl( 2 , 0 ) );

		// for and until can be either element or attribute
		String timeExp = "", repeatExp = "";
		boolean isDeadline = false, hasRepeat = false;
		ArrayList < Element > elems;
		if ( elem.hasAttribute( "until" ) ) {
			isDeadline = true;
			timeExp = elem.getAttribute( "until" );
		} else if ( ( elems = XMLFile.getChildrenByTagLocalName( elem , "until" ) ).size( ) > 0 ) {
			isDeadline = true;
			timeExp = elems.get( 0 ).getTextContent( );
		} else if ( elem.hasAttribute( "for" ) ) {
			isDeadline = false;
			timeExp = elem.getAttribute( "for" );
		} else if ( ( elems = XMLFile.getChildrenByTagLocalName( elem , "for" ) ).size( ) > 0 ) {
			isDeadline = false;
			timeExp = elems.get( 0 ).getTextContent( );
		}
		if ( elem.hasAttribute( "repeatEvery" ) ) {
			hasRepeat = true;
			repeatExp = elem.getAttribute( "repeatEvery" );
		} else if ( ( elems = XMLFile.getChildrenByTagLocalName( elem , "repeatEvery" ) ).size( ) > 0 ) {
			hasRepeat = true;
			repeatExp = elems.get( 0 ).getTextContent( );
		}

		if ( isDeadline ) {
			template.setAttribute( "until" , timeExp );
		} else {
			template.setAttribute( "for" , timeExp );
		}
		if ( hasRepeat ) {
			template.setAttribute( "repeatEvery" , repeatExp );
		}
		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;
	}


	public HashMap < String , ArrayList > applyONALARM( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		// create the alarm listener and add its returned to the vars
		HashMap < String , ArrayList > retReceive = applyALARM( elem , stack );
		HashMap < String , ArrayList > vars = stack.peek( );
		addAsChild( vars , retReceive ); // adds the ret as the last component

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		String compName = "onAlarm_" + ( templateList.getSize( ) + 1 );
		String tmplName = "ONALARM";

		StringTemplate template = prepareTemplate( tmplName , compName , "MA" , ret );

		// add code from receive to ret
		String codeFromAlarm = ( String ) MapUtils.getSingleEntry( retReceive , MapKeys.BIP_CODE );
		MapUtils.addToBIPCode( ret , codeFromAlarm );

		// bring the act's returned to the 2nd or 1st place in vars
		// if parent is eventHandler and if there is a repeatEvery
		int fromPlace = ( needsDuplicateComponent( elem ) ) ? 2 : 1;

		changeChildIndex( fromPlace , 0 , vars );

		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );

		// add childComp to template
		ArrayList childComp = ( ArrayList ) vars.get( MapKeys.CHILD_COMP );
		for ( int i = 0 ; i < childComp.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp.get( i ) );
		}
		int count = childComp.size( );
		template.setAttribute( "count" , count );

		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , applyBRDCAST( count , 1 , false ) );
		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;

	}


	public static boolean needsDuplicateComponent( Element elem ) {

		if ( ( elem.getNodeName( ).endsWith( "forEach" ) && ( elem.getAttribute( "parallel" ) != null ) && elem.getAttribute( "parallel" ) == "yes" ) ) {
			return true;
		} else if ( elem.getParentNode( ).getNodeName( ).endsWith( "eventHandlers" ) && elem.getNodeName( ).endsWith( "onEvent" ) ) {
			return true;
		} else if ( elem.getParentNode( ).getNodeName( ).endsWith( "eventHandlers" ) && elem.getNodeName( ).endsWith( "onMessage" ) ) {
			return true;
		} else if ( elem.getNodeName( ).endsWith( "repeatUntil" ) ) {
			return true;
		} else if ( elem.getNodeName( ).endsWith( "while" ) ) {
			return true;
		} else if ( elem.getNodeName( ).endsWith( "flowN" ) ) {
			return true;
		} else
			return false;
	}


	public HashMap < String , ArrayList > applyONMESSAGE( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		// create the listener and add its returned to the vars
		HashMap < String , ArrayList > retReceive = applyRECEIVE( elem , stack );
		HashMap < String , ArrayList > vars = stack.peek( );
		addAsChild( vars , retReceive ); // adds the ret as the last component

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		String compName = "onMessage_" + ( templateList.getSize( ) + 1 );
		String tmplName = "ONMESSAGE";

		StringTemplate template = prepareTemplate( tmplName , compName , "MA" , ret );

		// add code from receive to ret
		String codeFromReceive = ( String ) MapUtils.getSingleEntry( retReceive , MapKeys.BIP_CODE );
		MapUtils.addToBIPCode( ret , codeFromReceive );
		if ( NodeName.getNodeName( ( ( Element ) elem.getParentNode( ) ).getTagName( ) ).equals( NodeName.PICK )
				&& !NodeName.getNodeName( ( ( Element ) elem ).getTagName( ) ).equals( NodeName.ON_ALARM ) ) {
			// add rcvMsgPorts and as onMsgPorts to ret
			ArrayList onMsgPorts = ( ArrayList ) retReceive.get( MapKeys.RCV_MESSAGE_PORTS );
			ret.put( MapKeys.ON_MESSAGE_PORTS , onMsgPorts );
		}
		// bring the act's returned to the 2nd or 1st place in vars
		// if parent is not pick and if there is a repeatEvery
		int fromPlace = ( needsDuplicateComponent( elem ) ) ? 2 : 1;
		changeChildIndex( fromPlace , 0 , vars );

		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );
		if ( NodeName.getNodeName( ( ( Element ) elem.getParentNode( ) ).getTagName( ) ).equals( NodeName.PICK )
				&& !NodeName.getNodeName( ( ( Element ) elem ).getTagName( ) ).equals( NodeName.ON_ALARM ) ) {
			ret.remove( MapKeys.RCV_MESSAGE_PORTS );
		}

		// add childComp to template
		ArrayList childComp = ( ArrayList ) vars.get( MapKeys.CHILD_COMP );
		for ( int i = 0 ; i < childComp.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp.get( i ) );
		}
		int count = childComp.size( );
		template.setAttribute( "count" , count );

		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , applyBRDCAST( count , 1 , false ) );
		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;

	}


	/* String[] intLists, String fault */
	public HashMap < String , ArrayList > applyREPLY( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		String s = "\n";
		s += applyCHKMR( false );
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		ret.put( "sndMsgPorts" , new ArrayList < Object >( 0 ) );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );

		s += applyPortTypeDecl( 0 , 1 );
		s += applyPortTypeDecl( 1 , 0 );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "REPLY" );

		template.setAttribute( "id" , templateList.getSize( ) + 1 );
		String compName = "reply_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		String partnerLink = elem.getAttribute( "partnerLink" );
		String operation = elem.getAttribute( "operation" );
		String messageExchange = "";
		if ( elem.hasAttribute( "messageExchange" ) ) {
			messageExchange = elem.getAttribute( "messageExchange" );
		}

		MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "missingRequest" , "uninitializedVariable" );

		// //////////////////////////////////////////////////////////////////
		IOMsgPort newOma = new IOMsgPort( compName , "" , operation , partnerLink , messageExchange );
		newOma.addVarParts( stack , elem );

		MapUtils.addToMapEntry( ret , MapKeys.OMA_PORTS , newOma );

		newOma.setScopeOpenIMA( stack , partnerLink , messageExchange );
		newOma.setScopePartnerLink( stack , partnerLink );
		Map < String , String > extractedCSs = extractCorrSets( elem );
		newOma.addIMAInfo( elem , extractedCSs , template , stack , "initiateList" );
		s += applyPortTypeDecl( newOma.getOrderedCSs( ).size( ) , 2 );
		if ( newOma.isWithCS( ) ) {
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "correlationViolation" );
		}
		// /////////////////////////////////////////////////////////////////

		RwPort readPort = createReadPort( compName , elem , template , stack , ret );

		// //////////////////////////////////////////////////////////////////
		MapUtils.addToMapEntry( ret , "sndMsgPorts" , newOma.clonePort( ) );

		s += "\n\n" + template.toString( );
		MapUtils.addToBIPCode( ret , s );

		return ret;
	}


	/**
	 * Find the scope structure in <b>stack</b> whose <i>mapKey</i> entry
	 * contains the <i>searchValue</i>
	 * 
	 * @param stack
	 * @param searchValue
	 * @param mapKey
	 * @return
	 */
	public static Object findInMapOfEnclosingScopes( ArrayDeque < HashMap < String , ArrayList >> stack , String searchValue , String mapKey ) {

		Iterator < HashMap < String , ArrayList >> it = stack.iterator( );
		HashMap < String , ArrayList > vars;
		while ( ( vars = it.next( ) ) != null ) {
			HashMap < String , Object > varsEntry = ( HashMap < String , Object > ) MapUtils.getSingleEntry( vars , mapKey );
			if ( varsEntry == null || !varsEntry.containsKey( searchValue ) ) {
				continue;
			}
			return varsEntry.get( searchValue );
		}
		return null;
	}


	public static WSDLFile findReferencedWsdl( ArrayDeque < HashMap < String , ArrayList >> stack , String nspacedName , String tag ,
			HashMap < String , ArrayList < String > > nspaceL_to_paths ) {

		// get the wsdl of this partnerLinkType
		String nspace = nspacedName.split( ":" )[ 0 ];
		String name = nspacedName.split( ":" )[ 1 ];
		// String partnerRoleName = partnerRole.split( ":" )[ 1 ];

		// get wsdls and search the type in each
		ArrayList < WSDLFile > wsdls = getWSDLs_in_nspaceL( stack , nspace , nspaceL_to_paths );
		for ( WSDLFile wsdl : wsdls ) {
			if ( tag.equals( "partnerLinkType" ) ) {
				if ( !wsdl.getPartnerLinkTypes( ).containsKey( name ) ) {
					continue;
				}
			} else if ( tag.equals( "portType" ) ) {
				if ( !wsdl.getPortTypes( ).containsKey( name ) ) {
					continue;
				}
			}
			// portType = wsdl.getPortType( partnerLinkTypeName ,
			// partnerRoleName );
			// if ( wsdl.getPortType( partnerLinkTypeName , partnerRoleName
			// )==null ) {
			// continue;
			// }
			return wsdl;
		}
		return null;
	}


	public static String getPortType( ArrayDeque < HashMap < String , ArrayList >> stack , Element elem , WSDLFile wsdl_spec ) {

		if ( elem.getAttribute( "portType" ) == null ) {
			ArrayList < String > partnerLinkEntry = ( ArrayList < String > ) findInMapOfEnclosingScopes( stack , elem.getAttribute( "partnerLink" ) ,
					MapKeys.PARTNER_LINKS );
			String partnerLinkType = partnerLinkEntry.get( 0 );
			String partnerRole = partnerLinkEntry.get( 1 );
			String portType_name = wsdl_spec.getPortType( partnerLinkType , partnerRole );

			return portType_name;
		} else {
			// return elem.getAttribute( "portType" ).split( ":" )[ 1 ];
			return elem.getAttribute( "portType" );
		}
	}


	public static WSDLFile findMessageActivityWSDL( ArrayDeque < HashMap < String , ArrayList >> stack , Element elem ) {

		WSDLFile wsdl_spec = null;
		String partnerLink = elem.getAttribute( "partnerLink" );
		String portType = elem.getAttribute( "portType" );
		if ( portType == null ) {
			// if portType is not specified, find the operation in the
			// partnerLink type of the specified partnerLink
			ArrayList < String > partnerLinkEntry = ( ArrayList < String > ) findInMapOfEnclosingScopes( stack , partnerLink , MapKeys.PARTNER_LINKS );
			String partnerLinkType = partnerLinkEntry.get( 0 );
			String partnerRole = partnerLinkEntry.get( 1 );

			wsdl_spec = findReferencedWsdl( stack , partnerLinkType , "partnerLinkType" , null );
			portType = wsdl_spec.getPartnerLinkTypes( ).get( partnerLinkType ).get( partnerLinkType );
			if ( portType.contains( ":" ) ) {
				wsdl_spec = findReferencedWsdl( stack , portType , "portType" , wsdl_spec.getNspace2Locs( ) );
			}
		} else {
			wsdl_spec = findReferencedWsdl( stack , portType , "portType" , null );
		}
		return wsdl_spec;
	}


	/* String[] intList, String fault */
	public HashMap < String , ArrayList > applyINVOKE( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "uninitializedVariable" );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "INVOKE" );
		// String s = "";
		template.setAttribute( "id" , templateList.getSize( ) + 1 );
		String compName = "invoke_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		String operation = elem.getAttribute( "operation" );
		String partnerLink = elem.getAttribute( "partnerLink" );

		// //////////////////////////////////////////////////////////////////

		// ///////////////////////////////////////////////////////////////////

		/* One outgoing message */
		IOMsgPort sndMsg = new IOMsgPort( compName , "" , operation , partnerLink , "" );
		MapUtils.addToMapEntry( ret , MapKeys.INV_PORTS , sndMsg );
		sndMsg.addVarParts( stack , elem );
		RwPort readPort = createReadPort( compName , elem , template , stack , ret );

		/* One incoming message */
		IOMsgPort rcvMsg = new IOMsgPort( compName , "" , operation , partnerLink , "" );
		rcvMsg.addVarParts( stack , elem );
		if ( elem.hasAttribute( "outputVariable" ) ) {
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "invalidVariables" );
			MapUtils.addToMapEntry( ret , MapKeys.INV_IMA_PORTS , rcvMsg );
			MapUtils.addToMapEntry( ret , MapKeys.RCV_MESSAGE_PORTS , rcvMsg.clonePort( ) );

			RwPort writePort = new RwPort( );
			writePort.parsePort( compName , stack , elem , ret );
			writePort.applyTemplate( template );
			// writePort.applyTemplate(template, stack, ret);
		}

		ArrayList < Element > cssElems = XMLFile.getChildrenInWrapper( elem , "correlations" , "correlation" );
		HashMap < String , String > CSIns = new HashMap < String , String >( );
		HashMap < String , String > CSOuts = new HashMap < String , String >( );
		if ( cssElems.size( ) > 0 ) {
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "correlationViolation" );
			for ( Element csElement : cssElems ) {
				String initiate = ( csElement.hasAttribute( "initiate" ) ) ? csElement.getAttribute( "initiate" ) : "no";
				String pattern = ( csElement.hasAttribute( "pattern" ) ) ? pattern = csElement.getAttribute( "pattern" ) : "request";
				if ( pattern.equals( "request" ) ) {
					CSOuts.put( csElement.getAttribute( "name" ) , initiate );
				} else if ( pattern.equals( "response" ) ) {
					CSIns.put( csElement.getAttribute( "name" ) , initiate );
				} else {
					CSIns.put( csElement.getAttribute( "name" ) , initiate );
					CSOuts.put( csElement.getAttribute( "name" ) , initiate );
				}

			}
		}

		rcvMsg.addIMAInfo( elem , CSIns , template , stack , "initiateListIn" );
		sndMsg.addIMAInfo( elem , CSOuts , template , stack , "initiateListOut" );

		// ///////////////////////////////////////////////////////////////////

		// ////////////////////////////////////////////////////////////////////

		// find the port type that supports the operation, and the wsdl where it
		// is specified

		WSDLFile wsdl_spec = findMessageActivityWSDL( stack , elem );
		String portType = getPortType( stack , elem , wsdl_spec );

		// find a possible fault
		String faultName = "";
		String [ ] faultEntry = wsdl_spec.getOperationsFault( portType , operation );
		if ( faultEntry[ 0 ] != null ) {
			faultName = faultEntry[ 0 ] + "_" + faultEntry[ 1 ];
			int faultCode = faults.getFaultName( faultName );
			faultName = faultName.replace( ":" , "__" );
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , faultName );
			template.setAttribute( "faultName" , faultName );
			template.setAttribute( "faultCode" , faultCode );
		}

		// //////////////////////////////////////////////////

		String partnerRole = "";
		String initializePRole = "";
		ArrayList < String > partnerLinkList = ( ArrayList < String > ) findInMapOfEnclosingScopes( stack , partnerLink , MapKeys.PARTNER_LINKS );
		if ( partnerLinkList != null ) {
			initializePRole = ( String ) partnerLinkList.get( 2 );
			partnerRole = ( String ) partnerLinkList.get( 1 );
			if ( initializePRole.equals( "yes" ) ) {
				template.setAttribute( "initializePRole" , "0" );
			} else {
				MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "uninitializedPartnerRole" );
			}
		} else {
			throw new Exception( "partner link not found" );
		}

		sndMsg.setPartnerRole( partnerRole );
		sndMsg.setInitializePRole( initializePRole );

		// //////////////////////////////////////////////////
		MapUtils.addToMapEntry( ret , "sndMsgPorts" , sndMsg.clonePort( ) );

		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;
	}


	private HashMap < String , ArrayList > findEnclosingScope( ArrayDeque < HashMap < String , ArrayList >> stack , boolean forCompensate ) {

		HashMap < String , ArrayList > vars = new HashMap < String , ArrayList >( );
		// go back scopes, if (forCompensate==true) go past the FCT handler
		// (e.g. maybe a scope is inside them, go past it)
		boolean inFCT = true;
		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			vars = ( HashMap < String , ArrayList > ) it.next( );
			NodeName tagName = null;
			if ( ( tagName = MapUtils.readTag( vars ) ) == null ) {
				continue;
			}

			if ( // vars.containsKey("TAG") &&
			tagName.equals( NodeName.FAULT_HANDLERS ) || tagName.equals( NodeName.COMPENSATION_HANDLER )
					|| tagName.equals( NodeName.TERMINATION_HANDLER ) ) {
				inFCT = false;
			} else if ( // vars.containsKey("TAG") &&
			tagName.equals( NodeName.SCOPE ) || tagName.equals( NodeName.PROCESS ) ) {
				if ( forCompensate && inFCT ) {
					continue;
				} else {
					break;
				}
			}
		}

		return vars;
	}


	/**
	 * 
	 * @param elem
	 *            May be null, if
	 * @param stack
	 * @return
	 */
	public HashMap < String , ArrayList > applyCOMPENSATE( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		MapUtils.addToMapEntry( ret , "faultNames" , "default" );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "COMPENSATE" );
		String scopeName = null;
		Integer scopeCompenId = -1;
		if ( elem != null ) {
			scopeName = sanitize( elem.getAttribute( "target" ) );
		}
		if ( scopeName == null ) {
			scopeName = "";
		}
		String id = String.valueOf( templateList.getSize( ) + 1 );
		String compName = "Compensate";
		if ( scopeName.equals( "" ) ) {
			compName += "_" + id;

			template.setAttribute( "scopeCompenId" , 0 ); // not necessary

		} else {
			template.setAttribute( "scopeName" , "_" + scopeName );
			compName += "_" + scopeName + "_" + id;

			// if the scope has been translated already, it will be in the
			// scope2ids map
			// else add it in the map
			HashMap < String , ArrayList > enclScope = findEnclosingScope( stack , true );
			HashMap < String , Integer > scopes2ids = ( HashMap < String , Integer > ) MapUtils.getSingleEntry( enclScope , MapKeys.SCOPE_IDS );
			if ( scopes2ids.containsKey( scopeName ) ) {
				scopeCompenId = scopes2ids.get( scopeName );
			} else {
				scopeCompenId = scopes2ids.size( ) + 1;
				scopes2ids.put( scopeName , scopeCompenId );
			}
			template.setAttribute( "scopeCompenId" , scopeCompenId );
		}
		templateList.addInstance( compName );

		CompenPort compPort = new CompenPort( compName , "" , scopeName );
		MapUtils.addToMapEntry( ret , MapKeys.COMPEN_PORTS , compPort );
		template.setAttribute( "id" , templateList.getSize( ) );

		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		String s = "";
		s += applyPortTypeDecl( 2 , 0 );
		s += applyPortTypeDecl( 1 , 0 );
		s += "\n\n" + template.toString( );
		MapUtils.addToBIPCode( ret , s );
		return ret;
	}


	public HashMap < String , ArrayList > applyExit( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		String compName = "Exit_" + ( templateList.getSize( ) + 1 );
		String tmplName = "EXIT";
		StringTemplate template = prepareTemplate( tmplName , compName , "MA" , ret );
		ArrayList exitPort = ( ArrayList ) Arrays.asList( new int [ ] { 1 } );
		MapUtils.addToMap( ret , MapKeys.EXIT_PORTS , exitPort , -1 );
		return MapUtils.addToBIPCode( ret , template.toString( ) );
	}


	public HashMap < String , ArrayList > applyThrow( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		String compName = "Throw_" + ( templateList.getSize( ) + 1 );
		String tmplName = "THROW";
		StringTemplate template = prepareTemplate( tmplName , compName , "MA" , ret );

		ArrayList throwPort = new ArrayList( 1 );
		throwPort.add( 1 );

		String catchCompleteName = getFaultCompleteName( elem , stack );
		// if this is a custom fault
		int faultCode = ( catchCompleteName.equals( "all" ) ) ? 0 : faults.getFaultName( catchCompleteName );
		template.setAttribute( "faultCode" , faultCode );
		MapUtils.addToMap( ret , MapKeys.FAULTS_LIST , throwPort , -1 );
		return MapUtils.addToBIPCode( ret , template.toString( ) );
	}


	public HashMap < String , ArrayList > applyRethrow( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		String compName = "Rethrow_" + ( templateList.getSize( ) + 1 );
		String tmplName = "RETHROW";
		StringTemplate template = prepareTemplate( tmplName , compName , "MA" , ret );

		ArrayList rethrowPort = new ArrayList( 1 );
		rethrowPort.add( 1 );
		MapUtils.addToMap( ret , MapKeys.RETHROW_PORTS , rethrowPort , -1 );
		return MapUtils.addToBIPCode( ret , template.toString( ) );
	}


	public HashMap < String , ArrayList > applyCOPY( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		StringTemplate template = AttrFiller.getTemplate( templateFile , "COPY" );
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		String compName = "copy_" + ( templateList.getSize( ) + 1 );
		template.setAttribute( "id" , ( templateList.getSize( ) + 1 ) );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		/* get the read partsList */
		Element fromElement = XMLFile.getChildrenByTagLocalName( elem , "from" ).get( 0 );

		// ///////////////////////////////////////////////////////////
		RwPort readPort = createReadPort( compName , fromElement , template , stack , ret );

		// //////////////////////////////////////////////////////////////////////

		/* get the write partsList */
		Element toElement = XMLFile.getChildrenByTagLocalName( elem , "to" ).get( 0 );

		RwPort writePort = new RwPort( );
		writePort.parsePort( compName , stack , toElement , ret );
		writePort.applyTemplate( template );
		// /////////////////////////////////////////////////////////
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;
	}


	public HashMap < String , ArrayList > applySCOPE( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack , boolean process )
			throws Exception {

		// this function does not call exportPorts
		String s = "\n";
		s += applyPortTypeDecl( 2 , 0 );
		StringTemplate template = ( process ) ? AttrFiller.getTemplate( templateFile , "PROC" ) : AttrFiller.getTemplate( templateFile , "SCOPE" );

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );
		MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "missingReply" );

		// SEE WHICH SCOPE ROLES EXIST
		// the ArrayLists of the entries may have empty places
		HashMap < String , ArrayList > vars = stack.peek( );
		ArrayList varsChildComp = vars.get( MapKeys.CHILD_COMP );
		String [ ] childComp = new String [ varsChildComp.size( ) ];
		for ( int i = 0 ; i < varsChildComp.size( ) ; i++ ) {
			if ( varsChildComp.get( i ) == null ) {
				childComp[ i ] = null;
			} else {
				childComp[ i ] = ( String ) ( ( ArrayList ) varsChildComp.get( i ) ).get( 0 );
			}
		}

		/* track the integer positions of each scope Role in a map */
		// scopeRoles entry is an arrayList of the scopeRoles that exist in the
		// scope
		ArrayList childrenTag = vars.get( MapKeys.SCOPE_ROLES );
		for ( int i = 0 ; i < childrenTag.size( ) ; i++ ) {
			if ( childrenTag.get( i ) == null ) {
				continue;
			}
			String role = MapUtils.getScopeRole( childrenTag , i );

			/* for each role set the component type attribute to the template */
			if ( role.equals( "MA" ) | role.equals( "EH" ) | role.equals( "FH" ) | role.equals( "CH" ) | role.equals( "TH" ) ) {
				if ( childComp[ i ] != null ) {
					template.setAttribute( role.toLowerCase( ) , childComp[ i ] );
				}
			}
		}

		if ( template.getAttribute( "eh" ) == null ) {
			MapUtils.addToBIPCode( ret , applyEMPTYHANDLER( "eh" ) );
		}
		if ( template.getAttribute( "fh" ) == null ) {
			HashMap < String , ArrayList > retFH = applyDEFAULT_FH( elem , stack );
			addAsChild( vars , retFH );
			template.setAttribute( "fh" , retFH.get( MapKeys.CHILD_COMP ).get( 0 ) );

		}
		if ( template.getAttribute( "ch" ) == null ) {
			if ( process ) {
				MapUtils.addToBIPCode( ret , applyEMPTYHANDLER( "ch" ) );
			} else {
				if ( vars.containsKey( MapKeys.RVRS_PORTS ) && vars.get( MapKeys.RVRS_PORTS ).size( ) > 0 ) {
					HashMap < String , ArrayList > retCH = applyDEFAULT_CH( elem , stack );
					addAsChild( vars , retCH );
					template.setAttribute( "ch" , retCH.get( MapKeys.CHILD_COMP ).get( 0 ) );
				} else {
					MapUtils.addToBIPCode( ret , applyEMPTYHANDLER( "ch" ) );
				}
			}
		}
		if ( template.getAttribute( "th" ) == null ) {
			if ( process ) {
				MapUtils.addToBIPCode( ret , applyEMPTYHANDLER( "th" ) );
			} else {
				if ( vars.containsKey( MapKeys.RVRS_PORTS ) && vars.get( MapKeys.RVRS_PORTS ).size( ) > 0 ) {
					HashMap < String , ArrayList > retTH = applyDEFAULT_TH( elem , stack );
					addAsChild( vars , retTH );
					template.setAttribute( "th" , retTH.get( MapKeys.CHILD_COMP ).get( 0 ) );
				} else {
					MapUtils.addToBIPCode( ret , applyEMPTYHANDLER( "th" ) );
				}
			}
		}

		s += applyRDV( 2 , false );
		s += applyRDV( 2 , true );
		s += applyRDV( 3 , false );
		s += applyRDV( 3 , true );
		s += applyRDV( 6 , true );
		s += applyANY( 6 , false );
		s += applyANY( 6 , true );
		s += applyANY( 5 , true );
		s += applyANY( 4 , true );
		s += applyBRDCAST( 2 , 1 , false );
		s += applyBRDCAST( 6 , 1 , false );
		s += applyBRDCAST( 2 , 1 , true );
		s += applyBRDCAST( 5 , 1 , true );
		s += applyAAS( new int [ ] { 1 } , true );
		s += applyRDV( 6 , true );

		s += applyPortTypeDecl( 1 , 0 );
		s += applyPortTypeDecl( 2 , 0 );
		// add connectors for reverse/reversed/faultComp in scope
		s += applyConnTemplate( "FLTX" );
		// add connectors for export/handling faultComp in scope
		s += applyConnTemplate( "FLTH" );
		s += applyConnTemplate( "CNRVRS" );
		s += applyConnTemplate( "CNRVRSD" );
		s += applyConnTemplate( "CNTRMRVRS" );
		s += applyConnTemplate( "ASSC1" );
		s += applyConnTemplate( "FLTCMPSC" );

		/*
		 * if (!process) { int isolated = 0; if (elem.hasAttribute("isolated"))
		 * { if (elem.getAttribute("isolated").equals("yes")) { isolated = 1; }
		 * } template.setAttribute("isolated", isolated); }
		 */

		String templateId = ( String ) MapUtils.getSingleEntry( vars , MapKeys.ID );
		template.setAttribute( "id" , templateId );

		String scopeName = sanitize( elem.getAttribute( "name" ) );
		String extendedScopeName = scopeName + "_" + templateId;
		template.setAttribute( "name" , extendedScopeName );
		template.setAttribute( "shortName" , scopeName );

		// if the scope's compensation activity has been translated already,
		// the scopename will be in the scope2ids map else add it in the map

		if ( !process ) {
			int scopeCompenId = -1;
			HashMap < String , ArrayList > enclScope = findEnclosingScope( stack , true );
			HashMap < String , Integer > scopes2ids = ( HashMap < String , Integer > ) MapUtils.getSingleEntry( enclScope , MapKeys.SCOPE_IDS );
			if ( scopes2ids.containsKey( scopeName ) ) {
				scopeCompenId = scopes2ids.get( scopeName );
			} else {
				scopeCompenId = scopes2ids.size( ) + 1;
				scopes2ids.put( scopeName , scopeCompenId );
			}
			template.setAttribute( "scopeCompenId" , scopeCompenId );
		}
		// ///////////////////////////////
		String compName = "";
		if ( process ) {
			compName = "proc_" + extendedScopeName;
		} else {
			compName = "scope_" + extendedScopeName;
		}

		// replace the templateId with the compName
		templateList.rmvInstance( templateId );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );
		// add a reverse port
		if ( !process ) {
			// add a port that will enable the scope to be reversed
			RvrsPort rvrsPort = new RvrsPort( scopeName , "" );
			MapUtils.addToMapEntry( ret , MapKeys.RVRS_PORTS , rvrsPort );
		}

		HashMap templatePortsMap = new HashMap( );
		// manageReadWritePorts: StringTemplateART
		// 1: declare map entries for ports' transformation
		ArrayList < String > entries = new ArrayList < String >( Arrays.asList( new String [ ] { MapKeys.READ_PORTS , MapKeys.WRITE_PORTS } ) );
		// 2: initialize reusable info by adding entries to vars
		initializePortsTransform( vars , entries );
		// 3: transform ports
		transformMapEntryPorts( vars , templatePortsMap , template , ret , entries , false );
		// 4: do custom jobs depending on the activity under translation
		HashSet < String > dataList; // the var.parts in scope that were used
										// either for read or write

		if ( ( dataList = ( HashSet < String > ) vars.get( MapKeys.DATA_LIST ).get( 0 ) ) != null ) {
			// dataList.addAll( plSet );
			for ( String data : ( HashSet < String > ) dataList ) {
				template.setAttribute( "dataList" , data );
			}
		}
		// 5: erase temporary map entries
		finalizePortsTransform( vars );
		// manageReadWritePorts: END

		// manage reverse/reversed ports of child components
		ArrayList < Object > components = null;
		// add scope's reversible roles (i.e. with scopes) to rvrsRoles
		HashMap < String , ArrayList < RvrsPort >> roles2rvrsPorts = new HashMap < String , ArrayList < RvrsPort >>( 5 );
		HashMap < String , RvrsPort > scopes2rvrsPorts = new HashMap < String , RvrsPort >( 0 );
		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.RVRS_PORTS ) ) != null && components.size( ) > 0 ) {

			scopes2rvrsPorts = new HashMap < String , RvrsPort >( components.size( ) );
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( components.get( i ) == null ) {
					continue;
				}
				String scopeRole = MapUtils.getScopeRole( childrenTag , i );
				ArrayList < RvrsPort > componentsPorts = ( ArrayList < RvrsPort > ) components.get( i );

				roles2rvrsPorts.put( scopeRole , componentsPorts );
				for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
					RvrsPort rvrsPort = ( RvrsPort ) componentsPorts.get( j );
					rvrsPort.setCompIndex( scopeRole ); // add the current
														// scopeRole to port
					scopes2rvrsPorts.put( rvrsPort.getCompName( ) , rvrsPort );
				}

			}
		}

		ArrayList < Object > compPorts;
		if ( ( compPorts = vars.get( MapKeys.COMPEN_PORTS ) ) != null ) {

			s += applyPortTypeDecl( 2 , 0 );
			s += applyConnTemplate( "FLTCMP" );

			int MAThrowsFault = -1; // undefined
			int EHThrowsFault = -1; // undefined

			for ( int i = 0 ; i < compPorts.size( ) ; i++ ) {

				ArrayList < Object > component = ( ArrayList < Object > ) compPorts.get( i );
				if ( component == null ) {
					continue;
				}
				for ( int j = 0 ; j < component.size( ) ; j++ ) {
					CompenPort compPort = ( CompenPort ) component.get( j );

					String scopeCompRole = MapUtils.getScopeRole( childrenTag , i );
					compPort.setCompIndex( scopeCompRole ); /* scopeRole */

					// if the compensation port does not have a target
					// scope
					if ( compPort.getTargetScope( ).equals( "" ) ) {
						// the conn types are different, if there are not
						// reversible activities in
						if ( roles2rvrsPorts.size( ) > 0 ) {
							s += applyPRRVRSX( roles2rvrsPorts.size( ) );
							s += applyPRENDRVRSX( roles2rvrsPorts.size( ) );
							s += applyTRMRVSD( roles2rvrsPorts.size( ) );
							s += applyCMP( 1 );
							s += applyENDCMP( 1 );
							s += applyTRMCMP( 1 );
						} else {
							s += applyCMP( 0 );
							s += applyENDCMP( 0 );
							s += applyTRMCMP( 0 );
						}

						// s += applyHLDCMP();
						// s += applyHLDENDCMP();

						// if the compensate port comes from an
						// FCT-Handler, add a rvrsAllPort
						if ( scopeCompRole.equals( "CH" ) || scopeCompRole.equals( "FH" ) || scopeCompRole.equals( "TH" ) ) {
							// add a conn between the compensator and
							// every reverSIble component

							// add the reversible roles to the port
							compPort.getTargetRoles( ).addAll( roles2rvrsPorts.keySet( ) );

							s += applyCMP( compPort.getTargetRoles( ).size( ) );
							s += applyENDCMP( compPort.getTargetRoles( ).size( ) );

							ArrayList < String > toTemplate = compPort.toArray4Template( "compensatePort" );
							AttrFiller.addToTemplate( toTemplate , template , "compensatePorts" );

							// if we havn't searched before (undefined)
							if ( MAThrowsFault == -1 ) {
								// read each ports throwsFault()
								if ( roles2rvrsPorts.containsKey( "MA" ) ) {
									for ( RvrsPort rvrsPort : roles2rvrsPorts.get( "MA" ) ) {
										// once found throwsFault, define
										// MAThrowsFault
										MAThrowsFault = ( rvrsPort.throwsFault( ) ) ? 1 : -1;
									}
								}
								// if still MAThrowsFault -1, set 0
								MAThrowsFault = ( MAThrowsFault == -1 ) ? 0 : 1;
							}
							if ( EHThrowsFault == -1 ) {
								if ( roles2rvrsPorts.containsKey( "MA" ) ) {
									for ( RvrsPort rvrsPort : roles2rvrsPorts.get( "MA" ) ) {
										EHThrowsFault = ( rvrsPort.throwsFault( ) ) ? 1 : -1;
									}
								}
								EHThrowsFault = ( MAThrowsFault == -1 ) ? 0 : 1;
							}
							// it is determined if MAThrowsFault and
							// EHThrowsFault
							if ( MAThrowsFault != 1 && EHThrowsFault != 1 ) {
								continue;
							}
							if ( MAThrowsFault == 1 ) {
								compPort.getFaultRoles( ).add( "MA" );
							}
							if ( EHThrowsFault == 1 ) {
								compPort.getFaultRoles( ).add( "EH" );
							}
							ArrayList < String > toTemplateB = compPort.toArray4Template( "faultComp" );
							AttrFiller.addToTemplate( toTemplateB , template , "faultComp" );

						}
						// if the comp port is in MA or EH, then export
						// the compPort
						else {
							s += applyCPEB( 1 , 0 , true );
							MapUtils.addToMapEntry( ret , MapKeys.COMPEN_PORTS , compPort );

							ArrayList < String > expComp = compPort.toArray4Template( "expComp" );
							MapUtils.addToMap( templatePortsMap , "expComp" , expComp , -1 );
						}
					} else { // if the compensation targets at a
								// specific scope
						String targetScope = compPort.getTargetScope( );

						if ( scopeCompRole.equals( "CH" ) || scopeCompRole.equals( "FH" ) || scopeCompRole.equals( "TH" ) ) {
							s += applyCP2EB( 1 , 0 );
							// search matching rvrsPort
							String targetRole = scopes2rvrsPorts.get( targetScope ).getCompIndex( );
							// if one not found leave
							if ( targetRole == null ) {
								continue;
							}
							compPort.getTargetRoles( ).add( targetRole );

							s += applyConnTemplate( "CMPSC" );
							s += applyENDCMP( 1 );
							s += applyTRMCMP( 1 );
							s += applyTRMRVSD( 1 );

							ArrayList < String > toTemplate = compPort.toArray4Template( "compensatePort" );
							/*
							 * AttrFiller.addToTemplate(toTemplate, template,
							 * "compensateScopePorts");
							 * AttrFiller.addToTemplate(toTemplate, template,
							 * "compensateScopePorts");
							 */
							AttrFiller.addToTemplate( toTemplate , template , "compensateScopePorts" );

							if ( scopes2rvrsPorts.get( targetScope ).throwsFault( ) ) {
								compPort.getFaultRoles( ).add( targetRole );
							} else {
								continue;
							}

							ArrayList < String > toTemplateB = compPort.toArray4Template( "faultComp" );
							AttrFiller.addToTemplate( toTemplateB , template , "faultComp" );

						} else {
							s += applyCPEB( 1 , 0 , true );
							MapUtils.addToMapEntry( ret , MapKeys.COMPEN_PORTS , compPort );

							ArrayList < String > expComp = compPort.toArray4Template( "expComp" );
							MapUtils.addToMap( templatePortsMap , "expComp" , expComp , -1 );

						}
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.EXIT_PORTS ) ) != null ) {
			// the exit ports are exported by EXIT components up to the PROC
			boolean atLeastOne = false;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < String > componentsPorts = ( ArrayList < String > ) components.get( i );
				if ( componentsPorts != null ) {
					if ( process ) {
						ArrayList < String > exitPort = new ArrayList < String >( 1 );
						exitPort.add( MapUtils.getScopeRole( childrenTag , i ) );
						AttrFiller.addToTemplate( exitPort , template , "exitPorts" );
					} else {
						MapUtils.addToMap( templatePortsMap , "expExitPorts" , MapUtils.getScopeRole( childrenTag , i ) , -1 );
					}
					atLeastOne = true;

				}
			}
			if ( process && atLeastOne ) {
				List exitPort = Arrays.asList( new String [ ] { "1" } );
				MapUtils.addToMapEntry( ret , MapKeys.EXIT_PORTS , exitPort );
			}

		}

		/**** StringTemplateART ****/
		if ( ( components = ( ArrayList ) vars.get( MapKeys.FAULTS_LIST ) ) != null ) {

			// two lists: for components that throw handled and unhandled faults
			ArrayList < String > componentsWithHFaults = new ArrayList < String >( components.size( ) );
			ArrayList < String > componentsWithFaults = new ArrayList < String >( components.size( ) );
			// DHP might throw a missingReply fault

			// // for each component with a faults list
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsFaults;
				if ( ( componentsFaults = ( ArrayList < Object > ) components.get( i ) ) != null && componentsFaults.size( ) > 0 ) {
					String role = MapUtils.getScopeRole( childrenTag , i );
					if ( role.equals( "MA" ) || role.equals( "EH" ) ) {
						componentsWithHFaults.add( role );
						template.setAttribute( "faultsList" , role );
					} else if ( role.equals( "CH" ) ) {
						// add the port to enable transferring the fault that CH
						// might throw
						template.setAttribute( "scopeFaultComp" , "1" );
						( ( RvrsPort ) ret.get( MapKeys.RVRS_PORTS ).get( 0 ) ).setThrowsFault( true );
					} else if ( role.equals( "TH" ) ) {
						componentsWithFaults.add( role );
						template.setAttribute( "faultsTHList" , role );
					} else if ( role.equals( "FH" ) ) {
						componentsWithFaults.add( role );
						template.setAttribute( "expFaultsList" , role );
					}
				}
			}
			// add the faultsList to the returned map
			if ( componentsWithFaults.size( ) > 0 ) {
				MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "default" );
			}
		}

		// handle inbound message ports in scope (e.g. receive) fill hashSets
		// csSet, meSet with the imas different correlSets and MsgExchanges

		TreeSet < String > conflSet = new TreeSet( );
		TreeSet < String > csSet = new TreeSet( );
		TreeSet < String > meSet = new TreeSet( );
		TreeSet < String > plSet = new TreeSet( );

		HashMap < String , ArrayList > pl2info = ( HashMap < String , ArrayList > ) MapUtils.getSingleEntry( vars , MapKeys.PARTNER_LINKS );
		HashSet < String > partnerLinks = null;
		if ( pl2info != null ) {
			partnerLinks = new HashSet < String >( pl2info.keySet( ) );
		}

		// ///////////////////////////////////////////////////

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.IMA_PORTS ) ) != null && components.size( ) > 0 ) {

			ArrayList checkImaPorts4Template = new ArrayList( );
			ArrayList disableImaPorts4Template = new ArrayList( );

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsImas = ( ArrayList < Object > ) components.get( i );
				if ( componentsImas == null ) {
					continue;
				}
				for ( int j = 0 ; j < componentsImas.size( ) ; j++ ) {
					IOMsgPort imaPort = ( IOMsgPort ) componentsImas.get( j );

					int meHNum = 0;
					int meENum = 1;
					if ( imaPort.getOpenIMAScope( ).equals( "" ) ) {
						// not here nor exported, pl has been checked in lower
						// scope
						meENum = 0;
					} else if ( imaPort.getOpenIMAScope( ).equals( templateId ) ) {
						// pl is checked in this scope
						meHNum = 1;
						meENum = 0;
						imaPort.setOpenIMAScope( "" );
					}
					// //////////////////////////

					int plHNum = 0;
					int plENum = 1;
					if ( imaPort.getPartnerLinkScope( ).equals( "" ) ) {
						// not here nor exported, pl has been checked in lower
						// scope
						plENum = 0;
					} else if ( imaPort.getPartnerLinkScope( ).equals( templateId ) ) {
						// pl is checked in this scope
						plHNum = 1;
						plENum = 0;
						imaPort.setPartnerLinkScope( "" );
					}
					// /////////////////////////////

					boolean withCS = imaPort.isWithCS( );

					// count the CSs that are checked here
					int csHNum = 0;
					int csENum = 0;
					ArrayList < String [ ] > csHereList = new ArrayList < String [ ] >( 0 );
					if ( imaPort.getCSsNumPerScopeID( ).containsKey( templateId ) ) {
						csHNum = imaPort.getCSsNumPerScopeID( ).get( templateId );
						// count the CSs that are checked in this scope
						csENum = imaPort.getOrderedCSs( ).size( ) - csHNum;
						csHereList.addAll( imaPort.getOrderedCSs( ).subList( 0 , csHNum ) );
						int c = 0;
					}

					for ( String [ ] csH : csHereList ) {
						csSet.add( csH[ 0 ] );
					}
					// ////////////////////////////

					// count the CSLabels that are checked here
					int cfHNum = 0;
					int cfENum = 0;
					ArrayList < String > cfHereList = new ArrayList < String >( 0 );
					if ( imaPort.getCSLabelsNumPerScopeID( ).containsKey( templateId ) ) {
						cfHNum = imaPort.getCSLabelsNumPerScopeID( ).get( templateId );
						// count the CSs that are checked in this scope
						cfENum = imaPort.getOrderedCSLabels( ).size( ) - cfHNum;
						cfHereList.addAll( imaPort.getOrderedCSLabels( ).subList( 0 , cfHNum ) );
						int c = 0;
					}

					for ( String CSLabel : cfHereList ) {
						conflSet.add( imaPort.getCorrelationSetLabel( CSLabel ) );
					}

					if ( plHNum > 0 ) {
						plSet.add( imaPort.getPartnerLink( ) );
					}
					if ( meHNum > 0 ) {
						meSet.add( imaPort.getMessageExchangeLabel( ) );
					}

					// ////////////////////////////
					// check if there are exports
					boolean withExport = ( csENum > 0 || cfENum > 0 || plENum > 0 || meENum > 0 );

					// ////////////////////////////
					imaPort.setCompIndex( MapUtils.getComponentIndex( vars , template , i ) );
					int boolNumInDHS = plHNum + meHNum + csHNum + cfHNum;
					if ( boolNumInDHS == 0 ) {
						// no info of this ima in the scope: add to expIma
						// expIma and expIoma look same in template
						ArrayList < String > toTemplate = imaPort.toArray4Template( "expIma" );
						if ( imaPort.isRcvIO( ) ) {
							MapUtils.addToMap( templatePortsMap , "expIoma" , toTemplate , -1 );

						}
						MapUtils.addToMap( templatePortsMap , "expIma" , toTemplate , -1 );
						MapUtils.addToMap( templatePortsMap , "expDisIma" , toTemplate , -1 );
						MapUtils.addToMapEntry( ret , MapKeys.IMA_PORTS , imaPort );
					} else {
						String connName = applyCHKRC5Name( csHNum , csENum , cfHNum , meHNum , plHNum , withExport , withCS );
						MapUtils.addToBIPCode( ret ,
								applyCHKRC5( csHNum , csENum , cfHNum , cfENum , meHNum , plHNum , withExport , withCS , connName , boolNumInDHS ) );

						ArrayList msgList = new ArrayList( boolNumInDHS );
						for ( String [ ] CS : csHereList ) {
							msgList.add( "cs_" + CS[ 0 ] );
						}
						ArrayList cfHList = new ArrayList( cfHNum + 1 );
						for ( String CF : cfHereList ) {
							msgList.add( "cf_" + imaPort.getCorrelationSetLabel( CF ) );
							cfHList.add( imaPort.getCorrelationSetLabel( CF ) );
						}
						if ( plHNum > 0 ) {
							msgList.add( "cf_" + imaPort.getCorrelationSetLabel( "" ) );
							cfHList.add( imaPort.getCorrelationSetLabel( "" ) );
							conflSet.add( imaPort.getCorrelationSetLabel( "" ) );
						}
						if ( meHNum > 0 ) {
							msgList.add( "me_" + imaPort.getMessageExchangeLabel( ) );
						}
						s += applyPortTypeDecl( csHNum + csENum , 3 );

						HashMap map_check = new HashMap( );
						map_check.put( "compName" , imaPort.getCompName( ) );
						map_check.put( "compIndex" , imaPort.getCompIndex( ) );
						map_check.put( "connName" , connName );
						map_check.put( "varsStringInDHS" , msgList );
						map_check.put( "boolNumInDHS" , boolNumInDHS );
						if ( meENum + plENum + csENum > 0 ) {
							map_check.put( "withExport" , "D" );
						}
						if ( meHNum > 0 ) {
							map_check.put( "meH" , imaPort.getMessageExchangeLabel( ) );
						}
						checkImaPorts4Template.add( map_check );

						// -------------------------------------------
						// if conflicting receive flag and partner link is in
						// this scope,
						// prepare a disIma port...
						if ( cfHNum + plHNum > 0 ) {
							HashMap map_disable = new HashMap( );
							map_disable.put( "compName" , imaPort.getCompName( ) );
							map_disable.put( "compIndex" , imaPort.getCompIndex( ) );
							map_disable.put( "cfHList" , cfHList );
							if ( cfENum + plENum > 0 ) {
								// disIma goes higher
								MapUtils.addToBIPCode( ret , applyRDV( 2 , true ) );
								map_disable.put( "withExport" , "D" );
							}
							disableImaPorts4Template.add( map_disable );
						} else {
							ArrayList < String > toTemplate = imaPort.toArray4Template( "" );
							AttrFiller.addToTemplate( toTemplate , template , "expDisIma" );
						}
						// -------------------------------------------
						ArrayList < String > toTemplate = imaPort.toArray4Template( "ioma" );
						if ( imaPort.isRcvIO( ) ) {
							// also add Close ...
							if ( plHNum == 1 ) {
								AttrFiller.addToTemplate( toTemplate , template , "ioma" );

							} else {
								MapUtils.addToMap( templatePortsMap , "expIoma" , toTemplate , -1 );

							}
						}

						imaPort.getOrderedCSs( ).removeAll( csHereList );
						imaPort.getOrderedCSLabels( ).removeAll( cfHereList );

						if ( csENum + cfENum + plENum + meENum > 0 ) {
							MapUtils.addToMapEntry( ret , MapKeys.IMA_PORTS , imaPort );
						}
					}// END add port to template with the right entry

				}// END for each component's port
			}// END for each component

			template.setAttribute( MapKeys.IMA_PORTS , checkImaPorts4Template );
			template.setAttribute( "disIma" , disableImaPorts4Template );
			// template.setAttribute("ima", disableImaPorts4Template);
			/*
			 * handle conflicting ima
			 */
			HashMap < String , ArrayList < ArrayList >> retConflImaMap = new HashMap < String , ArrayList < ArrayList >>( );
			MapUtils.addToMapEntry( ret , MapKeys.CONFL_IMA_PORTS , retConflImaMap );
			HashMap < String , ArrayList > conflImaMap = uniteConflImaPorts( vars );

			// for each receive with the same partnerLink and operation (pl_op)
			// , get these conflicts
			if ( conflImaMap != null ) {
				for ( String pl_op : conflImaMap.keySet( ) ) {
					ArrayList < ArrayList > imasConflicts = conflImaMap.get( pl_op );
					/*
					 * after having a joined CHKRC2XDocurr port, add this entry
					 * to the returned map
					 */
					// : (pl_op, keyPort, compInd)
					// expConflIma: (conflsNum, pl_op, compInd+ ".checkAR_" +
					// keyPort)

					ArrayList < String > complIndices = new ArrayList < String >( imasConflicts.size( ) );
					/* addAll to template */
					for ( ArrayList ima : imasConflicts ) {
						String compInd = ( String ) ima.get( 2 );
						String keyPort = ( String ) ima.get( 1 );
						ima.remove( 2 ); // remove compInd
						complIndices.add( compInd + ".checkAR_" + keyPort );
					}

					ConflIMAPort conflImaPort = new ConflIMAPort( null , null , pl_op , String.valueOf( imasConflicts.size( ) ) , complIndices );
					ArrayList < String > toTemplate = conflImaPort.toArray4Template( "expConflIma" );

					if ( !partnerLinks.contains( pl_op.split( "_" )[ 0 ] ) ) {
						s += applyCHKRC2XD( Integer.valueOf( toTemplate.get( 0 ) ) );
						MapUtils.addToMap( templatePortsMap , "expConflIma" , toTemplate , -1 );

						/* addAll to returned map */
						retConflImaMap.put( pl_op , imasConflicts );

					} else if ( imasConflicts.size( ) > 1 ) {
						s += applyCHKRC2X( Integer.valueOf( toTemplate.get( 0 ) ) );
						AttrFiller.addToTemplate( toTemplate , template , "conflList" );
						/* addAll to returned map */
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.OMA_PORTS ) ) != null ) {

			ArrayList checkOmaPorts4Template = new ArrayList( );

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsPorts = ( ArrayList < Object > ) components.get( i );
				if ( componentsPorts == null ) {
					continue;
				}
				for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
					IOMsgPort omaPort = ( IOMsgPort ) componentsPorts.get( j );

					int meHNum = 0;
					int meENum = 1;
					if ( omaPort.getOpenIMAScope( ).equals( "" ) ) {
						// not here nor exported, pl has been checked in
						// lower
						// scope
						meENum = 0;
					} else if ( omaPort.getOpenIMAScope( ).equals( templateId ) ) {
						// pl is checked in this scope
						meHNum = 1;
						meENum = 0;
						omaPort.setOpenIMAScope( "" );
					}

					// /////////////////////////////

					// count the CSs that are checked here
					boolean withCS = omaPort.isWithCS( );
					int csHNum = 0;
					int csENum = 0;

					ArrayList < String [ ] > csHereList = new ArrayList < String [ ] >( 0 );
					if ( withCS ) {
						if ( omaPort.getCSsNumPerScopeID( ).containsKey( templateId ) ) {
							csHNum = omaPort.getCSsNumPerScopeID( ).get( templateId );
							// count the CSs that are checked in this scope
							csENum = omaPort.getOrderedCSs( ).size( ) - csHNum;
							csHereList.addAll( omaPort.getOrderedCSs( ).subList( 0 , csHNum ) );
						}

						for ( String [ ] csH : csHereList ) {
							csSet.add( csH[ 0 ] );
						}
					}

					// ...cf list .. //
					if ( meHNum > 0 ) {
						meSet.add( omaPort.getMessageExchangeLabel( ) );
					}
					// check if there are exports

					omaPort.setCompIndex( MapUtils.getComponentIndex( vars , template , i ) );

					if ( csHNum + meHNum == 0 ) {
						ArrayList < String > toTemplate = omaPort.toArray4Template( "expOma" );
						MapUtils.addToMap( templatePortsMap , "expOma" , toTemplate , -1 );

					} else {
						boolean withExport = ( csENum > 0 || meENum > 0 );

						int boolNumInDHS = meHNum + csHNum;

						String connName = applyCHKRPLName( csHNum , csENum , meHNum , withExport , withCS );
						MapUtils.addToBIPCode( ret , applyCHKRPL( csHNum , csENum , meHNum , withExport , withCS , connName , boolNumInDHS ) );

						ArrayList msgList = new ArrayList( boolNumInDHS );
						for ( String [ ] CS : csHereList ) {
							msgList.add( "cs_" + CS[ 0 ] );
						}
						if ( meHNum > 0 ) {
							msgList.add( "me_" + omaPort.getMessageExchangeLabel( ) );
						}
						s += applyPortTypeDecl( csHNum + csENum , 3 );

						HashMap map_check = new HashMap( );
						map_check.put( "compName" , omaPort.getCompName( ) );
						map_check.put( "compIndex" , omaPort.getCompIndex( ) );
						map_check.put( "connName" , connName );
						map_check.put( "varsStringInDHS" , msgList );
						map_check.put( "boolNumInDHS" , boolNumInDHS );
						if ( meENum + csENum > 0 ) {
							map_check.put( "withExport" , 1 );
						}
						if ( meHNum > 0 ) {
							map_check.put( "meH" , omaPort.getMessageExchangeLabel( ) );
						}
						checkOmaPorts4Template.add( map_check );

					}

					omaPort.getOrderedCSs( ).removeAll( csHereList );
					if ( csENum + meENum > 0 ) {
						MapUtils.addToMapEntry( ret , MapKeys.OMA_PORTS , omaPort );
					}
				}
			}
			template.setAttribute( MapKeys.OMA_PORTS , checkOmaPorts4Template );
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.INV_PORTS ) ) != null && components.size( ) > 0 && partnerLinks != null
				&& !partnerLinks.isEmpty( ) ) {

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsPorts = ( ArrayList < Object > ) components.get( i );
				if ( componentsPorts == null ) {
					continue;
				}
				for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
					IOMsgPort inv = ( IOMsgPort ) componentsPorts.get( j );

					/*
					 * if the partnerLink in the oma is declared in a higher
					 * scope, then oma is further exported. It has the format :
					 * 0:numCS, 1:keyPort, 2:meFlag, 3:msgActInfo
					 */
					inv.setCompIndex( MapUtils.getScopeRole( childrenTag , i ) );

					if ( !partnerLinks.contains( inv.getPartnerLink( ) ) ) {

						if ( inv.getOrderedCSs( ) != null && inv.getOrderedCSs( ).size( ) > 0 ) {
							s += applyCHKCVD( inv.getOrderedCSs( ).size( ) );
							s += applyCHKMR( true );
						}
						ArrayList < String > toTemplate = inv.toArray4Template( "expInv" );
						MapUtils.addToMap( templatePortsMap , "expInv" , toTemplate , -1 );
						ret.get( "expInv" ).add( inv );

					} else {
						s += applyCHKMR( false );
						if ( inv.getOrderedCSs( ) != null && inv.getOrderedCSs( ).size( ) > 0 ) {
							s += applyCHKCVD( inv.getOrderedCSs( ).size( ) );
						}
						plSet.add( inv.getPartnerLink( ) );
						ArrayList < String > toTemplate = inv.toArray4Template( MapKeys.INV_PORTS );
						AttrFiller.addToTemplate( toTemplate , template , MapKeys.INV_PORTS );
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.RCV_MESSAGE_PORTS ) ) != null && components.size( ) > 0 ) {

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsExps = ( ArrayList < Object > ) components.get( i );
				if ( componentsExps == null ) {
					continue;
				}
				for ( int j = 0 ; j < componentsExps.size( ) ; j++ ) {
					IOMsgPort ioPort = ( IOMsgPort ) componentsExps.get( j );

					ioPort.setCompIndex( MapUtils.getScopeRole( childrenTag , i ) );
					MapUtils.addToMapEntry( ret , MapKeys.RCV_MESSAGE_PORTS , ioPort );

					ArrayList < String > toTemplate = null;
					if ( ioPort.getOpenIMAScope( ).equals( templateId ) ) {
						// arrays for template with the tag rcvMsgPorts include
						// the meTag
						toTemplate = ioPort.toArray4Template( MapKeys.RCV_MESSAGE_PORTS );
						AttrFiller.addToTemplate( toTemplate , template , "rcvMsgPorts" );
						MapUtils.addToBIPCode( ret , applyConnTemplate( "RDSC1" ) );
					} else {
						toTemplate = ioPort.toArray4Template( "" );
						MapUtils.addToMap( templatePortsMap , "expRcvMsgPorts" , toTemplate , -1 );

						MapUtils.addToBIPCode( ret , applyCPEB( 1 , 0 , true ) );
					}
					if ( process ) {
						MapUtils.addToBIPCode( ret , applyCPEB( 1 , 0 , false ) );
					}

				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( "sndMsgPorts" ) ) != null && components.size( ) > 0 ) {

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < IOMsgPort > componentsExps = ( ArrayList < IOMsgPort > ) components.get( i );
				if ( componentsExps == null ) {
					continue;
				}
				for ( int j = 0 ; j < componentsExps.size( ) ; j++ ) {
					IOMsgPort exp = componentsExps.get( j );
					// set the component's index
					exp.setCompIndex( MapUtils.getScopeRole( childrenTag , i ) );
					MapUtils.addToMapEntry( ret , MapKeys.SND_MESSAGE_PORTS , exp ); // add
																						// port
					// to
					// returned
					// map

					ArrayList < String > toTemplate;
					if ( exp.getOpenIMAScope( ).equals( templateId ) ) {
						toTemplate = exp.toArray4Template( "sndMsgPorts" );
						MapUtils.addToBIPCode( ret , applyConnTemplate( "ASSC1" ) );
						AttrFiller.addToTemplate( toTemplate , template , "sndMsgPorts" );
					} else {
						toTemplate = exp.toArray4Template( "expSndMsgPorts" );
						MapUtils.addToMap( templatePortsMap , "expSndMsgPorts" , toTemplate , -1 );

						MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );
					}
					if ( process ) {
						MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , false ) );
					}

				}
			}
		}

		for ( String c : conflSet ) {
			// if ( !dataList.contains( c ) ) {
			// dataList.add( c );
			template.setAttribute( "corrSetLabels" , c );
			// }
		}
		for ( String c : plSet ) {
			if ( !dataList.contains( c ) ) {
				// dataList.add( c );
				template.setAttribute( "dataList" , c );
			}
		}
		for ( String c : csSet ) {
			// if ( !dataList.contains( c ) ) {
			// dataList.add( c );
			template.setAttribute( "csList" , c );
			// }
		}
		for ( String c : meSet ) {
			// if ( !dataList.contains( c ) ) {
			// dataList.add( "me_"+c );
			template.setAttribute( "meList" , c );
			// }
		}

		// add this scope to returned onIMAs
		MapUtils.addToMapEntry( ret , MapKeys.ON_IMA_PORTS , "1" );

		// add onIMAs to scope
		ArrayList < String > onIMAPort = new ArrayList < String >( 2 );
		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.ON_IMA_PORTS ) ) != null && components.size( ) > 0 ) {
			onIMAPort = new ArrayList( components.size( ) + 2 );
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( components.get( i ) == null ) {
					continue;
				}
				String compIndex = MapUtils.getComponentIndex( vars , template , i );
				onIMAPort.add( "SC" + templateId + "C" + compIndex );
			}
		}
		onIMAPort.add( "DHS" );
		int count = onIMAPort.size( );
		MapUtils.addToBIPCode( ret , applyANY( count , true ) );
		MapUtils.addToBIPCode( ret , applyRDV( count , true ) );
		onIMAPort.add( 0 , String.valueOf( count ) );
		template.setAttribute( "onIMAs" , onIMAPort );

		filler.addMapToTemplate( template , templatePortsMap , "ports" );
		MapUtils.addToBIPCode( ret , s + template.toString( ) );

		return ret;
	}


	/*
	 * applies the template for FHwrapper component, the enclosing CATCH
	 * component as well as the FAULTOBSERVER component.
	 */
	public HashMap < String , ArrayList > applyFHW( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		HashMap < String , ArrayList > vars = stack.peek( );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "FHW" );

		String s = "\n";
		s += applyPortTypeDecl( 2 , 0 );
		s += applyRDV( 2 , true ); /* 2 cause we add the catch component */
		s += applyRDV( 2 , false );
		s += applyCPEB( 1 , 0 , true );

		String faultMessageType = ( elem.hasAttribute( "faultMessageType" ) ) ? elem.getAttribute( "faultMessageType" ) : elem
				.getAttribute( "faultElement" );

		String catchCompleteName = getFaultCompleteName( elem , stack );
		// if this is a custom fault
		int faultCode = ( catchCompleteName.equals( "all" ) ) ? 0 : faults.getFaultName( catchCompleteName );
		template.setAttribute( "handledFault" , faultCode );

		String compName = catchCompleteName + "_" + ( templateList.getSize( ) + 1 );
		template.setAttribute( "id" , compName );
		MapUtils.addToMapEntry( ret , MapKeys.CHILD_COMP , "fhw_" + compName );
		//
		// ret.put(MapKeys.CATCH_COMPLETE_NAMES, new ArrayList());
		// ret.get(MapKeys.CATCH_COMPLETE_NAMES).add(catchCompleteName);

		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );

		if ( vars.containsKey( MapKeys.RETHROW_PORTS ) && vars.get( MapKeys.RETHROW_PORTS ).size( ) > 0 ) {
			template.setAttribute( "rethrow" , true );
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "default" );
		}
		MapUtils.addToBIPCode( ret , applyConnTemplate( "ASSC1" ) );

		/* (String childComp, String[] expFaults, String[][] expPorts) */
		ArrayList childComp = ( ArrayList ) MapUtils.getSingleEntry( vars , MapKeys.CHILD_COMP );
		if ( childComp != null && childComp.size( ) > 0 ) {
			template.setAttribute( "childComp" , childComp.get( 0 ) );
		}

		s += "\n\n" + template.toString( );
		MapUtils.addToBIPCode( ret , s );
		return ret;
	}


	public HashMap < String , ArrayList > applyFH( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "FH" , -1 );

		HashMap < String , ArrayList > vars = stack.peek( );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "FH" );
		String s = "\n";

		String compName = "fh_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		template.setAttribute( "id" , templateList.getSize( ) );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		/*
		 * if the String "all" is found in a faultHandler's name, then there is
		 * a catchAll faultHandler
		 */
		int all = -1;
		ArrayList < Object > components = vars.get( MapKeys.CHILD_COMP );

		if ( components != null ) {
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				String childComp = ( String ) ( ( ArrayList ) components.get( i ) ).get( 0 );
				if ( childComp.contains( "all" ) ) {
					all = i;
				}
			}

			if ( all == -1 ) {
				HashMap < String , ArrayList > retFH = applyDEFAULT_FHW( elem , stack );
				addAsChild( vars , retFH );
			}

		}
		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );

		components = vars.get( MapKeys.CHILD_COMP );
		int FHNum = components.size( );

		template.setAttribute( "countFH" , FHNum );
		for ( int i = 0 ; i < components.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , ( ( ArrayList ) components.get( i ) ).get( 0 ) );

		}
		s += "\n" + applySFH( FHNum );
		s += "\n" + applyConnTemplate( "RDSC1" );
		s += "\n" + applyRDV( FHNum , true );
		s += "\n" + applyANY( FHNum , true );

		s += "\n\n" + template.toString( );
		MapUtils.addToBIPCode( ret , s );
		return ret;
	}


	private void addToTemplatePortsBCastArranged( ArrayList < Object > components , int compNum , StringTemplate template , String templateAttribute ,
			boolean allChild ) {

		int i;
		for ( i = 0 ; i < components.size( ) ; i++ ) {
			// make a list of components to disable
			String [ ] subset = new String [ compNum ];
			int c = 0;
			subset[ 0 ] = String.valueOf( i + 1 );
			for ( int j = 1 ; j < compNum ; j++ ) {
				if ( c == i ) {
					c++;
				}
				subset[ j ] = String.valueOf( c + 1 );
				c++;
			}
			AttrFiller.addToTemplate( subset , template , templateAttribute );
		}
		if ( !allChild ) {
			// add one extra component, which does not appear in the components
			// list
			String [ ] subset = new String [ compNum ];
			int c = 0;
			subset[ 0 ] = String.valueOf( i + 1 );
			for ( int j = 1 ; j < compNum ; j++ ) {
				if ( c == i ) {
					c++;
				}
				subset[ j ] = String.valueOf( c + 1 );
				c++;
			}
			AttrFiller.addToTemplate( subset , template , templateAttribute );
		}
	}


	/*
	 * handlerName : "TH" or "CH"
	 */
	public HashMap < String , ArrayList > applyCTH( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack , String handlerName )
			throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , handlerName , -1 );

		HashMap < String , ArrayList > vars = stack.peek( );
		StringTemplate template = AttrFiller.getTemplate( templateFile , handlerName );
		String s = "\n";

		String compName = handlerName.toLowerCase( ) + "_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		template.setAttribute( "id" , templateList.getSize( ) );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		ArrayList < Object > components = new ArrayList < Object >( );
		if ( ( components = vars.get( MapKeys.CHILD_COMP ) ) != null ) {
			template.setAttribute( MapKeys.CHILD_COMP , components.get( 0 ) );
		}
		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );
		s += "\n" + applyCP2EB( 1 , 0 );

		s += "\n\n" + template.toString( );
		MapUtils.addToBIPCode( ret , s );
		return ret;
	}


	// lsi :(tmpl)
	// expPorts:(connType, portName, ind, )
	public HashMap < String , ArrayList > applyLS( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		String s = "\n";
		StringTemplate template = AttrFiller.getTemplate( templateFile , "LS" );

		template.setAttribute( "id" , ( templateList.getSize( ) + 1 ) );
		String compName = "ls_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		ret = sequencing( template , stack , ret );

		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		return ret;
	}


	private HashMap < String , ArrayList > applyDEFAULT_FH( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		// create a compensate

		Document xmlDoc = new DocumentImpl( );

		Node faultHandlers = xmlDoc.createElement( "faultHandlers" );
		Node sequence = xmlDoc.createElement( "sequence" );
		// if there are scopes underneath, start compensate
		HashMap < String , ArrayList > vars = stack.peek( );
		if ( vars.containsKey( MapKeys.RVRS_PORTS ) && vars.get( MapKeys.RVRS_PORTS ).size( ) > 0 ) {
			Node compensate = xmlDoc.createElement( "compensate" );
			sequence.appendChild( compensate );
		}
		Node catchAll = xmlDoc.createElement( "catchAll" );
		Node rethrow = xmlDoc.createElement( "rethrow" );

		sequence.appendChild( rethrow );
		catchAll.appendChild( sequence );
		faultHandlers.appendChild( catchAll );
		xmlDoc.appendChild( faultHandlers );

		ret = this.bcompiler.transform( ( Element ) faultHandlers , stack );

		return ret;
	}


	private HashMap < String , ArrayList > applyDEFAULT_FHW( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		// create a compensate

		Document xmlDoc = new DocumentImpl( );

		Node sequence = xmlDoc.createElement( "sequence" );
		// if there are scopes underneath, start compensate
		if ( stack.peek( ).containsKey( MapKeys.RVRS_PORTS ) && stack.peek( ).get( MapKeys.RVRS_PORTS ).size( ) > 0 ) {
			Node compensate = xmlDoc.createElement( "compensate" );
			sequence.appendChild( compensate );
		}
		Node catchAll = xmlDoc.createElement( "catchAll" );
		Node rethrow = xmlDoc.createElement( "rethrow" );

		sequence.appendChild( rethrow );
		catchAll.appendChild( sequence );
		xmlDoc.appendChild( catchAll );

		ret = this.bcompiler.transform( ( Element ) catchAll , stack );

		return ret;
	}


	private HashMap < String , ArrayList > applyDEFAULT_TCH( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , String handlerName )
			throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		Document xmlDoc = new DocumentImpl( );

		Node handler = xmlDoc.createElement( handlerName );
		// if there are no scopes underneath, do not compensate

		Node compensate = xmlDoc.createElement( "compensate" );
		handler.appendChild( compensate );
		xmlDoc.appendChild( handler );

		ret = this.bcompiler.transform( ( Element ) handler , stack );

		return ret;
	}


	private HashMap < String , ArrayList > applyDEFAULT_CH( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		return applyDEFAULT_TCH( el , stack , "compensationHandler" );
	}


	private HashMap < String , ArrayList > applyDEFAULT_TH( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		return applyDEFAULT_TCH( el , stack , "terminationHandler" );
	}


	public HashMap < String , ArrayList > applyLSW( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		// StringTemplateART calculate template's signature
		StringTemplate template = AttrFiller.getTemplate( templateFile , "LSW" );
		String templateNum = String.valueOf( templateList.getSize( ) + 1 );
		String compName = "lsw_" + templateNum;
		template.setAttribute( "id" , templateNum );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 ); /* particular */

		String linkName = sanitize( el.getAttribute( "linkName" ) );

		RwPort wrtLnkPort = new RwPort( compName , "" );
		wrtLnkPort.parts( ).add( linkName );
		MapUtils.addToMapEntry( ret , MapKeys.WRITE_LINK_PORTS , wrtLnkPort );
		MapUtils.addToMapEntry( ret , MapKeys.READ_LINK_PORTS , wrtLnkPort.clone( ) );

		ExpressionParsingUtils.processExpression( el , "transitionCondition" , stack , ret , template , "expression" , "true" );

		MapUtils.addToBIPCode( ret , applyCP2EB( 0 , 1 ) );
		MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );
		MapUtils.addToBIPCode( ret , applyConnTemplate( "RDD1" ) );
		MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );

		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;

	}


	/* String[] childComp, String[][] faultsList, String[][] expPorts */
	public HashMap < String , ArrayList > applySEQ( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		String s = "\n";
		StringTemplate template = AttrFiller.getTemplate( templateFile , "SEQ" );

		template.setAttribute( "id" , ( templateList.getSize( ) + 1 ) );
		String compName = "seq_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		ret = sequencing( template , stack , ret );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );

		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		return ret;
	}


	/* elem is the targets element */
	public HashMap < String , ArrayList > applyLT( Element targetsEl , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "LT" );
		String templateNum = String.valueOf( templateList.getSize( ) + 1 );
		String compName = "lt_" + templateNum;
		template.setAttribute( "id" , templateNum );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		ArrayList < Element > targetElems = XMLFile.getChildrenByTagLocalName( targetsEl , "target" );
		ArrayList < String > linkNames = new ArrayList < String >( targetElems.size( ) );
		for ( Element targetEl : targetElems ) {
			linkNames.add( sanitize( targetEl.getAttribute( "linkName" ) ) );
		}

		// //////////////////////////////////////////////////////////////

		// arrange linkNames: leftmost linkname, the higher the scope of the
		// variable
		HashMap < Integer , Integer > link2flow = new HashMap < Integer , Integer >( );
		int c = 0;
		for ( Element targetEl : targetElems ) {
			c++;
			/* we create an eval component for all target links */
			String link = sanitize( targetEl.getAttribute( "linkName" ) );
			// find in stack backwrds this linkName's flow (index)
			int flow_ind = 0;
			int j = stack.size( );
			Iterator it = stack.iterator( );
			while ( it.hasNext( ) ) {
				j--;
				HashMap < String , ArrayList > curr_map = ( HashMap < String , ArrayList > ) it.next( );

				HashSet < String > links = ( HashSet < String > ) MapUtils.getSingleEntry( curr_map , MapKeys.LINKS );
				if ( links != null && links.contains( link ) ) {
					flow_ind = j;
					break;
				}
			}

			link2flow.put( c , flow_ind );
		}

		HashMap flow2links = new HashMap( );
		for ( Integer linkIndex : link2flow.keySet( ) ) {
			if ( !flow2links.containsKey( link2flow.get( linkIndex ) ) ) {
				flow2links.put( link2flow.get( linkIndex ) , new ArrayList( ) );
			}
			( ( ArrayList ) flow2links.get( link2flow.get( linkIndex ) ) ).add( linkIndex );
		}
		/* sort flows from lower to higher */
		ArrayList < Integer > flow_inds = new ArrayList( link2flow.values( ) );
		Collections.sort( flow_inds );
		/* add each link of i'th flow to sortedLinks */
		ArrayList sortedLinks = new ArrayList( link2flow.keySet( ).size( ) );
		for ( Integer i : flow_inds ) {
			for ( Integer link : ( ArrayList < Integer > ) flow2links.get( i ) ) {
				sortedLinks.add( linkNames.get( link ) );
			}
		}
		/* array for the AASD template */
		RwPort toExport = new RwPort( compName , "" );
		int [ ] aasl = new int [ sortedLinks.size( ) ];
		Arrays.fill( aasl , 1 );
		toExport.parts( ).addAll( sortedLinks );

		MapUtils.addToBIPCode( ret , applyAAS( aasl , true ) );
		MapUtils.addToMapEntry( ret , MapKeys.WRITE_LINK_PORTS , toExport );

		// //////////////////////////////////////////////////////////////
		RwPort expReadLinksPort = new RwPort( compName , "" );
		expReadLinksPort.parts( ).addAll( linkNames );
		MapUtils.addToMapEntry( ret , MapKeys.READ_LINK_PORTS , expReadLinksPort );

		template.setAttribute( "linkNames" , linkNames );
		template.setAttribute( "countLinks" , linkNames.size( ) );
		MapUtils.addToBIPCode( ret , applyCP2EB( 0 , 1 ) );
		MapUtils.addToBIPCode( ret , applyRDVAR( linkNames.size( ) , false ) );
		MapUtils.addToBIPCode( ret , applyRDVAR( linkNames.size( ) , true ) );

		String joinCond = "";
		ArrayList < Element > joinCondWrapper = XMLFile.getChildrenByTagLocalName( targetsEl , "joinCondition" );
		Element joinConditionEl = null;
		/* if there is a transition condition */
		if ( joinCondWrapper.size( ) > 0 ) {
			joinConditionEl = joinCondWrapper.get( 0 );
			if ( joinConditionEl.getTextContent( ) != null ) {
				joinCond = joinConditionEl.getTextContent( );
			}

			/* TODO:parse the expression and add faults */
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "invalidExpressionValue" , "subLanguageExecutionFault" , "uninitializedVariable" );

		} else { /* if missing joinCondition, add the default one */
			for ( int i = 0 ; i < linkNames.size( ) ; i++ ) {
				String linkName = linkNames.get( i );
				if ( i > 0 ) {
					joinCond += "&&";
				}
				joinCond += "(" + linkName + "==1)";

				/*
				 * String[] tmp = { "1", "eval_" + templateNum + "_" + linkName,
				 * String.valueOf(i + 1), String.valueOf(i + 2), linkName };
				 * AttrFiller.addToTemplate(tmp, template, "varTransLists");
				 */
			}
			joinCond += ";";

		}
		// if there suppressJoinFailure doesn't exist in any ancestor, then add
		// default 'no'
		template.setAttribute( "suppJoinFail" , MapUtils.getSuppressJoinFailure( stack ) );
		template.setAttribute( "joinCondition" , joinCond );

		HashMap templatePortsMap = new HashMap( );
		// exportPorts(stack.peek(), templatePortsMap, template, ret,
		// false);
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return ret;
	}


	public void addAsChild( HashMap < String , ArrayList > vars , HashMap < String , ArrayList > act ) {

		// determine lists' size in vars
		int toIndex = 0;
		if ( vars.containsKey( MapKeys.CHILD_COMP ) ) {
			toIndex = vars.get( MapKeys.CHILD_COMP ).size( );
		}

		for ( String key : act.keySet( ) ) {
			if ( act.get( key ) == null || ( ( act.get( key ) instanceof ArrayList && act.get( key ).size( ) == 0 ) ) || MapKeys.isHashMapEntry( key )
					&& ( ( HashMap ) act.get( key ).get( 0 ) ).keySet( ).size( ) == 0 ) {
				continue;
			}
			if ( !vars.containsKey( key ) ) {
				vars.put( key , new ArrayList < Object >( ) );
			}
			/* add to general, putting nulls for prev children */
			if ( toIndex > vars.get( key ).size( ) ) {
				for ( int j = vars.get( key ).size( ) ; j < toIndex ; j++ ) {
					vars.get( key ).add( j , null );
				}
			}

			if ( act.get( key ) instanceof ArrayList ) {
				vars.get( key ).add( toIndex , act.get( key ) );
			} else {
				vars.get( key ).add( toIndex , act.get( key ).get( 0 ) );
			}
		}

		return;
	}


	private void lowerChildsLevel( HashMap < String , ArrayList > toVars , HashMap < String , ArrayList > fromVars , Integer fromIndex ) {

		// determine toIndex
		int toIndex = 0;
		if ( toVars.containsKey( MapKeys.CHILD_COMP ) ) {
			toIndex = toVars.get( MapKeys.CHILD_COMP ).size( );
		}

		for ( String key : fromVars.keySet( ) ) {
			if ( fromVars.get( key ) == null || fromVars.get( key ).size( ) < fromIndex + 1 ) {
				continue;
			}
			if ( !toVars.containsKey( key ) ) {
				toVars.put( key , new ArrayList < Object >( toIndex ) );
			}
			while ( toVars.get( key ).size( ) < toIndex ) {
				if ( key.equals( MapKeys.CONFL_IMA_PORTS ) ) {
					toVars.get( key ).add( new HashMap < String , ArrayList >( 1 ) );
				} else {
					toVars.get( key ).add( new ArrayList < Object >( 1 ) );
				}
			}
			toVars.get( key ).add( fromVars.get( key ).get( fromIndex ) );
		}
	}


	private void changeChildIndex( int oldIndex , int newIndex , HashMap < String , ArrayList > vars ) {

		for ( String key : vars.keySet( ) ) {
			// first create empty ArrayLists for earlier places
			while ( vars.get( key ).size( ) - 1 < newIndex ) {
				if ( key.equals( MapKeys.CONFL_IMA_PORTS ) ) {
					vars.get( key ).add( new HashMap < String , ArrayList >( 1 ) );
				} else {
					vars.get( key ).add( new ArrayList < Object >( 1 ) );
				}
			}
			// now add to newIndex the 1st index
			ArrayList actList;
			if ( ( ( ArrayList ) vars.get( key ) ).size( ) > oldIndex ) {
				// if oldIndex is represented in the key entry, retrieve and
				// remove it
				actList = ( ArrayList ) vars.get( key ).remove( oldIndex );
			} else {
				// if oldIndex is not represented in the key entry, create an
				// empty
				actList = new ArrayList( 1 );
			}
			// add the entry to the newIndex
			vars.get( key ).add( newIndex , actList );
		}

	}


	public HashMap < String , ArrayList > applyACTW( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ,
			HashMap < String , ArrayList > act ) throws Exception {

		// after entering an activity, there might be source or target elements
		boolean hasSrc = ( XMLFile.getChildrenByTagLocalName( elem , "sources" ).size( ) > 0 );
		boolean hasTrg = ( XMLFile.getChildrenByTagLocalName( elem , "targets" ).size( ) > 0 );
		if ( !hasSrc && !hasTrg ) {
			return act;
		}

		int actOldInd = 0;
		int actNewInd = 0;

		if ( hasSrc && hasTrg ) {
			actOldInd = 2;
			actNewInd = 1;
		} else if ( hasTrg ) { // if !hasSrc && hasTrg
			actOldInd = 1;
			actNewInd = 1;
		} else { // if hasSrc && !hasTrg
			actOldInd = 1;
			actNewInd = 0;
		}

		HashMap < String , ArrayList > vars = stack.peek( );
		// add act as a last child
		addAsChild( vars , act );
		changeChildIndex( vars.get( MapKeys.CHILD_COMP ).size( ) - 1 , actNewInd , vars );
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		MapUtils.addToBIPCode( ret , ( String ) act.get( MapKeys.BIP_CODE ).get( 0 ) ); /**/
		StringTemplate template = AttrFiller.getTemplate( templateFile , "ACTW" );
		/* component's name contains the name of the enclosed comp :actw_[comp] */
		String childComp = ( String ) act.get( MapKeys.CHILD_COMP ).get( 0 );

		template.setAttribute( "id" , childComp );
		String compName = "actw_" + childComp;
		/* add the component's name to ret */
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );

		if ( hasTrg ) {
			template.setAttribute( "hasTrg" , hasTrg );
		}
		if ( hasSrc ) {
			template.setAttribute( "hasSrc" , hasSrc );
		}
		ret = sequencing( template , stack , ret );

		return ret;

	}


	public HashMap < String , ArrayList > applyPICK( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		// in this template some rcvMsgPorts have been replaced by onMsgPorts
		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "PICK" );
		template.setAttribute( "id" , templateList.getSize( ) + 1 );
		String compName = "pick_" + ( templateList.getSize( ) + 1 );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		HashMap < String , ArrayList > vars = stack.peek( );

		int compNum = vars.get( MapKeys.CHILD_COMP ).size( );
		template.setAttribute( "count" , String.valueOf( compNum ) );

		MapUtils.addToBIPCode( ret , "\n" + applyRDV( compNum , false ) );
		MapUtils.addToBIPCode( ret , "\n" + applyRDV( compNum , true ) );
		MapUtils.addToBIPCode( ret , "\n" + applyANY( compNum , true ) );

		ArrayList < Object > components = vars.get( MapKeys.CHILD_COMP );

		for ( int i = 0 ; i < components.size( ) ; i++ ) {
			template.setAttribute( MapKeys.CHILD_COMP , components.get( i ) );
		}

		ArrayList < Object > onMsgComponents = vars.get( MapKeys.ON_MESSAGE_PORTS );
		if ( onMsgComponents != null ) {
			MapUtils.addToBIPCode( ret , "\n" + applyPICKD( components.size( ) ) );

			for ( int i = 0 ; i < onMsgComponents.size( ) ; i++ ) {
				// get the onMsgPort, keep index and name
				// only one such port per component.
				if ( onMsgComponents.get( i ) != null ) {
					IOMsgPort ioPort = ( IOMsgPort ) ( ( ArrayList ) onMsgComponents.get( i ) ).get( 0 );
					ioPort.setCompIndex( String.valueOf( i + 1 ) );

					ArrayList < String > startDisPort = new ArrayList < String >( 1 + components.size( ) );
					startDisPort.addAll( ioPort.toArray4Template( "" ) );
					for ( int disIndex = 0 ; disIndex < components.size( ) ; disIndex++ ) {
						if ( disIndex == i ) {
							continue;
						}
						startDisPort.add( String.valueOf( disIndex + 1 ) );
					}
					AttrFiller.addToTemplate( startDisPort , template , "onMsgStartDis" );
					MapUtils.addToMap( ret , MapKeys.RCV_MESSAGE_PORTS , ioPort , -1 );
				}
			}
		}

		ArrayList < Object > onAlarmComponents = vars.get( "onAlarmPorts" );
		if ( onAlarmComponents != null ) {
			for ( int i = 0 ; i < onAlarmComponents.size( ) ; i++ ) {
				// as above
				if ( onAlarmComponents.get( i ) != null ) {
					Port alarmPort = ( Port ) ( ( ArrayList ) onAlarmComponents.get( i ) ).get( 0 );
					alarmPort.setCompIndex( String.valueOf( i + 1 ) );
					ArrayList < String > startDisPort = new ArrayList < String >( 1 + components.size( ) );
					startDisPort.addAll( alarmPort.toArray4Template( "" ) );
					for ( int disIndex = 0 ; disIndex < components.size( ) ; disIndex++ ) {
						if ( disIndex == i ) {
							continue;
						}
						startDisPort.add( String.valueOf( disIndex ) );
					}
					AttrFiller.addToTemplate( startDisPort , template , "onAlarmStartDis" );
				}
			}
		}
		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );

		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;
	}


	private HashMap < String , ArrayList > uniteConflImaPorts( HashMap < String , ArrayList > general ) {

		/* unite all chilren's conflIma maps (conflicting IMA's) */
		HashMap < String , ArrayList > conflIma = new HashMap < String , ArrayList >( );
		ArrayList < Object > componentsConflictingImas = null;

		if ( ( componentsConflictingImas = ( ArrayList < Object > ) general.get( MapKeys.CONFL_IMA_PORTS ) ) != null ) {

			// each component has a hashMap pl_op->
			for ( int i = 0 ; i < componentsConflictingImas.size( ) ; i++ ) {
				ArrayList componentsConflictingIma = ( ArrayList ) componentsConflictingImas.get( i );
				if ( componentsConflictingIma == null ) {
					continue;
				}
				if ( componentsConflictingIma.size( ) == 0 ) {
					continue;
				}
				HashMap < String , ArrayList > component = ( HashMap < String , ArrayList > ) componentsConflictingIma.get( 0 );
				if ( component == null ) {
					continue;
				}

				// for each pl_op in the conflImasMap
				for ( String pl_op : component.keySet( ) ) {
					// add pl_op to the conflIma general map
					if ( conflIma.get( pl_op ) == null ) {
						conflIma.put( pl_op , new ArrayList < Object >( ) );
					}
					// add each of the component's conflIma (and its
					// compIndex) to the existing entry
					for ( Object port : ( ArrayList < Object > ) component.get( pl_op ) ) {
						// add the components index i
						conflIma.get( pl_op ).add( ( ArrayList < String > ) port );
						( ( ArrayList < String > ) port ).add( 2 , String.valueOf( i + 1 ) );
					}
				}
			}
		}
		return conflIma;
	}


	public HashMap < String , ArrayList > applyFLOWN_BRANCH( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		HashMap < String , ArrayList > vars = stack.peek( );
		HashMap templatePortsMap = new HashMap( );

		String compName = "flowN_branch_" + ( templateList.getSize( ) + 1 );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "FLOWN_BRANCH" );
		template.setAttribute( "id" , ( templateList.getSize( ) + 1 ) );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		// add to map the data (i.e. index) handler for the branch
		// HashMap < String , ArrayList > dhs_flow = new HashMap < String ,
		// ArrayList >( );
		// String dhs_flow_name = "dhs_" + compName;
		// addAsChild( vars , dhs_flow );
		// template.setAttribute( "dataList" , elem.getAttribute(
		// "indexVariable" ) );

		for ( Object childComp : vars.get( MapKeys.CHILD_COMP ) ) {
			template.setAttribute( MapKeys.CHILD_COMP , childComp );
		}

		exportPorts( vars , templatePortsMap , template , ret , false );
		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;

	}


	public HashMap < String , ArrayList > applyFLOW( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack , int Nbranches )
			throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );

		// variables used in both flow and flowN cases
		HashMap < String , ArrayList > vars = stack.peek( );
		HashMap templatePortsMap = new HashMap( );

		StringTemplate template = null;
		String compName = "";

		if ( Nbranches == -1 ) {
			compName = "flow_" + ( templateList.getSize( ) + 1 );
			template = AttrFiller.getTemplate( templateFile , "FLOW" );
			template.setAttribute( "id" , ( templateList.getSize( ) + 1 ) );
			templateList.addInstance( compName );
			MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

			HashSet < String > links;
			if ( ( links = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.LINKS ) ) != null ) {
				for ( String link : links ) {
					template.setAttribute( "linksList" , link );
				}
			}

			int compNum = vars.get( MapKeys.CHILD_COMP ).size( );
			template.setAttribute( "count" , String.valueOf( compNum ) );

			for ( Object childComp : vars.get( MapKeys.CHILD_COMP ) ) {
				template.setAttribute( MapKeys.CHILD_COMP , childComp );
			}

			exportPorts( vars , templatePortsMap , template , ret , false );

			MapUtils.addToBIPCode( ret , "\n" + applyRDV( compNum , true ) );
			MapUtils.addToBIPCode( ret , "\n" + applyANY( compNum , true ) );

			MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		} else {
			HashMap < String , ArrayList > newRet = new HashMap < String , ArrayList >( );

			// create each branch: replace each childComp with branchComp (i.e.
			// wrapper)
			// for each branch:
			// 1. create a ret
			// 2. append the BIP code
			// 3. append ret info to this level's ret
			int compNum = 2;
			HashMap < String , ArrayList > flowNVars = new HashMap < String , ArrayList >( );
			for ( int index = 0 ; index < compNum ; index++ ) {

				// create branch
				// begin: add new vars entry for branchComp
				stack.push( new HashMap < String , ArrayList >( ) );
				HashMap < String , ArrayList > varsBranch = stack.peek( );
				// add childComp to branchComp: just rename
				lowerChildsLevel( varsBranch , vars , index );

				HashMap < String , ArrayList > variablesEntry = new HashMap < String , ArrayList >( 1 );
				variablesEntry.put( elem.getAttribute( "indexVariable" ) , new ArrayList <>( 0 ) );
				MapUtils.addToMapEntry( varsBranch , MapKeys.VARIABLES , variablesEntry );

				// produce branch code
				HashMap < String , ArrayList > retBranch = applyFLOWN_BRANCH( elem , stack );
				Object branchCode = MapUtils.getSingleEntry( retBranch , MapKeys.BIP_CODE );
				newRet = MapUtils.addToBIPCode( newRet , "\n\n" + ( String ) branchCode );
				retBranch.remove( MapKeys.BIP_CODE );
				retBranch.remove( MapKeys.CPP_CODE );

				// add retBranch to vars
				addAsChild( flowNVars , retBranch );
				// end : remove varsBranch from stack
				stack.pop( );
			}

			// add to flowNVars a flowN controller for the index variable
			// HashMap < String , ArrayList > flowN_ctrl = new HashMap < String
			// , ArrayList >( );
			// String flowN_ctrl_name = "FlowN_ctrl_" + compName;
			// addAsChild( flowNVars , flowN_ctrl );

			// procuce wrapper's code
			template = AttrFiller.getTemplate( templateFile , "FLOWNW" );
			compName = "flownw_" + ( templateList.getSize( ) + 1 );
			templateList.addInstance( compName );
			MapUtils.addToMap( newRet , MapKeys.CHILD_COMP , compName , -1 );
			template.setAttribute( "id" , templateList.getSize( ) );
			template.setAttribute( "count" , String.valueOf( vars.get( MapKeys.CHILD_COMP ).size( ) ) );
			for ( Object childComp : flowNVars.get( MapKeys.CHILD_COMP ) ) {
				template.setAttribute( MapKeys.CHILD_COMP , ( ( ArrayList ) childComp ).get( 0 ) );
			}

			templatePortsMap = new HashMap( );
			newRet = exportPorts( vars , templatePortsMap , template , newRet , false );

			newRet = MapUtils.addToBIPCode( newRet , "\n" + applyRDV( compNum , true ) );
			newRet = MapUtils.addToBIPCode( newRet , "\n" + applyANY( compNum , true ) );

			newRet = MapUtils.addToBIPCode( newRet , "\n\n" + template.toString( ) );
			return newRet;
		}

		return ret;
	}


	/*
	 * StringTemplate template, String[] childComp, String[][] expPorts,
	 * String[][] faultsList
	 */
	private HashMap < String , ArrayList > sequencing( StringTemplate template , ArrayDeque < HashMap < String , ArrayList >> stack ,
			HashMap < String , ArrayList > ret ) throws Exception {

		HashMap < String , ArrayList > vars = stack.peek( );
		MapUtils.addToBIPCode( ret , '\n' + applyConnTemplate( "COMPCTRL" ) );

		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );
		ArrayList childComp = new ArrayList( vars.get( MapKeys.CHILD_COMP ).size( ) );

		for ( int j = 0 ; j < vars.get( MapKeys.CHILD_COMP ).size( ) ; j++ ) {
			if ( vars.get( MapKeys.CHILD_COMP ).get( j ) != null ) {
				String childCompTmpl = ( String ) ( ( ArrayList ) vars.get( MapKeys.CHILD_COMP ).get( j ) ).get( 0 );
				template.setAttribute( MapKeys.CHILD_COMP , childCompTmpl );
				childComp.add( childCompTmpl );
			} else {
				String childCompTmpl = "cust";
				template.setAttribute( MapKeys.CHILD_COMP , childCompTmpl );
				childComp.add( childCompTmpl ); /* CUStringTemplate */
			}
		}

		MapUtils.addToBIPCode( ret , applyRDV( childComp.size( ) , true ) );
		MapUtils.addToBIPCode( ret , applyRDV( 2 , false ) );
		MapUtils.addToBIPCode( ret , applyANY( childComp.size( ) , true ) );

		String s = "\n";
		s += applyANY( Integer.valueOf( childComp.size( ) ) , true );
		// attribute startdone will be a list where each i'th startdone where i
		// in [1..childNum] is an array [i,i+1], so that the template will add
		// connectors for these component's done and start ports
		String [ ] startdone;
		for ( int i = 0 ; i < childComp.size( ) - 1 ; i++ ) {
			startdone = new String [ 2 ];
			startdone[ 0 ] = String.valueOf( i + 1 );
			startdone[ 1 ] = String.valueOf( i + 2 );
			AttrFiller.addToTemplate( startdone , template , "startdone" );
		}
		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , true );

		s += template.toString( );
		MapUtils.addToBIPCode( ret , s );

		return ret;
	}


	public static HashMap < String , ArrayList > manageFaultsList( HashMap < String , ArrayList > vars , Map templatePortsMap ,
			StringTemplate template , HashMap < String , ArrayList > ret ) {

		/* used only in structured activities */
		/* if there are no faults raised by children */
		ArrayList < ArrayList > components = vars.get( MapKeys.FAULTS_LIST );
		if ( components == null ) {
			return ret;
		}

		/* create an ArrayList with the components that throw some fault */
		// add components with non-empty faultsLists
		boolean throwsFault = false;
		for ( int i = 0 ; i < components.size( ) ; i++ ) {
			if ( components.get( i ) != null && components.get( i ).size( ) > 0 ) {
				throwsFault = true;
				if ( template.getName( ).equals( "FHW" ) ) {
					template.setAttribute( "expFaultsList" , i + 1 );
				} else if ( !template.getName( ).equals( "SCOPE" ) && !template.getName( ).equals( "PROCESS" ) ) {
					MapUtils.addToMap( templatePortsMap , "expFaultsList" , String.valueOf( i + 1 ) , -1 );
				}
			}
		}
		if ( throwsFault ) {
			MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "default" );
		}

		return ret;
	}


	private HashMap < String , ArrayList > exportPorts( HashMap < String , ArrayList > vars , HashMap templatePortsMap , StringTemplate template ,
			HashMap < String , ArrayList > ret , boolean sequential ) throws Exception {

		// handle in-message ports in scope: fill
		// csSet, meSet with the ima's different correlSets and MsgExchanges

		ArrayList < Object > components = null;
		ret = manageFaultsList( vars , templatePortsMap , template , ret );

		// if there are compensate ports (i.e. there is a compensate activity in
		// the current activity)
		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.COMPEN_PORTS ) ) != null ) {
			if ( template.getName( ).equals( "FHW" ) ) {
				MapUtils.addToBIPCode( ret , applyConnTemplate( "ASSC1" ) );
			}
			// if (!template.getName().equals("SCOPE") &&
			// !template.getName().equals("PROCESS")) {

			HashSet < String > compensateComponents = new HashSet < String >( );
			MapUtils.addToBIPCode( ret , applyConnTemplate( "RDIFD2" ) );
			MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );
			ArrayList < Object > componentsPorts = null;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {

					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						// export all compPorts
						CompenPort compPort = ( CompenPort ) componentsPorts.get( j );
						MapUtils.addToMapEntry( ret , MapKeys.COMPEN_PORTS , compPort );
						compPort.setCompIndex( String.valueOf( i + 1 ) );
						ArrayList < String > toTemplate = compPort.toArray4Template( "expComp" );
						MapUtils.addToBIPCode( ret , applyCPEB( 1 , 0 , true ) );
						if ( compPort.getTargetScope( ).equals( "" ) ) {
							MapUtils.addToMap( templatePortsMap , "expComp" , toTemplate , -1 );
						} else {
							MapUtils.addToMap( templatePortsMap , "expCompScope" , toTemplate , -1 );
						}
						if ( compensateComponents.add( compPort.getCompIndex( ) ) ) {
							MapUtils.addToMap( templatePortsMap , "expCompAll" , toTemplate , -1 );
						}
					}
				}
			}
			// }
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.RVRS_PORTS ) ) != null && components.size( ) > 0 ) {
			if ( !template.getName( ).equals( "SCOPE" ) && !template.getName( ).equals( "PROCESS" ) ) {
				ArrayList < Integer > reversibleComponentsIndices = new ArrayList < Integer >( components.size( ) );

				MapUtils.addToBIPCode( ret , applyConnTemplate( "RDD1" ) );
				MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );
				MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 2 } , true ) ); // needed

				ArrayList < Object > componentsPorts = null;

				for ( int i = 0 ; i < components.size( ) ; i++ ) {
					if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) == null || componentsPorts.size( ) == 0 ) {
						continue;
					}
					// add this component's index to
					// reversibleComponentsIndices
					reversibleComponentsIndices.add( i + 1 );
					// add to template every rvrsPort as expRvrsScope
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						RvrsPort rvrsPort = ( RvrsPort ) componentsPorts.get( j );
						rvrsPort.setCompIndex( String.valueOf( i + 1 ) );
						ArrayList < String > toTemplate = rvrsPort.toArray4Template( "expRvrsScope" );

						MapUtils.addToMap( templatePortsMap , "expRvrsScope" , toTemplate , -1 );
						MapUtils.addToMapEntry( ret , MapKeys.RVRS_PORTS , rvrsPort );
						break;
					}
				}
				if ( sequential ) {
					MapUtils.addToBIPCode( ret , applyASS( 0 , 1 ) ); // changed
					// that
					ArrayList < String > startdone;
					// if ( reversibleComponentsIndices.size( ) > 0 ) {
					MapUtils.addToBIPCode( ret , applyConnTemplate( "RVRSCSD" ) );
					MapUtils.addToBIPCode( ret , applyConnTemplate( "RVRSSD" ) );
					MapUtils.addToBIPCode( ret , applyConnTemplate( "RVRSDSD" ) );
					MapUtils.addToBIPCode( ret , applyTRMRVSD( 2 ) );
					MapUtils.addToBIPCode( ret , applyTRMRVSD( reversibleComponentsIndices.size( ) ) );
					// }
					if ( reversibleComponentsIndices.size( ) > 1 ) {
						for ( int i = 0 ; i < reversibleComponentsIndices.size( ) - 1 ; i++ ) {
							startdone = new ArrayList < String >( 2 );
							startdone.add( String.valueOf( reversibleComponentsIndices.get( i ) ) );
							startdone.add( String.valueOf( String.valueOf( ( Integer ) reversibleComponentsIndices.get( i ) + 1 ) ) );
							MapUtils.addToMap( templatePortsMap , "rvrsStartdone" , startdone , -1 );
						}
						MapUtils.addToBIPCode( ret , applyRDV( 2 , false ) );
						MapUtils.addToBIPCode( ret , applyConnTemplate( "RVSIND" ) );
						MapUtils.addToBIPCode( ret , applyConnTemplate( "TRMRVSIND" ) );
						MapUtils.addToBIPCode( ret , applyConnTemplate( "RVSIN" ) );
					}
				} else {
					MapUtils.addToBIPCode( ret , applyTRMRVSD( reversibleComponentsIndices.size( ) ) );
					MapUtils.addToBIPCode( ret , applyPRRVRSX( reversibleComponentsIndices.size( ) ) );
					MapUtils.addToBIPCode( ret , applyPRENDRVRSX( reversibleComponentsIndices.size( ) ) );
				}
				if ( reversibleComponentsIndices.size( ) > 0 ) {

					MapUtils.addToBIPCode( ret , applyCMP( reversibleComponentsIndices.size( ) ) );
					MapUtils.addToBIPCode( ret , applyTRMCMP( reversibleComponentsIndices.size( ) ) );
					MapUtils.addToBIPCode( ret , applyENDCMP( reversibleComponentsIndices.size( ) ) );
					MapUtils.addToMap( templatePortsMap , "expRvrs" , String.valueOf( reversibleComponentsIndices.size( ) ) , -1 );

					for ( int i = 0 ; i < reversibleComponentsIndices.size( ) ; i++ ) {
						MapUtils.addToMap( templatePortsMap , "expRvrs" , String.valueOf( reversibleComponentsIndices.get( i ) ) , -1 );
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.IMA_PORTS ) ) != null ) {

			ArrayList < Object > componentsPorts = null;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						IOMsgPort imaPort = ( IOMsgPort ) componentsPorts.get( j );
						imaPort.setCompIndex( String.valueOf( i + 1 ) );
						/*
						 * add the ima to template it is in format (numCS,
						 * keyPort, meFlag, info) and it must be in the
						 * format(numCS, keyport, compInd, meFlag)
						 */
						if ( imaPort.isExported( "expIma" ) ) {
							ArrayList < String > toTemplate = imaPort.toArray4Template( "expIma" );
							/* export the ima port further */

							if ( imaPort.isRcvIO( ) ) {
								MapUtils.addToMap( templatePortsMap , "expIoma" , toTemplate , -1 );
							}
							MapUtils.addToMap( templatePortsMap , "expIma" , toTemplate , -1 );
						}
						if ( imaPort.isExported( "disIma" ) ) {
							ArrayList < String > toTemplate = imaPort.toArray4Template( "" );
							MapUtils.addToMap( templatePortsMap , "expDisIma" , toTemplate , -1 );
						}
						if ( imaPort.isExported( "expIma" ) || imaPort.isExported( "disIma" ) ) {
							MapUtils.addToMapEntry( ret , MapKeys.IMA_PORTS , imaPort );
						}
					}
				}
			}

			/* join conflImas */
			HashMap < String , ArrayList > conflIma;
			if ( ( conflIma = uniteConflImaPorts( vars ) ) != null ) {
				HashMap < String , ArrayList < ArrayList >> retConflIma = new HashMap < String , ArrayList < ArrayList >>( );
				MapUtils.addToMapEntry( ret , MapKeys.CONFL_IMA_PORTS , retConflIma );

				// for each receive with the same partnerLink and operation
				// (pl_op) , get these conflicts
				for ( String pl_op : conflIma.keySet( ) ) {
					ArrayList < ArrayList > imasConflicts = conflIma.get( pl_op );

					ArrayList < String > complIndices = new ArrayList < String >( imasConflicts.size( ) );
					/* addAll to template */
					for ( ArrayList ima : imasConflicts ) {
						String compInd = ( String ) ima.get( 2 );
						String keyPort = ( String ) ima.get( 1 );
						ima.remove( 2 ); // remove compInd
						complIndices.add( compInd + ".checkAR_" + keyPort );
					}

					ConflIMAPort conflImaPort = new ConflIMAPort( null , null , pl_op , String.valueOf( imasConflicts.size( ) ) , complIndices );
					ArrayList < String > toTemplate = conflImaPort.toArray4Template( "expConflIma" );
					MapUtils.addToBIPCode( ret , applyCHKRC2XD( imasConflicts.size( ) ) );

					if ( imasConflicts.size( ) > 0 ) {
						MapUtils.addToBIPCode( ret , applyCHKRC2X( imasConflicts.size( ) ) );
					}
					MapUtils.addToMap( templatePortsMap , "expConflIma" , toTemplate , -1 );

					/* addAll to returned map */
					retConflIma.put( pl_op , imasConflicts );
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.WRITE_LINK_PORTS ) ) != null ) {

			HashSet < String > links = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.LINKS );

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsWrtLnk = ( ArrayList < Object > ) components.get( i );
				if ( ( componentsWrtLnk = ( ArrayList < Object > ) components.get( i ) ) == null ) {
					continue;
				}
				for ( int j = 0 ; j < componentsWrtLnk.size( ) ; j++ ) {
					RwPort wrtLnk = ( RwPort ) componentsWrtLnk.get( j );

					wrtLnk.setCompIndex( String.valueOf( i + 1 ) );

					if ( links != null && links.contains( wrtLnk.parts( ).get( 0 ) ) ) {

						ArrayList < String > toTemplate = wrtLnk.toArray4Template( "wrtLnkPorts" );
						MapUtils.addToMap( templatePortsMap , "wrtLnkPorts" , toTemplate , -1 );
						MapUtils.addToBIPCode( ret , applyCP2EB( 1 , 0 ) );
					} else {
						ArrayList < String > toTemplate = wrtLnk.toArray4Template( "expWrtLnkPorts" );
						MapUtils.addToMap( templatePortsMap , "expWrtLnkPorts" , toTemplate , -1 );
						MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );
						MapUtils.addToMapEntry( ret , MapKeys.WRITE_LINK_PORTS , wrtLnk );
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.READ_LINK_PORTS ) ) != null ) {

			HashSet < String > links = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.LINKS );

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < Object > componentsPorts = ( ArrayList < Object > ) components.get( i );
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						RwPort port = ( RwPort ) componentsPorts.get( j );
						// clone the port to export it, and remove the
						// non-exported links
						RwPort port2exp = port.clone( );

						port.setCompIndex( String.valueOf( i + 1 ) );

						boolean existLinks = false;
						if ( links != null ) {

							for ( String link : port.parts( ) ) {
								if ( links.contains( link ) ) {
									port2exp.parts( ).remove( link );
									existLinks = true;
								}
							}
						}
						// at least one link is in this flow, then we create
						if ( existLinks ) {
							int hereNums = port.parts( ).size( ) - Integer.valueOf( port2exp.parts( ).size( ) );
							port.setHereNums( hereNums );

							ArrayList < String > toTemplate = port.toArray4Template( "rdLnkPorts" );
							AttrFiller.addToTemplate( toTemplate , template , "rdLnkPorts" );

							MapUtils.addToBIPCode( ret , applyRDLNK( port.parts( ).size( ) , hereNums ) );
							if ( port2exp.parts( ).size( ) > 0 ) {
								MapUtils.addToMapEntry( ret , MapKeys.READ_LINK_PORTS , port2exp );
							}
						} else {
							ArrayList < String > toTemplate = port.toArray4Template( "expRdLnkPorts" );
							MapUtils.addToMap( templatePortsMap , "expRdLnkPorts" , toTemplate , -1 );
							MapUtils.addToBIPCode( ret , applyRDVAR( port.parts( ).size( ) , true ) );
							MapUtils.addToMapEntry( ret , MapKeys.READ_LINK_PORTS , port );
						}
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.OMA_PORTS ) ) != null ) {

			ArrayList < Object > componentsPorts = null;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						IOMsgPort oma = ( IOMsgPort ) componentsPorts.get( j );
						oma.setCompIndex( String.valueOf( i + 1 ) );

						ArrayList < String > toTemplate = oma.toArray4Template( "expOma" );
						/*
						 * TODO toTemplate.add(0, (String) oma.get(0)); // numCS
						 * toTemplate.add(1, (String) oma.get(1));// keyport
						 * toTemplate.add(2, String.valueOf(i + 1)); // compInd
						 * // toTemplate.add(3, (String) oma.get(2));// meFlag
						 */
						MapUtils.addToMap( templatePortsMap , "expOma" , toTemplate , -1 );
						MapUtils.addToMapEntry( ret , MapKeys.OMA_PORTS , oma );

					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.INV_PORTS ) ) != null ) {

			ArrayList < Object > componentsPorts = null;
			MapUtils.addToBIPCode( ret , applyCHKMR( true ) );
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						IOMsgPort inv = ( IOMsgPort ) componentsPorts.get( j );

						// if the partnerLink in the inv is declared in a higher
						// scope, then inv is further exported
						inv.setCompIndex( String.valueOf( i + 1 ) );
						ArrayList < String > toTemplate = inv.toArray4Template( "expInv" );
						MapUtils.addToMap( templatePortsMap , "expInv" , toTemplate , -1 );
						MapUtils.addToMapEntry( ret , MapKeys.INV_PORTS , inv );
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.INV_IMA_PORTS ) ) != null ) {

			ArrayList < Object > componentsPorts = null;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						IOMsgPort inv = ( IOMsgPort ) componentsPorts.get( j );

						// if the partnerLink in the invIma is declared in a
						// higher
						// scope, then inv is further exported
						MapUtils.addToMapEntry( ret , MapKeys.INV_IMA_PORTS , inv );
						inv.setCompIndex( String.valueOf( i + 1 ) );
						ArrayList < String > toTemplate = inv.toArray4Template( "expInvIma" );
						MapUtils.addToMap( templatePortsMap , "expInvIma" , toTemplate , -1 );
					}
				}
			}
		}

		// manageReadWritePorts: StringTemplateART
		// This part exists in applySCOPE
		// 1: declare map entries for ports' transformation
		String [ ] s = new String [ ] { MapKeys.READ_PORTS , MapKeys.WRITE_PORTS };
		ArrayList < String > entries = new ArrayList < String >( Arrays.asList( s ) );
		// 2: initialize reusable info by adding entries to vars
		initializePortsTransform( vars , entries );
		// 3: transform ports
		transformMapEntryPorts( vars , templatePortsMap , template , ret , entries , sequential );
		// 4: do custom jobs depending on the activity under translation
		HashSet < String > dataList;
		if ( ( dataList = ( HashSet < String > ) vars.get( MapKeys.DATA_LIST ).get( 0 ) ) != null ) {
			for ( String data : ( HashSet < String > ) dataList ) {
				template.setAttribute( "dataList" , data );
			}
		}
		// 5: erase temporary map entries
		finalizePortsTransform( vars );
		// manageReadWritePorts: END

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.RCV_MESSAGE_PORTS ) ) != null ) {
			ArrayList < Object > componentsPorts = null;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						IOMsgPort port = ( IOMsgPort ) componentsPorts.get( j );
						port.setCompIndex( String.valueOf( i + 1 ) );
						ArrayList < String > toTemplate = port.toArray4Template( "expRcvMsgPorts" );

						MapUtils.addToBIPCode( ret , applyCPEB( 1 , 0 , true ) );
						MapUtils.addToMap( templatePortsMap , "expRcvMsgPorts" , toTemplate , -1 );

						MapUtils.addToMapEntry( ret , MapKeys.RCV_MESSAGE_PORTS , port );
					}
				}
			}
		}

		if ( ( components = ( ArrayList < Object > ) vars.get( "sndMsgPorts" ) ) != null ) {
			ret.put( "sndMsgPorts" , new ArrayList( ) );
			ArrayList < Object > componentsPorts = null;
			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				if ( ( componentsPorts = ( ArrayList < Object > ) components.get( i ) ) != null ) {
					for ( int j = 0 ; j < componentsPorts.size( ) ; j++ ) {
						IOMsgPort port = ( IOMsgPort ) componentsPorts.get( j );
						port.setCompIndex( String.valueOf( i + 1 ) );
						ArrayList < String > toTemplate = port.toArray4Template( "expSndMsgPorts" );

						MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { 1 } , true ) );
						MapUtils.addToMap( templatePortsMap , "expSndMsgPorts" , toTemplate , -1 );

						MapUtils.addToMapEntry( ret , MapKeys.SND_MESSAGE_PORTS , port );
					}
				}
			}
		}

		String [ ][ ] basicExpPorts;
		if ( template.getName( ).equals( "FHW" ) ) {
			basicExpPorts = new String [ ] [ ] { new String [ ] { MapKeys.EXIT_PORTS , "expExitPorts" } };
		} else {
			basicExpPorts = new String [ ] [ ] { new String [ ] { MapKeys.EXIT_PORTS , "expExitPorts" } ,
					new String [ ] { MapKeys.RETHROW_PORTS , "expRethrowPorts" } };
		}
		int port_count = 0;

		for ( int i = 0 ; i < basicExpPorts.length ; i++ ) {
			if ( ( components = ( ArrayList < Object > ) vars.get( basicExpPorts[ i ][ 0 ] ) ) != null ) {

				ret.put( basicExpPorts[ i ][ 0 ] , new ArrayList( ) );
				for ( int j = 0 ; j < components.size( ) ; j++ ) {

					ArrayList < ArrayList > componentsExps = ( ArrayList < ArrayList > ) components.get( j );
					if ( componentsExps != null && componentsExps.size( ) > 0 ) {
						ArrayList < String > toTemplate = new ArrayList( 1 );
						toTemplate.add( Integer.toString( j + 1 ) );
						MapUtils.addToMap( templatePortsMap , basicExpPorts[ i ][ 1 ] , toTemplate , -1 );
						port_count++;
					}
				}
				if ( port_count > 0 ) {
					ArrayList al = new ArrayList( 1 );
					al.add( "1" ); // symbolic value
					ret.get( basicExpPorts[ i ][ 0 ] ).add( al );
				}
			}

		}

		if ( ( components = ( ArrayList < Object > ) vars.get( MapKeys.ON_IMA_PORTS ) ) != null ) {
			ArrayList < String > toTemplate = new ArrayList( components.size( ) + 1 );

			for ( int i = 0 ; i < components.size( ) ; i++ ) {
				ArrayList < ArrayList > componentsExps = ( ArrayList < ArrayList > ) components.get( i );
				// add the components with onIMA ports to the list
				if ( componentsExps != null && componentsExps.size( ) > 0 ) {
					toTemplate.add( String.valueOf( i + 1 ) );
				}
			}
			if ( toTemplate.size( ) > 0 ) {
				MapUtils.addToBIPCode( ret , applyANY( toTemplate.size( ) , true ) );
				MapUtils.addToBIPCode( ret , applyRDV( toTemplate.size( ) , true ) );
				toTemplate.add( 0 , String.valueOf( toTemplate.size( ) ) );
				MapUtils.addToMap( templatePortsMap , "expOnIMAs" , toTemplate , -1 );
				MapUtils.addToMapEntry( ret , MapKeys.ON_IMA_PORTS , Arrays.asList( new String [ ] { "1" } ) );
			}

		}

		filler.addMapToTemplate( template , templatePortsMap , "ports" );
		return ret;
	}


	public static HashMap < String , ArrayList > transformMapEntryPorts( HashMap < String , ArrayList > vars , HashMap templatePortsMap ,
			StringTemplate template , HashMap < String , ArrayList > ret , ArrayList < String > mapEntries , boolean sequential ) throws Exception {

		ArrayList components;

		for ( String mapEntry : mapEntries ) {
			if ( ( components = vars.get( mapEntry ) ) != null && components.size( ) > 0 ) {
				ret.put( mapEntry , new ArrayList < Object >( ) );

				/* for each child ports */
				for ( int p = 0 ; p < components.size( ) ; p++ ) {
					if ( components.get( p ) == null ) {
						continue;
					}
					ArrayList < Object > componentPorts = ( ArrayList < Object > ) components.get( p );
					for ( int i = 0 ; i < componentPorts.size( ) ; i++ ) {

						Port port = ( Port ) componentPorts.get( i );
						port.setCompIndex( MapUtils.getComponentIndex( vars , template , p ) );

						ret = transformPort( vars , templatePortsMap , template , ret , mapEntry , port );
					}
				}
			}

		}

		return ret;
	}


	public static HashMap < String , ArrayList > initializePortsTransform( HashMap < String , ArrayList > vars , ArrayList < String > mapEntries ) {

		if ( ! ( mapEntries.contains( MapKeys.READ_PORTS ) & mapEntries.contains( MapKeys.WRITE_PORTS ) ) ) {
			return vars;
		}
		// varHash exists if this function has been called before, even if
		// it's empty
		if ( vars.containsKey( MapKeys.VARIABLES_HASH ) ) {
			return vars;

		}
		// variables_hash will hold all assignable scope data.
		HashSet < String > variables_hash = new HashSet < String >( );

		HashMap < String , ArrayList < String >> variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
				MapKeys.VARIABLES );
		if ( variables != null ) {
			for ( String var : variables.keySet( ) ) {
				if ( variables.get( var ).size( ) == 0 ) {
					variables_hash.add( var );
				} else {
					variables_hash.addAll( variables.get( var ) );
				}
			}
		}
		variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars , MapKeys.PARTNER_LINKS );
		if ( variables != null ) {
			for ( String var : variables.keySet( ) ) {
				variables_hash.add( var + "_pRole" );
			}
		}
		// add to varHash the variable's parts if any (they are
		// named as var_part)
		MapUtils.addToMapEntry( vars , MapKeys.VARIABLES_HASH , variables_hash );
		MapUtils.addToMapEntry( vars , MapKeys.DATA_LIST , new HashSet < String >( ) );

		return vars;
	}


	public static HashMap < String , ArrayList > finalizePortsTransform( HashMap < String , ArrayList > vars ) {

		vars.remove( MapKeys.VARIABLES_HASH );
		vars.remove( MapKeys.DATA_LIST );
		return vars;
	}


	public static HashMap < String , ArrayList > transformPort( HashMap < String , ArrayList > vars , HashMap templatePortsMap ,
			StringTemplate template , HashMap < String , ArrayList > ret , String portType , Port port ) throws Exception {

		if ( portType.equals( MapKeys.READ_PORTS ) )
			return transformRWPort( vars , templatePortsMap , template , ret , port , MapKeys.READ_PORTS );
		if ( portType.equals( MapKeys.WRITE_PORTS ) )
			return transformRWPort( vars , templatePortsMap , template , ret , port , MapKeys.WRITE_PORTS );
		return ret;

	}


	public static HashMap < String , ArrayList > transformRWPort( HashMap < String , ArrayList > vars , HashMap templatePortsMap ,
			StringTemplate template , HashMap < String , ArrayList > ret , Port port , String mapKey ) throws Exception {

		RwPort writePort = ( RwPort ) port; // the port in Map
		// clone of the writePort to manipulate for template
		// RwPort forTemplate = writePort.clone( );
		// get parts declared in scope
		HashSet < String > partsInScopeSet = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.VARIABLES_HASH );
		HashSet < String > dataList = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.DATA_LIST );

		int hereNum = 0; // num of port variable_parts defined in scope

		// copy ports in assign are ordered: leftmost ports with parts declared
		// higher
		ArrayList < String > partsInPort = new ArrayList < String >( writePort.parts( ) );

		for ( String part : partsInPort ) {
			if ( partsInScopeSet != null && partsInScopeSet.contains( part ) ) {
				// if the part is in the scope
				hereNum++; // increase here parts num
				// remove the part from writePort (they won't be exported)
				// writePort.getVarParts( ).remove( part );
				// add the part to the dataList (used in scope data store)
				dataList.add( part );
			} else {
				// if the part is not in the scope
				// remove the part to be exported
				// forTemplate.getVarParts( ).remove( part );
			}
		}

		if ( hereNum > 0 ) {
			// if there are parts in this scope: add to template as "intWPorts"

			// forTemplate.setHereNums( String.valueOf( hereNum ) );
			// prepare writePort for template
			int varsNum = writePort.parts( ).size( );

			String templateAttr = "";
			if ( mapKey.equals( MapKeys.WRITE_PORTS ) ) {
				templateAttr = "intWPorts";
				MapUtils.addToBIPCode( ret , applyASS( varsNum - hereNum , hereNum ) );
			} else if ( mapKey.equals( MapKeys.READ_PORTS ) ) {
				templateAttr = "intRPorts";
				MapUtils.addToBIPCode( ret , applyRDD( varsNum - hereNum , hereNum ) );
			}

			RwPort forTemplate = writePort.clone( );
			writePort.parts( ).removeAll( dataList );

			// prepare port for template
			forTemplate.setHereNums( hereNum );
			forTemplate.setContNums( varsNum - hereNum );
			forTemplate.parts( ).removeAll( writePort.parts( ) );

			ArrayList < String > toTemplate = forTemplate.toArray4Template( templateAttr );
			AttrFiller.addToTemplate( toTemplate , template , templateAttr );

			if ( varsNum - hereNum > 0 ) {
				// if there are var_parts in higher scopes, export the writePort
				ret.get( mapKey ).add( writePort );
				MapUtils.addToMap( templatePortsMap , templateAttr , toTemplate , -1 ); //
			}
		} else {
			// if there are no parts in this scope: add to template as
			// "expWPorts"
			String templateAttr = "";
			if ( mapKey.equals( MapKeys.WRITE_PORTS ) ) {
				templateAttr = "expWPorts";
				MapUtils.addToBIPCode( ret , applyAAS( new int [ ] { writePort.parts( ).size( ) } , true ) );
			} else if ( mapKey.equals( MapKeys.READ_PORTS ) ) {
				templateAttr = "expRPorts";
				MapUtils.addToBIPCode( ret , applyRDVAR( writePort.parts( ).size( ) , true ) );
				ret = MapUtils.addToCPPCode( applyRDVAR_DOWN_CPP( writePort.parts( ).size( ) ) , ret );
				ret = MapUtils.addToHPPCode( applyRDVAR_DOWN_HPP( writePort.parts( ).size( ) ) , ret );
			}

			ArrayList < String > toTemplate = writePort.toArray4Template( templateAttr );
			MapUtils.addToMap( templatePortsMap , templateAttr , toTemplate , -1 );
			// export the port
			ret.get( mapKey ).add( writePort );
			// MapUtils.addToBIPCode( applyAAS( new int [ ] {
			// writePort.getVarParts( ).size( ) } , true ) , ret );
		}
		return ret;

	}


	private HashMap < String , ArrayList > transformReadPort( HashMap < String , ArrayList > vars , HashMap templatePortsMap ,
			StringTemplate template , HashMap < String , ArrayList > ret , Port port ) throws Exception {

		RwPort readPort = ( RwPort ) port; // the port in Map
		// clone of the readPort and process for template
		RwPort forTemplate = readPort.clone( );
		// get parts declared in scope
		HashSet < String > partsInScopeSet = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.VARIABLES_HASH );
		HashSet < String > dataList = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.DATA_LIST );

		// int hereNum = 0; // num of var_parts defined in scope
		// var_parts are ordered: leftmost ports with parts declared
		// higher
		ArrayList partsInPort = new ArrayList( readPort.parts( ) );
		for ( String part : ( ArrayList < String > ) partsInPort ) {
			if ( partsInScopeSet != null && partsInScopeSet.contains( part ) ) {
				// if the part is in the scope
				// hereNum++; // increase here parts num
				// portHereNum++; // increase q
				// remove the part from readPort (it won't be exported)
				readPort.parts( ).remove( part );
				// add the part to the dataList (used in scope data store)
				dataList.add( part );
			} else {
				// if the part is not in the scope
				// l++; // increase l
				// remove the part to be exported
				forTemplate.parts( ).remove( part );
			}
		}

		int hereNum = forTemplate.parts( ).size( );
		forTemplate.setHereNums( hereNum );
		if ( hereNum > 0 ) {
			// if there are parts in this scope: add to template as "intWPorts"
			ArrayList < String > toTemplate = forTemplate.toArray4Template( "intRPorts" );
			AttrFiller.addToTemplate( toTemplate , template , "intRPorts" );
			// calculate varsNum as the parts of the write port + the parts
			// declared here
			int varsNum = readPort.parts( ).size( ) + hereNum;

			// MapUtils.addToBIPCode( applyRDVAR( readPort.getVarParts(
			// ).size( ),
			// false ) , ret );
			// ret = addToCPPCode( applyRDVAR_DOWN_CPP( readPort.getVarParts(
			// ).size( ) ) , ret );
			// ret = addToHPPCode( applyRDVAR_DOWN_HPP( readPort.getVarParts(
			// ).size( ) ) , ret );

			MapUtils.addToBIPCode( ret , applyRDD( varsNum , hereNum ) );
			if ( hereNum < varsNum ) {
				// if there are var_parts in higher scopes, export the writePort
				MapUtils.addToMapEntry( ret , MapKeys.READ_PORTS , readPort );
			}
		} else {
			// if there are no parts in this scope: add to template as
			// "expWPorts"
			ArrayList < String > toTemplate = readPort.toArray4Template( "expRPorts" );
			MapUtils.addToMap( templatePortsMap , "expRPorts" , toTemplate , -1 );
			// export the port
			MapUtils.addToMapEntry( ret , MapKeys.READ_PORTS , readPort );
			// MapUtils.addToBIPCode( applyAAS( new int [ ] {
			// readPort.getVarParts(
			// ).size( ) } , true ) , ret );

			MapUtils.addToBIPCode( ret , applyRDVAR( readPort.parts( ).size( ) , true ) );
			ret = MapUtils.addToCPPCode( applyRDVAR_DOWN_CPP( readPort.parts( ).size( ) ) , ret );
			ret = MapUtils.addToHPPCode( applyRDVAR_DOWN_HPP( readPort.parts( ).size( ) ) , ret );
		}
		return ret;

	}


	private HashMap < String , ArrayList > transformReadPort_old( HashMap < String , ArrayList > vars , HashMap templatePortsMap ,
			StringTemplate template , HashMap < String , ArrayList > ret , Port port ) throws Exception {

		RwPort readPort = ( RwPort ) port;
		HashSet varHash = ( HashSet ) vars.get( MapKeys.VARIABLES_HASH ).get( 0 );
		// varHash exists in Scope
		if ( varHash != null && varHash.contains( readPort.parts( ).get( 0 ) ) ) {
			/* include it in readdata and intRPorts */
			HashSet < String > dataList = ( HashSet < String > ) MapUtils.getSingleEntry( vars , MapKeys.DATA_LIST );
			dataList.addAll( readPort.parts( ) );

			MapUtils.addToBIPCode( ret , applyRDVAR( readPort.parts( ).size( ) , false ) );
			ret = MapUtils.addToCPPCode( applyRDVAR_DOWN_CPP( readPort.parts( ).size( ) ) , ret );
			ret = MapUtils.addToHPPCode( applyRDVAR_DOWN_HPP( readPort.parts( ).size( ) ) , ret );
			// intRPorts: 0:(varNums0, portName0, comp0, parts0)

			ArrayList < String > toTemplate = readPort.toArray4Template( "intRPorts" );
			AttrFiller.addToTemplate( toTemplate , template , "intRPorts" );

		} else {
			/* include it in returned map's readPorts */
			/* as 0:(varNums0, portName0, lastInd0) */
			ArrayList < String > toTemplate = readPort.toArray4Template( "expRPorts" );
			MapUtils.addToMap( templatePortsMap , "expRPorts" , toTemplate , -1 );
			MapUtils.addToMapEntry( ret , MapKeys.READ_PORTS , readPort );
			MapUtils.addToBIPCode( ret , applyRDVAR( readPort.parts( ).size( ) , true ) );
		}

		return ret;
	}


	public static String sanitize( String input ) {

		String prev = input;
		prev = prev.replaceAll( "-" , "_" );
		prev = prev.replaceAll( "\\." , "_0_" );
		return prev;
	}


	private ArrayList < String > parseVarParts( String variableString , ArrayDeque < HashMap < String , ArrayList >> stack ) {

		ArrayList < String > parts = new ArrayList < String >( );

		int ind = variableString.indexOf( "." );
		if ( ind > 0 ) {
			String variable = variableString.replace( "$" , "" );
			String part = variable.replace( '.' , '_' );
			parts.add( part );
		} else {
			String variable = variableString.replace( "$" , "" );
			parts = MapUtils.getVariableParts( stack , variable );
		}
		return parts;
	}


	private String getFaultCompleteName( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) {

		String faultName = ( elem.hasAttribute( "faultName" ) ) ? elem.getAttribute( "faultName" ) : "";
		String faultMessageType = ( elem.hasAttribute( "faultMessageType" ) ) ? elem.getAttribute( "faultMessageType" ) : elem
				.getAttribute( "faultElement" );
		if ( faultMessageType == null ) {
			faultMessageType = getVariableType( stack , faultName ); // added
																		// this
																		// for
																		// BPEL1.1
		}

		/* the fault caught is defined by the faultName?_faultMessageType? */
		String catchCompleteName = ( faultName.equals( "" ) ) ? "all" : faultName + "_" + faultMessageType;
		catchCompleteName = catchCompleteName.replace( ":" , "__" );
		return catchCompleteName;
	}


	private String getVariableType( ArrayDeque < HashMap < String , ArrayList >> stack , String varName ) {

		String type = null;
		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			String variablesMsg = ( String ) MapUtils.getSingleEntry( vars , MapKeys.VARIABLES_MSG );
			if ( variablesMsg != null ) {
				type = variablesMsg;
			}
		}
		return type;
	}


	private String applyFOBSERVER( ArrayList < String > fhlist ) throws Exception {

		StringTemplate template;
		if ( !templateList.addInstance( "FaultObserver" ) ) {
			template = AttrFiller.getTemplate( templateFile , "FOBSERVER" );
			template.setAttribute( "FHandlersList" , fhlist );
			return template.toString( );
		}
		return "";
	}


	private String applyCMP( int intN ) throws Exception {

		String templateLabel = "CMP";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		return template.toString( );
	}


	private String applyRVRSDSD( int intN ) throws Exception {

		String templateLabel = "RVRSDSD";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		return template.toString( );
	}


	private String applyPRRVRSX( int intN ) throws Exception {

		String templateLabel = "PRRVRSX";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		return template.toString( );
	}


	private String applyPRENDRVRSX( int intN ) throws Exception {

		String templateLabel = "PRENDRVRSX";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		return template.toString( );
	}


	private String applyTRMRVSD( int intN ) throws Exception {

		String templateLabel = "TRMRVSD";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		return template.toString( );
	}


	private String applyCMPSC( ) throws Exception {

		StringTemplate template;
		if ( ( template = prepareTemplate( "CMPSC" , "CMPSC" ) ) == null ) {
			return "";
		}
		return template.toString( );
	}


	private String applyHLDCMP( ) throws Exception {

		StringTemplate template;
		if ( ( template = prepareTemplate( "HLDCMP" , "HLDCMP" ) ) == null ) {
			return "";
		}
		return template.toString( );
	}


	private String applyHLDENDCMP( ) throws Exception {

		StringTemplate template;
		if ( ( template = prepareTemplate( "HLDENDCMP" , "HLDENDCMP" ) ) == null ) {
			return "";
		}
		return template.toString( );
	}


	private String applyTRMCMP( int intN ) throws Exception {

		String templateLabel = "TRMCMP";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumAndPWsetToTemplate( template , "intList" , 1 , intN );

		return template.toString( );
	}


	private String applyENDCMP( int intN ) throws Exception {

		String templateLabel = "ENDCMP";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );

		return template.toString( );
	}


	private String applyPICKD( int intN ) throws Exception {

		if ( intN == 0 ) {
			return "";
		}
		String templateLabel = "PICKD";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 2 , intN );
		return template.toString( );
	}


	public static String applyRDV( int intN , boolean withExport ) {

		if ( intN == 0 ) {
			return "";
		}
		String templateLabel = ( withExport ) ? "RDVD" : "RDV";
		String templateName = templateLabel + intN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		return template.toString( );
	}


	public static StringTemplate prepareTemplate( String templateName , String templateLabel ) {

		try {
			if ( templateList.addInstance( templateName ) == true ) {
				return AttrFiller.getTemplate( templateFile , templateLabel );
			}
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		return null;
	}


	public static String applyEmptyTemplate( String templateName , String templateLabel ) {

		StringTemplate template = prepareTemplate( templateName , templateLabel );
		if ( template == null ) {
			return "";
		}else{
			return template.toString( );
		}

	}


	public static String applyRDVAR( int varsNum , boolean withExport ) throws Exception {

		String templateLabel = ( withExport ) ? "RDVARD" : "RDVAR";
		String templateName = templateLabel + varsNum;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		String s = "\n";
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , varsNum );
		// s += applyPortTypeDecl(intN, 0);

		s += "\n\n" + template.toString( );
		return s;
	}


	public static String applyRDVAR_DOWN_HPP( int intN ) throws Exception {

		if ( !templateList.addInstance( "RDVAR_DOWN_HPP" + intN ) ) {
			return "";
		}

		StringTemplate template = AttrFiller.getTemplate( templateFile , "RDVAR_DOWN_HPP" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		template.setAttribute( "intNum" , intN );
		return "\n\n" + template.toString( );
	}


	public static String applyRDVAR_DOWN_CPP( int intN ) throws Exception {

		if ( !templateList.addInstance( "RDVAR_DOWN_CPP" + intN ) ) {
			return "";
		}
		StringTemplate template = AttrFiller.getTemplate( templateFile , "RDVAR_DOWN_CPP" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intN );
		template.setAttribute( "intNum" , intN );
		return "\n\n" + template.toString( );
	}


	public static String applyAAS( int [ ] portNums , boolean withExport ) throws Exception {

		String intList = "" + portNums[ 0 ];
		for ( int i = 1 ; i < portNums.length ; i++ ) {
			intList += "o" + portNums[ i ];
		}
		String templateLabel = ( withExport ) ? "AASD" : "AAS";
		String templateName = templateLabel + intList;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		String s = "\n";
		int sum = 0;
		ArrayList < ArrayList < Integer >> b;
		for ( int j = 0 ; j < portNums.length ; j++ ) {
			b = new ArrayList < ArrayList < Integer >>( Integer.valueOf( portNums[ j ] ) );
			// make a new array of size portNums[j]
			s += applyPortTypeDecl( Integer.valueOf( portNums[ j ] ) , 0 );
			ArrayList < Integer > a;
			for ( int i = 0 ; i < Integer.valueOf( portNums[ j ] ) ; i++ ) {
				a = new ArrayList < Integer >( 2 );
				a.add( i + 1 );
				a.add( ++sum );
				b.add( a ); // add i+1 in b
			}

			template.setAttribute( "portList.{item,intNum}" , j + 1 , b );
			// add b in the portList list
		}
		if ( sum > 0 ) {
			s += applyPortTypeDecl( sum , 0 );
		}
		s += "\n\n" + template.toString( );
		return s;
	}


	private String applyConnTemplate( String templateName ) throws Exception {

		if ( templateList.addInstance( templateName ) ) {
			return AttrFiller.getTemplate( templateFile , templateName ).toString( );
		}
		return "";
	}


	public String applyCPEB( int intNum , int boolNum , boolean withExport ) throws Exception {

		String templateLabel = ( withExport ) ? "CPEBD" : "CPEB";
		String templateName = ( withExport ) ? "CPE" + intNum + "B" + boolNum + "D" : "CPE" + intNum + "B" + boolNum;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		String s = "";
		s += "\n" + applyPortTypeDecl( intNum , boolNum );

		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intNum );
		AttrFiller.addEnumerationToTemplate( template , "boolList" , intNum + 1 , intNum + boolNum );

		if ( intNum > 0 ) {
			template.setAttribute( "intList" , String.valueOf( intNum ) );
		}
		if ( boolNum > 0 ) {
			template.setAttribute( "boolList" , String.valueOf( boolNum ) );
		}
		s += "\n\n" + template.toString( );

		return s;
	}


	private String applyRDLNK( int varsNum , int hereNum ) throws Exception {

		String templateName = "RDLNK" + varsNum + "o" + hereNum;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , "RDLNK" ) ) == null ) {
			return "";
		}
		String s = "\n";
		AttrFiller.addEnumerationToTemplate( template , "hereList" , 1 , hereNum );
		// one varsNum+1 for the extra variable 'undefVar'
		AttrFiller.addEnumerationToTemplate( template , "contList" , hereNum + 1 , varsNum );

		s += applyPortTypeDecl( varsNum , 0 );
		s += applyPortTypeDecl( hereNum , 0 );
		if ( varsNum - hereNum > 0 ) {
			s += applyPortTypeDecl( varsNum - hereNum , 0 );
		}
		template.setAttribute( "hereNum" , hereNum );
		s += "\n\n" + template.toString( );
		return s;
	}


	// array with var_parts declared in the scope
	public static String applyASS( int contNum , int hereNum ) throws Exception {

		// cont: 1, .., M, here: M+1, ..., N
		String templateName = "ASS" + contNum + "o" + hereNum;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , "ASS" ) ) == null ) {
			return "";
		}
		String s = "\n";
		// AttrFiller.addEnumerationToTemplate( template , "hereList" , 1 ,
		// hereNum );
		// AttrFiller.addEnumerationToTemplate( template , "contList" , hereNum
		// + 1 , varsNum );
		AttrFiller.addEnumerationToTemplate( template , "contList" , 1 , contNum );
		AttrFiller.addEnumerationToTemplate( template , "hereList" , contNum + 1 , contNum + hereNum );
		s += applyPortTypeDecl( hereNum , 0 );
		if ( contNum > 0 ) {
			s += applyPortTypeDecl( contNum , 0 );
		}
		// s += applyPortTypeDecl( hereNum , 0 );
		// if ( varsNum - hereNum > 0 ) {
		// s += applyPortTypeDecl( varsNum - hereNum , 0 );
		// }
		template.setAttribute( "hereNum" , hereNum );
		s += "\n\n" + template.toString( );
		return s;
	}


	public static String applyRDD( int contNum , int hereNum ) throws Exception {

		// cont: 1, .., M, here: M+1, ..., N
		String templateName = "RDD" + contNum + "o" + hereNum;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , "RDD" ) ) == null ) {
			return "";
		}
		String s = "\n";
		AttrFiller.addEnumerationToTemplate( template , "contList" , 1 , contNum );
		AttrFiller.addEnumerationToTemplate( template , "hereList" , contNum + 1 , contNum + hereNum );
		s += applyPortTypeDecl( hereNum , 0 );
		if ( contNum > 0 ) {
			s += applyPortTypeDecl( contNum , 0 );
		}
		template.setAttribute( "hereNum" , hereNum );
		s += "\n\n" + template.toString( );
		return s;
	}


	public String applyBRDCAST( int intN , int brdN , boolean withExport ) throws Exception {

		String templateLabel = ( withExport ) ? "BRDCASTD" : "BRDCAST";
		String templateName = templateLabel + intN + "o" + brdN;
		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}

		for ( int i = 0 ; i < intN ; i++ ) {
			if ( i < brdN ) {
				template.setAttribute( "brdList" , i + 1 );
			} else {
				template.setAttribute( "intList" , i + 1 );
			}
		}
		return template.toString( );
	}


	public HashMap < String , ArrayList > applyEMPTYACT( Element elem , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );
		String compName = "empty_" + ( templateList.getSize( ) + 1 );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "EMPTYACT" );
		template.setAttribute( "id" , ( templateList.getSize( ) + 1 ) );
		MapUtils.addToBIPCode( ret , template.toString( ) );

		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );
		return ret;
	}


	public static String applyEMPTYHANDLER( String type ) throws Exception {

		String templateLabel = "";
		String templateName = "";
		if ( type.equals( "fh" ) ) {
			templateLabel = "EMPTYFH";
			templateName = type + "_empty";
		} else if ( type.equals( "eh" ) ) {
			templateLabel = "EMPTYEH";
			templateName = type + "_empty";
		} else if ( type.equals( "assign" ) ) {
			templateLabel = "EMPTYASSIGN";
			templateName = "empty_assign";
		} else {
			templateLabel = "EMPTY";
			templateName = "empty";
		}

		StringTemplate template;
		if ( ( template = prepareTemplate( templateName , templateLabel ) ) == null ) {
			return "";
		}
		return template.toString( );
	}


	private String applyRCV( boolean withExport ) throws Exception {

		String templateLabel = ( withExport ) ? "RCVD" : "RCV";
		StringTemplate template;
		if ( ( template = prepareTemplate( templateLabel , templateLabel ) ) == null ) {
			return "";
		}
		if ( withExport ) {
			template.setAttribute( "export" , "D" );
		}
		return template.toString( );
	}


	public static String applyANY( int intN , boolean withExport ) {

		if ( intN <= 0 ) {
			return "";
		}
		String templateLabel = ( withExport ) ? "ANYD" : "ANY";
		StringTemplate template;
		if ( ( template = prepareTemplate( templateLabel + intN , templateLabel ) ) == null ) {
			return "";
		}
		Set < Integer > p = new HashSet < Integer >( intN );
		for ( int i = 0 ; i < intN ; i++ ) {
			template.setAttribute( "intList" , i + 1 );
			p.add( i + 1 );
		}

		return template.toString( );
	}


	private String applyCHKRC1( int numOfCSflags ) throws Exception {

		if ( !templateList.addInstance( "CHKRC1" + numOfCSflags ) ) {
			return "";
		}
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC1" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , numOfCSflags + 1 );
		// +1 for the pl_pt_op_me

		String s = "";
		s += applyPortTypeDecl( 0 , numOfCSflags + 1 );
		s += applyPortTypeDecl( 1 , 0 );
		s += "\n\n" + template.toString( );
		return s;

	}


	private String applyCHKRC3( ) throws Exception {

		if ( !templateList.addInstance( "CHKRC3" ) ) { // +numOfCSflags
			return "";
		}
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC3" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , 0 );

		String s = "";
		s += applyPortTypeDecl( 0 , 0 ); // applyPortTypeDecl(0, numOfCSflags +
											// 1)
		s += applyPortTypeDecl( 1 , 0 );
		s += "\n\n" + template.toString( );
		return s;

	}


	private String applyCHKCV( int numOfCSflags ) throws Exception {

		if ( !templateList.addInstance( "CHKCV" + numOfCSflags ) ) {
			return "";
		}
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKCV" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , numOfCSflags );
		template.setAttribute( "extra" , numOfCSflags + 1 );
		template.setAttribute( "extra" , numOfCSflags + 2 );
		String s = "";
		s += applyPortTypeDecl( 0 , numOfCSflags );
		s += applyPortTypeDecl( 0 , numOfCSflags + 2 );
		s += "\n\n" + template.toString( );

		return s;
	}


	private String applyCHKRC4( int numOfCSflags ) throws Exception {

		if ( !templateList.addInstance( "CHKRC4" + ( numOfCSflags ) ) ) {
			return "";
		}
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC4" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , numOfCSflags );
		template.setAttribute( "extra" , numOfCSflags + 1 );
		template.setAttribute( "extra" , numOfCSflags + 2 );
		String s = "";
		s += applyPortTypeDecl( 0 , numOfCSflags );
		s += applyPortTypeDecl( 0 , numOfCSflags + 2 );
		s += "\n\n" + template.toString( );

		return s;
	}


	private String applyCHKCVD( int numOfCS ) throws Exception {

		String s = "";
		if ( templateList.addInstance( "CHKCVD" + numOfCS ) ) {
			StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKCVD" );
			int count = 0;
			for ( int i = 0 ; i < numOfCS ; i++ ) {
				template.setAttribute( "intList" , ++count );
			}
			template.setAttribute( "extra" , ++count );
			template.setAttribute( "extra" , ++count );
			s += "\n\n" + template.toString( );
		}
		return s;
	}


	private String applyCHKRC1D( ) throws Exception {

		String s = "";
		if ( templateList.addInstance( "CHKRC1D" ) ) {
			StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC1D" );
			s += "\n\n" + template.toString( );
		}
		return s;
	}


	private String applyCHKRPLName( int csHNum , int csENum , int meHNum , boolean withExport , boolean withCS ) throws Exception {

		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRPLName" );
		template.setAttribute( "csHNum" , csHNum );
		template.setAttribute( "csENum" , csENum );
		template.setAttribute( "meHNum" , meHNum );
		if ( withCS ) {
			template.setAttribute( "withCS" , 1 );
		}
		if ( withExport ) {
			template.setAttribute( "withExport" , "D" );
		}
		return template.toString( );
	}


	private String applyCHKRPL( int csHNum , int csENum , int meHNum , boolean withExport , boolean withCS , String templateName , int boolNumInDHS )
			throws Exception {

		String s = "";
		if ( templateList.addInstance( templateName ) ) {
			StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRPL" );
			// ima,d
			AttrFiller.addEnumerationToTemplate( template , "csHList" , 1 , csHNum );
			AttrFiller.addEnumerationToTemplate( template , "csEList" , csHNum + 1 , csHNum + csENum ); // ima
			AttrFiller.addEnumerationToTemplate( template , "fltList" , csHNum + csENum + 1 , csHNum + csENum + 2 );// ima

			AttrFiller.addEnumerationToTemplate( template , "meH" , csHNum + 1 , csHNum + meHNum ); // d

			if ( withCS ) {
				template.setAttribute( "withCS" , 1 );
			}
			template.setAttribute( "csHNum" , csHNum );
			template.setAttribute( "csENum" , csENum );
			template.setAttribute( "meHNum" , meHNum );
			template.setAttribute( "connName" , templateName );
			template.setAttribute( "boolNumInDHS" , boolNumInDHS );
			if ( withExport ) {
				template.setAttribute( "withExport" , "D" );
			}
			s += "\n" + applyPortTypeDecl( 0 , boolNumInDHS ); // for DHS
			s += "\n" + applyPortTypeDecl( csENum , 2 ); // for xpr
			s += "\n\n" + template.toString( );
		}
		return s;
	}


	private String applyCHKRC5Name( int csHNum , int csENum , int cfHNum , int meHNum , int plHNum , boolean withExport , boolean withCS )
			throws Exception {

		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC5Name" );
		template.setAttribute( "csHNum" , csHNum );
		template.setAttribute( "csENum" , csENum );
		template.setAttribute( "cfHNum" , cfHNum );
		template.setAttribute( "plHNum" , plHNum );
		template.setAttribute( "meHNum" , meHNum );
		if ( withCS ) {
			template.setAttribute( "withCS" , "1" );
		} else {
			template.setAttribute( "withCS" , "0" );
		}
		if ( withExport ) {
			template.setAttribute( "withExport" , "D" );
		}
		return template.toString( );
	}


	private String applyCHKRC5( int csHNum , int csENum , int cfHNum , int cfENum , int meHNum , int plHNum , boolean withExport , boolean withCS ,
			String templateName , int boolNumInDHS ) throws Exception {

		String s = "";
		if ( templateList.addInstance( templateName ) ) {
			StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC5" );
			// ima,d
			AttrFiller.addEnumerationToTemplate( template , "csHList" , 1 , csHNum ); // correlation
																						// sets
																						// here
			AttrFiller.addEnumerationToTemplate( template , "csEList" , csHNum + 1 , csHNum + csENum ); // correlation
																										// sets
																										// higher
			AttrFiller.addEnumerationToTemplate( template , "fltList" , csHNum + csENum + 1 , csHNum + csENum + 3 );// ima

			AttrFiller.addEnumerationToTemplate( template , "cfHList" , csHNum + 1 , csHNum + cfHNum ); // d
			AttrFiller.addEnumerationToTemplate( template , "plH" , csHNum + cfHNum + 1 , csHNum + cfHNum + plHNum ); // d
			AttrFiller.addEnumerationToTemplate( template , "meH" , csHNum + cfHNum + plHNum + 1 , csHNum + cfHNum + plHNum + meHNum ); // d

			template.setAttribute( "withCS" , withCS );
			template.setAttribute( "csHNum" , csHNum );
			template.setAttribute( "csENum" , csENum );
			template.setAttribute( "cfHNum" , cfHNum );
			template.setAttribute( "plHNum" , plHNum );
			template.setAttribute( "meHNum" , meHNum );
			template.setAttribute( "connName" , templateName );
			template.setAttribute( "boolNumInDHS" , boolNumInDHS );

			if ( withExport ) {
				template.setAttribute( "withExport" , "D" );
			}
			s += "\n" + applyPortTypeDecl( 0 , boolNumInDHS ); // for DHS
			s += "\n" + applyPortTypeDecl( csENum , 3 ); // for xpr
			s += "\n\n" + template.toString( );
		}
		return s;
	}


	private String applyWRTMSG( int varHNum , int varENum , int csHNum , int csENum ) throws Exception {

		String s = "";
		String templateName = "WRTMSG" + varHNum + "o" + varENum + "o" + csHNum + "o" + csENum;
		if ( templateList.addInstance( templateName ) ) {
			StringTemplate template = AttrFiller.getTemplate( templateFile , templateName );
			AttrFiller.addEnumerationToTemplate( template , "varHList" , 1 , varHNum );
			AttrFiller.addEnumerationToTemplate( template , "csHList" , varHNum + 1 , varHNum + csHNum );
			AttrFiller.addEnumerationToTemplate( template , "varEList" , varHNum + csHNum + 1 , varHNum + csHNum + varHNum );
			AttrFiller.addEnumerationToTemplate( template , "csEList" , varHNum + csHNum + varHNum + 1 , varHNum + csHNum + varHNum + varENum );

			template.setAttribute( "varHNum" , varHNum );
			template.setAttribute( "varENum" , varENum );
			template.setAttribute( "csHNum" , csHNum );
			template.setAttribute( "csENum" , csENum );
			if ( csENum + csHNum == 0 ) {
				template.setAttribute( "withExport" , "D" );
			}
			s += template.toString( );
		}
		return s;
	}


	private String applyCP2EB( int intNum , int boolNum ) throws Exception {

		String s = "";
		String templateName = "CP2E" + intNum + "B" + boolNum;
		if ( templateList.addInstance( templateName ) ) {
			s += "\n" + applyPortTypeDecl( intNum , boolNum );

			StringTemplate template = AttrFiller.getTemplate( templateFile , "CP2EB" );

			AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intNum );
			AttrFiller.addEnumerationToTemplate( template , "boolList" , intNum + 1 , intNum + boolNum );
			if ( intNum > 0 ) {
				template.setAttribute( "intList" , String.valueOf( intNum ) );
			}
			if ( boolNum > 0 ) {
				template.setAttribute( "boolList" , String.valueOf( boolNum ) );
			}

			s += "\n\n" + template.toString( );

		}
		return s;
	}


	private String applyCHKRC2( int numOfCSflags ) throws Exception {

		String s = "";
		if ( templateList.addInstance( "CHKRC2" + numOfCSflags ) ) {
			StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC2" );
			s += applyPortTypeDecl( 0 , numOfCSflags );
			AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , numOfCSflags );
			s += "\n\n" + template.toString( );
		}
		return s;
	}


	private String applyCHKRC2D( ) throws Exception {

		if ( !templateList.addInstance( "CHKRC2D" ) ) {
			return "";
		}
		return AttrFiller.getTemplate( templateFile , "CHKRC2D" ).toString( );
	}


	private String applyCHKMR( boolean withExport ) throws Exception {

		String tmplName = ( withExport ) ? "CHKMRD" : "CHKMR";
		String s = "";
		if ( templateList.addInstance( tmplName ) ) {
			s += applyPortTypeDecl( 0 , 1 );
			s += "\n" + AttrFiller.getTemplate( templateFile , tmplName ).toString( );
		}
		return s;
	}


	private String applyRCV( ) throws Exception {

		if ( !templateList.addInstance( "RCV" ) ) {
			return "";
		}
		return AttrFiller.getTemplate( templateFile , "RCV" ).toString( );
	}


	private String applySFH( int N ) throws Exception {

		// SFH is a connector type for start_handle interaction
		String tmplName = "SFH" + N;
		String s = "";
		if ( !templateList.addInstance( tmplName ) ) {
			return s;
		}
		StringTemplate template = AttrFiller.getTemplate( templateFile , "SFH" );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , N );
		// s += applyPortTypeDecl(2, 0);
		s += "\n" + template.toString( );
		return s;
	}


	private String applyCHKRC2XD( int intN ) throws Exception {

		if ( !templateList.addInstance( "CHKRC2XD" + intN ) ) {
			return "";
		}
		String s = "\n";
		s += applyPortTypeDecl( 0 , 1 );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC2XD" );

		AttrFiller.addEnumAndPWsetToTemplate( template , "intList" , 1 , intN );

		s += "\n\n" + template.toString( );
		return s;
	}


	private String applyCHKRC2X( int intN ) throws Exception {

		if ( !templateList.addInstance( "CHKRC2X" + intN ) ) {
			return "";
		}
		String s = "\n";
		s += applyPortTypeDecl( 0 , 1 );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRC2X" );
		AttrFiller.addEnumAndPWsetToTemplate( template , "intList" , 1 , intN );

		s += "\n\n" + template.toString( );
		return s;
	}


	private String applyCHKRCD( int intN ) throws Exception {

		if ( !templateList.addInstance( "CHKRC" + intN + "D" ) ) {
			return "";
		}
		String s = "\n";
		s += applyPortTypeDecl( 0 , 1 );
		StringTemplate template = AttrFiller.getTemplate( templateFile , "CHKRCD" );
		AttrFiller.addEnumAndPWsetToTemplate( template , "intList" , 1 , intN );

		s += template.toString( );
		return s;
	}


	// UNUSED
	private HashMap < String , ArrayList > applyBOOLEVAL( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		MapUtils.addToMapEntry( ret , MapKeys.TAG , "booleval" );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "BOOLEVAL" );
		String templateNum = String.valueOf( templateList.getSize( ) + 1 );
		String compName = "booleval_" + templateNum;
		template.setAttribute( "id" , templateNum );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );
		// TODO: Expression is not processed correctly yet
		ExpressionParsingUtils.processExpression( el , "condition" , stack , ret , template , "expression" , "true" );

		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;
	}


	private HashMap < String , ArrayList > applyEVAL( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ) throws Exception {

		HashMap < String , ArrayList > ret = new HashMap < String , ArrayList >( );

		StringTemplate template = AttrFiller.getTemplate( templateFile , "EVAL" );
		String templateNum = String.valueOf( templateList.getSize( ) + 1 );
		String compName = "eval_" + templateNum;
		template.setAttribute( "id" , templateNum );
		templateList.addInstance( compName );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , "MA" , -1 );
		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , compName , -1 );

		// create an arrayList of HashMaps
		HashMap < String , Object > map = new HashMap < String , Object >( );
		// for each input create a readPort
		// a pre and a post state
		int count = 0;
		if ( false ) {
			map.put( "index" , ++count );
			map.put( "preState" , count );
			map.put( "postState" , count + 1 );
			int partsCount = 0;
			map.put( "partsCount" , partsCount );
		}

		ExpressionParsingUtils.processExpression( el , "condition" , stack , ret , template , "expression" , "true" );

		HashMap < String , ArrayList > vars = stack.peek( );
		HashMap templatePortsMap = new HashMap( );
		exportPorts( vars , templatePortsMap , template , ret , false );
		MapUtils.addToBIPCode( ret , "\n\n" + template.toString( ) );
		return ret;

	}

}
