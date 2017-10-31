package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import bipUtils.IOMsgPort;
import bipUtils.RwPort;
import bpelUtils.NodeName;
import bpelUtils.WSDLFile;
import stUtils.AttrFiller;
import stUtils.MapKeys;
import stUtils.MapUtils;
import translator.TemplateList;
import translator.TemplateMaker;

public class Receive extends Component {

	public String partnerLink;
	public String operation;
	public String messageExchange;
	public String plop;

	public boolean isRcvIO;

	IOMsgPort newIma;
	RwPort writePort;
	private boolean createInstance;


	@Override
	public String getStaticTemplateName( ) {

		return "RECEIVE";
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret , Element element ,
			TemplateList templateList ) {

		super.parseElement( stack , ret , element , templateList );
		MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "conflictingReceive" , "conflictingRequest" , "ambiguousReceive" , "invalidVariables" );

		MapUtils.addToBIPCode( ret , TemplateMaker.applyPortTypeDecl( 0 , 1 ) , TemplateMaker.applyPortTypeDecl( 1 , 0 ) );

		this.partnerLink = element.getAttribute( "partnerLink" );
		this.operation = element.getAttribute( "operation" );
		this.messageExchange = ( element.hasAttribute( "messageExchange" ) ) ? element.getAttribute( "messageExchange" ) : "";
		this.plop = this.partnerLink + "_" + this.operation;

		WSDLFile wsdl_spec = TemplateMaker.findMessageActivityWSDL( stack , element );
		String portType = TemplateMaker.getPortType( stack , element , wsdl_spec );

		// /////////////////////////////////////////////////////////////////

		this.newIma = new IOMsgPort( componentName , "" , this.operation , this.partnerLink , this.messageExchange );
		MapUtils.addToMapEntry( ret , MapKeys.IMA_PORTS , this.newIma );

		// if the receive has an output, add port also to ioma
		if ( wsdl_spec.operationHasOutput( portType , operation ) ) {
			newIma.setRcvIO( true );
			newIma.setScopeOpenIMA( stack , partnerLink , messageExchange );
		}
		newIma.setScopePartnerLink( stack , partnerLink );

		TreeMap < String , String > extractedCSs;
		try {
			extractedCSs = TemplateMaker.extractCorrSets( element );
			newIma.addIMAInfo( element , extractedCSs , template , stack , "initiateList" );
			MapUtils.addToBIPCode( ret , TemplateMaker.applyPortTypeDecl( extractedCSs.size( ) , 3 ) );

			if ( newIma.getOrderedCSs( ).size( ) > 0 ) {
				MapUtils.addToMapEntry( ret , MapKeys.FAULTS_LIST , "correlationViolation" );
			}

		} catch ( XPathExpressionException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

		newIma.addVarParts( stack , element );
		// /////////////////////////////////////////////////////////////////
		try {
			writePort = new RwPort(); 
			writePort.parsePort( this.componentName , stack , element , ret );
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

		// /////////////////////////////////////////////////////////////////

		// add in the first place of an ArrayList associated to the
		// entry 'plop'
		HashMap < String , ArrayList > conflImas = new HashMap < String , ArrayList >( 1 );
		conflImas.put( plop , new ArrayList < Object >( 1 ) );
		String [ ] conflIma = { "1" , plop };
		conflImas.get( plop ).add( Arrays.asList( conflIma ) );

		// //////////////////////////////////////////////////////////////////
		Element parentElement = ( Element ) element.getParentNode( );

		if ( element.getNodeName( ).equals( "receive" ) && element.hasAttribute( "createInstance" )
				&& element.getAttribute( "createInstance" ).equals( "yes" ) ) {
			this.createInstance = true;

		} else if ( parentElement.getNodeName( ).equals( "pick" ) && parentElement.hasAttribute( "createInstance" )
				&& parentElement.getAttribute( "createInstance" ).equals( "yes" ) ) {
			this.createInstance = true;
		}
		MapUtils.addToMapEntry( ret , MapKeys.RCV_MESSAGE_PORTS , newIma.clonePort( ) );

	}


	@Override
	protected void prepareTemplate( String templateFile , TemplateList templateList ) {

		super.prepareTemplate( templateFile , templateList );

		template.setAttribute( "plop" , partnerLink + "_" + operation );
		if ( isRcvIO ) {
			template.setAttribute( "isRcvIO" , "1" );
		}
		if ( this.createInstance ) {
			template.setAttribute( "createInstance" , 1 );
		}
		
		writePort.applyTemplate( this.template );
		
		
	}
	
	


	@Override
	public String getRole( ) {

		return "MA";
	}

}
