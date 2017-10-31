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

public class DeadlockObserver extends BIPPropertyObserver {

	private static String templateFile = "/resources/deadlockObserver.stg";

	public DeadlockObserver( String name , TemplateMaker tm ) {

		super( name, tm );
	}



	@Override
	public void post_observeActivity( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret )
			throws Exception {


		if ( NodeName.getNodeName( el.getTagName( ) ).equals( NodeName.PROCESS ) ) {
			applyObserver( el , stack , ret );

		}
		return;
	}


	private void applyObserver( Element el , ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret )
			throws Exception {

		StringTemplate template = AttrFiller.getTemplate( templateFile , "OBSERVER" );
		template.setAttribute( "compName" , MapUtils.getSingleEntry( ret , MapKeys.CHILD_COMP ) );

		String childComp = AttrFiller.getTemplate(templateFile , "compound_name" ).toString( );
		ret.get( MapKeys.CHILD_COMP ).remove( 0 );
		ret.get( MapKeys.CHILD_COMP ).add( childComp );

		HashSet < String > observedPorts = new HashSet < String >( );


		for ( IOMsgPort msgPort : ( ArrayList < IOMsgPort > ) ret.get( MapKeys.SND_MESSAGE_PORTS ) ) {

			if ( observedPorts.contains( msgPort.getCompName( ) ) ) {
				continue;
			}
			ArrayList < String > toTemplate = msgPort.toArray4Template( "sndMsgPorts" );
			AttrFiller.addToTemplate( toTemplate , template , "obsSnd" );
			ret = MapUtils.addToBIPCode( ret , tm.applyAAS( new int [ ] { 1 } , true ) );
		}

		for ( IOMsgPort msgPort : ( ArrayList < IOMsgPort > ) ret.get( MapKeys.RCV_MESSAGE_PORTS ) ) {
			if ( observedPorts.contains( msgPort.getCompName( ) ) ) {
				continue;
			}

			ArrayList < String > toTemplate = msgPort.toArray4Template( "rcvMsgPorts" );
			AttrFiller.addToTemplate( toTemplate , template , "obsRcv" );
			ret = MapUtils.addToBIPCode( ret , tm.applyCPEB( 1 , 0 , false ) );
		}

		MapUtils.addToBIPCode( ret , template.toString( ) );
		return;
	}







}
