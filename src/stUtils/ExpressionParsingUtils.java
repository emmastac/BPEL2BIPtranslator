package stUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import translator.TemplateMaker;
import bpelUtils.XMLFile;
import stUtils.MapUtils;

public class ExpressionParsingUtils {

	/**
	 * Extracts and parses the the expression of some element.
	 * 
	 * @param expressionTagName
	 *            is the name of the expression's XMLNode
	 */
	public static ArrayList < String >  processExpression( Element el , String expression , ArrayDeque < HashMap < String , ArrayList >> stack ,
			HashMap < String , ArrayList > ret , StringTemplate template , String templateAttribute , String defaultValue ) throws Exception {

//		ArrayList < Element > elems = XMLFile.getChildrenByTagLocalName( el , expressionTagName );
//		String expression = "";
//		if ( elems.size( ) > 0 ) {
//			Element expressionElement = elems.get( 0 );
//			expression = expressionElement.getTextContent( );
//		} else if ( el.hasAttribute( expressionTagName ) ) {
//			expression = el.getAttribute( expressionTagName );
//		} else {
//			expression = defaultValue;
//		}
		if ( templateAttribute != null ) {
			template.setAttribute( "expression" , expression );
		}

		/* TODO: parse condition */
		ArrayList < String > parts = parseExpression( expression , stack );

		if ( ret == null ) {
			return parts;
		}
		if ( !ret.containsKey( MapKeys.FAULTS_LIST ) ) {
			ret.put( MapKeys.FAULTS_LIST , new ArrayList( 3 ) );
		}
		ret.get( MapKeys.FAULTS_LIST ).add( "invalidExpressionValue" );
		ret.get( MapKeys.FAULTS_LIST ).add( "subLanguageExecutionFault" );
		if ( parts.size( ) > 0 ) {
			ret.get( MapKeys.FAULTS_LIST ).add( "uninitializedVariable" );
		}
		// get the input variables
		// create a C function in .hpp and .cpp,
		// add expression signature
		// template.setAttribute("faults", "default");
		return parts;
	}


	private static ArrayList < String > parseExpression( String expression_text , ArrayDeque < HashMap < String , ArrayList >> stack ) {

		// String[] variables = expression_text.split("[^a-zA-Z_]+");
		ArrayList < String > parts = new ArrayList < String >( );
		System.out.println( "Expression :" + expression_text );
		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			HashMap < String , ArrayList < String >> variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
					MapKeys.VARIABLES );
			parseExpressionPerStackEntry( variables, expression_text, parts, stack);
			HashMap < String , ArrayList < String >> partnerLinks = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
					MapKeys.PARTNER_LINKS );
			parseExpressionPerStackEntry( partnerLinks, expression_text, parts, stack);
		}

		return parts;
	}
	
	private static void parseExpressionPerStackEntry(HashMap < String , ArrayList < String >> variables, String expression_text, ArrayList < String > parts, ArrayDeque < HashMap < String , ArrayList >> stack){
		

		if ( variables == null ) {
			return;
		}
		for ( String varName : variables.keySet( ) ) {
			String regex = "([^a-zA-Z_]|\\A)" + varName + "([^a-zA-Z_]|\\z)";
			Pattern pattern = Pattern.compile( regex );
			Matcher matcher = pattern.matcher( expression_text );
			// check all occurence
			if ( !matcher.find( ) ) {
				continue;
			}
			// else this variable is found
			parts.addAll( MapUtils.getVariableParts(stack,varName) );
			//parts.addAll( variables.get( varName ) );
			System.out.println( "Var :" + variables.get( varName ) );
		}
	}
	
	
//
//
//	/**
//	 * Obsolete Function
//	 */
//	private ArrayList < String > parseLiteralValues( String compName , Element el , StringTemplate template ,
//			ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret ) throws Exception {
//
//		ArrayList < String > litValues = new ArrayList < String >( 1 );
//		Element elem = el;
//		// maybe the literal value is enclosed in a <literal> element
//		if ( XMLFile.getChildrenByTagLocalName( elem , "literal" ).size( ) > 0 ) {
//			elem = XMLFile.getChildrenByTagLocalName( elem , "literal" ).get( 0 );
//		}
//
//		String variableTag = "";
//		String variableValue = "";
//		if ( elem.getTextContent( ) != null && !elem.getTextContent( ).trim( ).equals( "" ) ) {
//
//			int i = 0;
//			if ( ( i = elem.getChildNodes( ).getLength( ) ) > 0 ) {
//				// this is the case of a literal structure, then we don't have a
//				// readVar port, but an internal assignment to variables
//				for ( int j = 0 ; j < i ; j++ ) {
//
//					Node n = elem.getChildNodes( ).item( j );
//					if ( n.getNodeType( ) == Node.ELEMENT_NODE ) {
//						if ( ( ( Element ) n ).getTextContent( ).startsWith( "[#text" ) ) {
//							continue;
//						} else {
//							variableTag = n.getNodeName( );
//							variableValue = n.getNodeName( );
//							break;
//						}
//					} else {
//						continue;
//					}
//				}
//			} else {
//				// literal is unstructured: TODO: check if this is a valid
//				// parsing
//				litValues.add( elem.getTextContent( ) );
//			}
//		}
//
//		// get the elements to be copied (messageType's parts or single
//		// messagePart)
//		if ( litValues.size( ) == 0 ) {
//			ArrayList < String > types = matchLiteralToMessageType( stack , variableTag );
//			litValues = new ArrayList < String >( types.size( ) );
//			if ( types.size( ) == 1 ) {
//				litValues.add( variableValue );
//			} else {
//				// TODO:partition variableValue to each varPart type.
//			}
//		}
//		return litValues;
//	}
//	
//	private ArrayList < String > matchLiteralToMessageType( ArrayDeque < HashMap < String , ArrayList >> stack , String varType ) throws Exception {
//
//		ArrayList < String > mParts = getMessageParts( stack , varType );
//		if ( mParts != null && mParts.size( ) > 0 ) {
//			// varType is a message type, then copy each part.
//			// TODO:
//			throw new Exception( "Literal is a message type" );
//		} else {
//			String type = getMessageOfPart( stack , varType );
//			if ( type != null ) {
//				// varType is a part, copy as a whole.
//				return new ArrayList < String >( 0 );
//			}
//		}
//
//		return new ArrayList < String >( 0 );
//	}



}
