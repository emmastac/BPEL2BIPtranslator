package bpelUtils;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import translator.XMLDomParser;

public class XMLFile {

	protected String targetNamespace;
	protected String path;
	protected Document doc;
	protected static XPath xpath = XPathFactory.newInstance( ).newXPath( );
	
	public XMLFile( String path ){
		this.path = path;

		XMLDomParser domParser = new XMLDomParser( this.path );
		domParser.parseXmlFile( );
		this.doc = domParser.getDom( );
		this.doc.normalize( );

		Element root = doc.getDocumentElement( );
		this.targetNamespace = root.getAttribute( "targetNamespace" );
	}


	public Document getDoc( ) {

		return doc;
	}
	
	public String getTargetNamespace( ) {

		return targetNamespace;
	}


	public void setTargetNamespace( String targetNamespace ) {

		this.targetNamespace = targetNamespace;
	}


	/**
	 * TODO:
	 * @throws XPathExpressionException 
	 */
//	public static ArrayList < Integer > getChildrenByTagLocalName( Element startElement , String tagName ) {
//
//		Element el = startElement;
//		ArrayList < Integer > al = new ArrayList < Integer >( );
//		NodeList nl = el.getChildNodes( );
//		// String tagName2 = resolveXMLTagName(tagName);
//		for ( int i = 0 ; i < nl.getLength( ) ; i++ ) {
//			if ( nl.item( i ).getNodeType( ) == Node.ELEMENT_NODE ) {
//				el = ( Element ) nl.item( i );
//				if ( el.getTagName( ).equals( tagName ) || el.getTagName( ).endsWith( ":" + tagName ) ) {
//					al.add( i );
//				}
//			}
//		}
//		return al;
//	}

	public  static ArrayList < Element > getChildrenInWrapper( Element startElement , String wrapper , String children ){
		String expression =  "./*[local-name()='"+wrapper+"']/*[local-name()='"+children+"']";
		try {
			return getNodesByXpath(  startElement ,  expression ) ;
		} catch ( XPathExpressionException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	public static ArrayList < Element >  getNodesByXpath( Element startElement , String expression ) throws XPathExpressionException {
		XPathExpression expr = xpath.compile( expression );
		// evaluate expression result on XML document
		NodeList nodes = ( NodeList ) expr.evaluate( startElement , XPathConstants.NODESET );
		ArrayList < Element > nodeList = new ArrayList<Element>(nodes.getLength( ));
		for(int i=0; i< nodes.getLength(); i++){
			nodeList.add( (Element) nodes.item( i ) );
		}
		return nodeList;
	}
	
	public NodeList getChildrenByXpath( Element startElement , String tagNameL ) throws XPathExpressionException {

		XPathExpression expr = xpath.compile( "./*[local-name() = '"+tagNameL+"']" );
		// evaluate expression result on XML document
		NodeList nodes = ( NodeList ) expr.evaluate( startElement , XPathConstants.NODESET );
		return nodes;
	}


	public static ArrayList < Element > getChildrenByTagLocalName( Element el , String tagName ) {

		ArrayList < Element > retNodeList = new ArrayList < Element >( );
		NodeList nodeList = el.getChildNodes( );
		// String tagName2 = resolveXMLTagName(tagName);
		for ( int i = 0 ; i < nodeList.getLength( ) ; i++ ) {
			if ( nodeList.item( i ).getNodeType( ) == Node.ELEMENT_NODE ) {
				el = ( Element ) nodeList.item( i );
				if ( el.getTagName( ).equals( tagName ) || el.getTagName( ).endsWith( ":" + tagName ) ) {
					retNodeList.add( el );
				}
			}
		}

		return retNodeList;
	}


	public String resolveXMLTagName( String tagName ) {

		return tagName;
	}


	public String getDOM1LocalName( Element el ) {

		String NSUri = null;
		String elName = el.getTagName( );
		if ( el.getTagName( ).contains( ":" ) ) {
			NSUri = el.getTagName( ).split( ":" )[ 0 ];
			elName = el.getTagName( ).split( ":" )[ 1 ];
		}
		return elName;
	}


	public String getNspaceCombined( String baseURI , String uri ) {

		String nspaceCombined = baseURI;
		if ( !nspaceCombined.equals( "" ) ) {
			nspaceCombined += ",";
		}
		nspaceCombined += uri;
		return nspaceCombined;
	}
	
	

}
