package auxiliary;

import java.io.File;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bpelUtils.BPELFile;
import bpelUtils.NodeName;

public class TransformInput {

	private static HashSet < String > activityNames = new HashSet < String >( );


	private static void nameElements( String filepath ) throws Exception {

		BPELFile bpelFile = new BPELFile( filepath );
		Document oldbpelDoc = bpelFile.getDoc( );
		if ( oldbpelDoc == null ) {
			throw new IllegalArgumentException( "Null bpel File" );
		}

		DocumentBuilder db = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( );
		Document bpelDoc = db.newDocument( );
		bpelDoc.appendChild( bpelDoc.importNode( oldbpelDoc.getDocumentElement( ) , true ) );
		Element root = bpelDoc.getDocumentElement( );
		// get all children to be visited (only those in NodeNames)
		// ArrayList < Element > al = bpelFile.getBPELChildren( root );

		visitElement( root );

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance( );
		Transformer transformer = transformerFactory.newTransformer( );
		DOMSource source = new DOMSource( root );
		StreamResult result = new StreamResult( new File( "thisisit" ) );
		transformer.transform( source , result );

	}


	private static String getNewName( String activity ) {

		int i = activityNames.size( ) + 1;
		String name = activity + "_" + i;
		while ( activityNames.contains( name ) ) {
			i++;
			name = activity + "_" + i;
		}

		return name;
	}


	private static void visitElement( Element el ) {

		// name parent
		nameElement( el );
		// visit children
		NodeList nlist = el.getChildNodes( );
		for ( int i =0 ; i< nlist.getLength( ); i++ ) {
			Node child = nlist.item( i );
			if ( child instanceof Element ) {
				visitElement( (Element) child );
			}
		}
		return;
	}


	private static void nameElement( Element el ) {

		String nodeName = el.getTagName( ).toLowerCase( );
		if (! ( nodeName.equals( NodeName.getNodeNameAsString(NodeName.EMPTY) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.INVOKE) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.RECEIVE ))
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.REPLY) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.COPY) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.RETHROW) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.COMPENSATE) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.COMPENSATE_SCOPE) )
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.THROW) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.TERMINATE) ) 
				|| nodeName.equals( NodeName.getNodeNameAsString(NodeName.EXIT) ) ) )
			return;

		if ( el.getAttribute( "name" ).equals( "" ) || activityNames.contains( el.getAttribute( "name" ) ) ) {
			String newName = getNewName( el.getNodeName( ) );
			activityNames.add( newName );
			el.setAttribute( "name" , newName );
		}
	}


//	public static void main( String [ ] args ) throws Exception {
//
//		nameElements( "bpelHome/bpelPrograms/Travel_58/Travel.bpel" );
//	}

}
