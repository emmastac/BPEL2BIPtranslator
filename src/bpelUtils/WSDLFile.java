package bpelUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import translator.XMLDomParser;

public class WSDLFile extends XMLFile {

	private HashMap < String , ArrayList < String > > nspace2locs = new HashMap < String , ArrayList < String > >( );

	private ArrayList < String > faultNames = new ArrayList < String >( );
	// messageName -> { msgPart1 , ... }
	private HashMap < String , ArrayList < String > > messages = new HashMap < String , ArrayList < String > >( );
	// portTypeName -> { OperationName -> OperationObj }
	private HashMap < String , HashMap < String , Operation > > portTypes = new HashMap < String , HashMap < String , Operation > >( );
	// pLinkType -> { roleName -> portTypeName }
	private HashMap < String , HashMap < String , String > > partnerLinkTypes = new HashMap < String , HashMap < String , String > >( );


	public WSDLFile( String path ) {

		super( path );
		// this.targetNamespace = targetNamespace;
	}


	public void parseWSDL( ) {

		// pick the namespaceURI that is declared last in the
		// locs2nspaces map
		readNamespaces( );
		readMessages( );
		readPortTypes( );
		readPartnerLinkTypes( );

	}


	private void readNamespaces( ) {

		// ArrayList<Node> implicitImports = new ArrayList<Node>();
		Element el = doc.getDocumentElement( );
		el.normalize( );
		NamedNodeMap implicitImports = el.getAttributes( );

		for ( int i = 0 ; i < implicitImports.getLength( ) ; i++ ) {

			Attr attr = ( Attr ) implicitImports.item( i );
			String nodeName = attr.getName( );
			// if the attribute is not a namespaced file, continue
			if ( nodeName == null || !nodeName.startsWith( "xmlns" ) || attr.getNodeName( ).equals( "xmlns" ) ) {
				continue;
			}
			String localPart = nodeName.split( ":" )[ 1 ];
			// record nspace
			if ( !nspace2locs.containsKey( localPart ) ) {
				nspace2locs.put( localPart , new ArrayList < String >( ) );
			}
			String namespaceURL = XMLDomParser.transPath( attr.getValue( ) );
			nspace2locs.get( localPart ).add( namespaceURL );

			// addNameSpacedImport( namespaceURL, hm );
		}
	}


	public String getPortType( String partnerLinkType , String partnerRole ) {

		HashMap < String , HashMap < String , String > > partnerLinkTypes = this.getPartnerLinkTypes( );
		if ( !partnerLinkTypes.containsKey( partnerLinkType ) ) {
			return null;
		}
		return partnerLinkTypes.get( partnerLinkType ).get( partnerRole );
	}

	public ArrayList < String > getNamespacePaths( String namespace ) {

		return this.nspace2locs.get( namespace );
	}
	
	public HashMap < String, ArrayList < String >> getNspace2Locs( ) {

		return this.nspace2locs;
	}

	public String [ ] getOperationsFault( String portType , String operationName ) {
		if(portType.contains(":")){
			portType = portType.split( ":" )[1];
		}
		Operation operation = ( Operation ) ( ( HashMap < String , Operation > ) this.portTypes.get( portType ) ).get( operationName );
		return operation.getFault( );
	}


	public boolean getOperationsOutput( String portType , String operationName ) {
		if(portType.contains(":")){
			portType = portType.split( ":" )[1];
		}
		Operation operation = ( Operation ) ( ( HashMap < String , Operation > ) this.portTypes.get( portType ) ).get( operationName );
		return operation.isHasOutput( );
	}


	private void readMessages( ) {

		// associate each WSDL messageType->partName
		Element el = doc.getDocumentElement( );
		el.normalize( );
		NodeList messagesEl = el.getElementsByTagNameNS( "*" , "message" );

		for ( int i = 0 ; i < messagesEl.getLength( ) ; i++ ) {
			Element messageEl = ( Element ) messagesEl.item( i );
			String key = messageEl.getAttribute( "name" );

			ArrayList < Element > partsEl = XMLFile.getChildrenByTagLocalName( messageEl , "part" );
			ArrayList < String > parts = new ArrayList < String >( partsEl.size( ) );
			// for ( int j : prtI ) {
			for ( Element partEl : partsEl ) {
				parts.add( partEl.getAttribute( "name" ) );
			}
			messages.put( key , parts );
		}
	}


	private void readPortTypes( ) {

		// read Catch

		for ( Element portTypeEl : XMLFile.getChildrenByTagLocalName( doc.getDocumentElement( ) , "portType" ) ) {
			String key = portTypeEl.getAttribute( "name" );

			ArrayList < Element > operationElems = XMLFile.getChildrenByTagLocalName( portTypeEl , "operation" );
			portTypes.put( key , new HashMap < String , Operation >( operationElems.size( ) ) );

			for ( Element operation : operationElems ) {

				String operKey = operation.getAttribute( "name" );
				Operation op = new Operation( operKey );
				( ( HashMap ) portTypes.get( key ) ).put( operKey , op );

				// add any fault message
				ArrayList < Element > faultElems = XMLFile.getChildrenByTagLocalName( operation , "fault" );
				if ( faultElems.size( ) > 0 ) {
					String faultMsg = null;
					Element faultElement = faultElems.get( 0 );
					String faultName = faultElement.getAttribute( "name" );
					// add faults to faultNames
					if ( faultElement.getAttribute( "message" ).split( ":" ).length > 0 ) {
						// remove the namespace (it is probably of another file)
						faultMsg = faultElement.getAttribute( "message" ).split( ":" )[ 1 ];
						faultMsg = faultMsg;
					}
					faultName = faultName.replaceAll( ":" , "__" );
					faultMsg = faultMsg.replaceAll( ":" , "__" );
					faultNames.add( faultName + "_" + faultMsg );
					String [ ] fault = op.getFault( );
					fault[ 0 ] = faultName;
					fault[ 1 ] = faultMsg;

				}
				// add if has output message
				if ( XMLFile.getChildrenByTagLocalName( operation , "output" ).size( ) > 0 ) {
					op.setHasOutput( true );
				}
			}
		}
	}


	private void readPartnerLinkTypes( ) {

		// read Catch

		for ( Element partnerLinkType_Element : XMLFile.getChildrenByTagLocalName( doc.getDocumentElement( ) , "partnerLinkType" ) ) {
			
			HashMap < String , String > hm = new HashMap < String , String >( 2 );
			for ( Element role_Element : XMLFile.getChildrenByTagLocalName( partnerLinkType_Element , "role" ) ) {
				hm.put( role_Element.getAttribute( "name" ) , role_Element.getAttribute( "portType" ) );
			}
			partnerLinkTypes.put( partnerLinkType_Element.getAttribute( "name" ) , hm );
		}
	}


	public ArrayList < String > getFaultNames( ) {

		return faultNames;
	}


	public final HashMap < String , ArrayList < String > > getMessages( ) {

		return messages;
	}


	public HashMap < String , HashMap < String , Operation > > getPortTypes( ) {

		return portTypes;
	}


	public HashMap < String , HashMap < String , String > > getPartnerLinkTypes( ) {

		return partnerLinkTypes;
	}

}
