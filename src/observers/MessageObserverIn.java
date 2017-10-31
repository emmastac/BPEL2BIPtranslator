package observers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.stringtemplate.v4.debug.AddAttributeEvent;
import org.w3c.dom.Element;

import stUtils.AttrFiller;
import stUtils.MapKeys;
import stUtils.MapUtils;
import translator.TemplateMaker;
import bipUtils.IOMsgPort;
import bpelUtils.NodeName;
import bpelUtils.XMLFile;

public class MessageObserverIn extends BIPPropertyObserver {

	private static String cfgPath = "/bip_cfg.txt";

	private HashMap < String , ArrayList < String > > req_to_resp = new HashMap < String , ArrayList < String > >( );
	private HashMap < String , ArrayList < String > > resp_to_req = new HashMap < String , ArrayList < String > >( );
	private HashMap < String , String > req_to_act = new HashMap < String , String >( );
	private HashMap < String , String > resp_to_act = new HashMap < String , String >( );

	public MessageObserverIn( String name , TemplateMaker tm ) {

		super( name, tm );
	}


	@Override
	public void post_observeActivity( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret )
			throws Exception {

		String name = el.getAttribute( "name" );
		if( name.equals( "" ) ){
			return;
		}
		if ( NodeName.getNodeName( el.getTagName( ) ).equals( NodeName.INVOKE ) && resp_to_req.containsKey( name )  ) {

				IOMsgPort ioMsgPort = ( IOMsgPort ) ret.get( MapKeys.INV_PORTS ).get( 0 );
			String ioMsgPortName = ioMsgPort.getCompName( );
			resp_to_act.put( name , ioMsgPortName );

		} else if ( ( NodeName.getNodeName( el.getTagName( ) ).equals( NodeName.RECEIVE ) || NodeName.getNodeName( el.getTagName( ) ).equals(
				NodeName.ON_MESSAGE )   ) && req_to_resp.containsKey( name )  ) {

			IOMsgPort ioMsgPort = ( IOMsgPort ) ret.get( MapKeys.IMA_PORTS ).get( 0 );
			String ioMsgPortName = ioMsgPort.getCompName( );
			req_to_act.put( name , ioMsgPortName );

		} else if ( NodeName.getNodeName( el.getTagName( ) ).equals( NodeName.PROCESS ) ) {
			applyObserver( el , stack , ret );

		}
		return;
	}


	private void applyObserver( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret )
			throws Exception {

		StringTemplate template = AttrFiller.getTemplate( "/resources/msgObserverAllSent.stg" , "OBSERVER" );
		template.setAttribute( "compName" , MapUtils.getSingleEntry( ret , MapKeys.CHILD_COMP ) );

		String childComp = "bpelProcess_msgin_obs";
		ret.get( MapKeys.CHILD_COMP ).remove( 0 );
		ret.get( MapKeys.CHILD_COMP ).add( childComp );

		HashSet < String > observedPorts = new HashSet < String >( );

		for ( String req : req_to_act.keySet( ) ) {
			String act = req_to_act.get( req );
			// sndMsgPorts.remove( act );
			ArrayList < String > actList = new ArrayList < String >( 2 );
			actList.add( act );
			actList.add( req );
			AttrFiller.addToTemplate( actList , template , "obsReq" );
			observedPorts.add( act );

			template.setAttribute( "reqId" , req );
		}

		for ( String resp : resp_to_req.keySet( ) ) {
			ArrayList < String > actList = new ArrayList < String >( resp_to_req.get( resp ).size( ) + 1 );
			String act = resp_to_act.get( resp );
			actList.add( act );
			actList.addAll( resp_to_req.get( resp ) );

			AttrFiller.addToTemplate( actList , template , "obsResp" );
			observedPorts.add( act );

		}

		for ( IOMsgPort msgPort : ( ArrayList < IOMsgPort > ) ret.get( MapKeys.SND_MESSAGE_PORTS ) ) {

			if ( observedPorts.contains( msgPort.getCompName( ) ) ) {
				continue;
			}
			ArrayList < String > toTemplate = msgPort.toArray4Template( "sndMsgPorts" );
			AttrFiller.addToTemplate( toTemplate , template , "resp" );
			ret = MapUtils.addToBIPCode( ret , tm.applyAAS( new int [ ] { 1 } , true ) );
		}

		for ( IOMsgPort msgPort : ( ArrayList < IOMsgPort > ) ret.get( MapKeys.RCV_MESSAGE_PORTS ) ) {
			if ( observedPorts.contains( msgPort.getCompName( ) ) ) {
				continue;
			}

			ArrayList < String > toTemplate = msgPort.toArray4Template( "rcvMsgPorts" );
			AttrFiller.addToTemplate( toTemplate , template , "req" );
			ret = MapUtils.addToBIPCode( ret , tm.applyCPEB( 1 , 0 , false ) );
		}

		MapUtils.addToBIPCode( ret , tm.applyBRDCAST( 2 , 1 , true ) );
		MapUtils.addToBIPCode( ret , template.toString( ) );
		return;
	}


	public boolean configure( String bpelFile , String sourceHome ) throws Exception {

		File conf = this.getConfigFile( bpelFile , sourceHome , cfgPath );
		// read conf file
		BufferedReader in = new BufferedReader( new FileReader( conf ) );

		// skip first line
		in.readLine( );
		String line = null;
		boolean found_one = false;

		while ( ( line = in.readLine( ) ) != null && !(line.equals( "" )) ) {
			// get first text in []
			String requestDirection = readRequestDirection( line );

			if ( requestDirection.equals( "in" ) ) {
				found_one = true;

				String request = readRequestDeclaration( line );
				String reqSign = createMsgActSignature( request );
				if ( !req_to_resp.containsKey( reqSign ) ) {
					req_to_resp.put( reqSign , new ArrayList < String >( ) );
				}
				// req_to_act.put( reqSign , new ArrayList < String >( ) );

				String response = null;
				int responseCounter = 0;
				while ( ! ( response = readResponseDeclaration( line , responseCounter ) ).equals( "" ) ) {
					responseCounter++;
					String respSign = createMsgActSignature( response );
					// add response to req_to_resp
					req_to_resp.get( reqSign ).add( respSign );
					// resp_to_act.put( respSign , new ArrayList < String >( )
					// );
					if ( !resp_to_req.containsKey( respSign ) ) {
						resp_to_req.put( respSign , new ArrayList < String >( ) );
					}
					resp_to_req.get( respSign ).add( reqSign );
				}
			}
		}
		// fill hash map
		return found_one;
	}

	HashMap < Character , Integer > invalidChars = new HashMap < Character , Integer >( );


	private String sanitize( String name ) {

		String post_string = name.replaceAll( ":" , "0" );
		post_string = post_string.replaceAll( "," , "_" );
		return post_string;
	}


	private String constructActivitysIMASignature( Element el , ArrayDeque < HashMap < String , ArrayList >> stack ,
			HashMap < String , ArrayList > ret ) throws XPathExpressionException {

		// get partnerLink
		String partnerLink = el.getAttribute( "partnerLink" ).trim( );
		String partnerLinkScopeName = "";
		String partnerLinkType = "";
		// find where it is declared
		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			HashMap < String , ArrayList < String >> partnerLinks = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
					MapKeys.PARTNER_LINKS );
			ArrayList < String > partnerLinkEntry;
			if ( partnerLinks != null && ( partnerLinkEntry = partnerLinks.get( partnerLink ) ) != null ) {
				// get scope's name
				partnerLinkScopeName = ( String ) MapUtils.getSingleEntry( vars , MapKeys.SCOPE_NAME );
				partnerLinkType = partnerLinkEntry.get( 0 );
				break;
			}
		}

		// get partnerLink
		String portType = el.getAttribute( "portType" ).trim( );
		String operation = el.getAttribute( "operation" ).trim( );

		// TODO
		ArrayList < Element > cs = XMLFile.getChildrenInWrapper( el , "correlationSets" , "correlationSet" );
		String csSignature = "";
		for ( Element a : cs ) {
			String csName = a.getAttribute( "name" ).trim( );
			String csScopeName = "";

			it = stack.iterator( );
			while ( it.hasNext( ) ) {
				HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
				HashMap < String , ArrayList < String >> corrSets = ( HashMap < String , ArrayList < String >> ) MapUtils.getSingleEntry( vars ,
						MapKeys.CORRELATION_SETS );
				ArrayList < String > partnerLinkEntry;
				if ( corrSets != null && corrSets.get( csName ) != null ) {
					// get scope's name
					csScopeName = ( String ) MapUtils.getSingleEntry( vars , MapKeys.SCOPE_NAME );
					csSignature += "," + csScopeName + "::" + csName;
					break;
				}
			}

		}

		String act_signature = partnerLinkScopeName + "::" + partnerLink;
		act_signature += "," + portType;
		act_signature += "," + operation;
		act_signature += csSignature;

		act_signature = sanitize( act_signature );
		return act_signature;
	}


	private String readRequestDirection( String line ) {

		return line.substring( 0 , line.indexOf( ',' ) ).trim( );
	}


	private String readRequestDeclaration( String line ) {

		int init = 0;
		init = line.indexOf( '[' , init );
		int end = line.indexOf( ']' , init );
		return line.substring( init , end + 1 );
	}


	private String readResponseDeclaration( String line , int n ) {

		int init = 0;
		init = line.indexOf( '[' , init + 1 );
		// skip n previously parsed responses
		for ( int i = 0 ; i < n ; i++ ) {
			init = line.indexOf( '[' , init + 1 );
		}
		init = line.indexOf( '[' , init + 1 );
		if ( init == -1 ) {
			return "";
		}
		int end = line.indexOf( ']' , init );
		if ( end == -1 ) {
			return "";
		}
		return line.substring( init , end + 1 );

	}


	private String createMsgActSignature( String msgDecl ) {

		// remove []
		msgDecl = msgDecl.trim( ).substring( 1 , msgDecl.length( ) - 1 );
		msgDecl = sanitize( msgDecl );
		return msgDecl;
	}


	private String createMsgActSignature2( String msgDecl ) {

		// remove []
		msgDecl = msgDecl.trim( ).substring( 1 , msgDecl.length( ) - 1 );

		String [ ] declParts = msgDecl.split( "," );

		// partnerLink
		String signature = declParts[ 0 ].trim( );
		signature += "," + declParts[ 1 ].trim( );
		signature += "," + declParts[ 2 ].trim( );
		for ( int i = 3 ; i < declParts.length ; i++ ) {
			signature += "," + declParts[ i ].trim( );
		}

		signature = sanitize( signature );
		return signature;
	}

}
