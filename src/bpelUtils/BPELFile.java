package bpelUtils;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BPELFile extends XMLFile {

	QName BPELnamespace;


	public BPELFile( String path ) {
		super( path );
	}

	public ArrayList < Element > getBPELChildren( Element el ) {

		ArrayList < Element > children = new ArrayList < Element >(el.getChildNodes( ).getLength( ));
		
		for ( int i = 0 ; i < el.getChildNodes( ).getLength( ) ; i++ ) {
			Node n = el.getChildNodes( ).item( i );
			if ( n.getNodeType( ) == Node.ELEMENT_NODE && NodeName.getNodeName( ( ( Element ) n ).getTagName( ) ) != NodeName.UNKNOWN ) {
				// System.out.println(" in Children:  "+NodeName.getNodeName(el.getTagName()));
				children.add( (Element) n ) ;
			}
		}
		return children;
	}
	
	
	////GET, SET///

	public QName getBPELnamespace( ) {
		return BPELnamespace;
	}


	public void setBPELnamespace( QName bPELnamespace ) {

		BPELnamespace = bPELnamespace;
	}
}
