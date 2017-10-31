package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

public class Assign extends CompoundComponent {

	public ArrayList < ArrayList < String >> expWPorts;


	public Assign() {

		super( );
		expWPorts = new ArrayList( );
	}


	@Override
	public String getStaticTemplateName( ) {

		return "ASSIGN";
	}


	@SuppressWarnings({ "rawtypes" , "unchecked" })
	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret , Element element ,
			TemplateList templateList ) {

		super.parseElement( stack , ret , element , templateList );

		// ///////////////////////////////////////////////

		HashMap < String , ArrayList > vars = stack.peek( );

		HashMap templatePortsMap = new HashMap( );
		TemplateMaker.manageFaultsList( vars , templatePortsMap , template , ret );

		// 1: declare map entries for ports' transformation
		ArrayList < String > entries = new ArrayList < String >( Arrays.asList( new String [ ] { MapKeys.READ_PORTS } ) );
		// 2: initialize reusable info by adding entries to vars
		TemplateMaker.initializePortsTransform( vars , entries );
		// 3: transform ports
		try {
			TemplateMaker.transformMapEntryPorts( vars , templatePortsMap , template , ret , entries , false );
		} catch ( Exception e1 ) {
			// TODO Auto-generated catch block
			e1.printStackTrace( );
		}
		// 4: do custom jobs depending on the activity under translation PAST
		// ENTRIES
		// 5: erase temporary map entries
		TemplateMaker.finalizePortsTransform( vars );
		// manageReadWritePorts: END

		/* WritePort */
		ArrayList components;
		HashMap scopes_to_ports = new HashMap( );
		if ( ( components = vars.get( MapKeys.WRITE_PORTS ) ) != null ) {
			// place leftmost ports with variables in higher scopes
			for ( int comp_idx = 0 ; comp_idx < components.size( ) ; comp_idx++ ) {
				@SuppressWarnings("unchecked")
				ArrayList < RwPort > writePorts = ( ArrayList < RwPort > ) components.get( comp_idx );
				if ( writePorts == null ) {
					continue;
				}
				for ( RwPort port : writePorts ) {
					String variable = port.getVariable( );
					// find (the index) of the scope where variable is declared
					int scopeIdx = MapUtils.getScopeOfVariable( stack , variable );
					if ( !scopes_to_ports.containsKey( scopeIdx ) ) {
						scopes_to_ports.put( scopeIdx , new ArrayList( ) );
					}
					// add comp_idx (i.e. port's index) to the scope's list of
					// the scope
					( ( ArrayList ) scopes_to_ports.get( scopeIdx ) ).add( comp_idx );
					break;

				}
			}

			// 1. go through scopes low to high : if there is a variable, add
			// its parts
			// create a sorted list of scope indices

			ArrayList < Integer > scope_idxs = new ArrayList < Integer >( scopes_to_ports.keySet( ) );
			Collections.sort( scope_idxs );

			RwPort toExport = new RwPort( this.componentName , "" );
			int [ ] partsPerWR = new int [ components.size( ) ];

			int comp_idx = 0;
			// from higher to lower scope
			for ( Integer scope_idx : scope_idxs ) {
				ArrayList < String > scope_vars = new ArrayList < String >( );

				// for each WR port that writes in scope
				for ( Integer curr : ( ArrayList < Integer > ) scopes_to_ports.get( scope_idx ) ) {

					RwPort port = ( RwPort ) ( ( ArrayList ) components.get( curr ) ).get( 0 );
					ArrayList parts = port.parts( );

					toExport.parts( ).addAll( parts );
					scope_vars.addAll( parts );
					/* add to the array for the AASD template */
					partsPerWR[ comp_idx++ ] = parts.size( );

					expWPorts.add( port.toArray4Template( "expWPorts" ) );
				}
				toExport.getPartsInScope( ).add( scope_vars );

			}
			/* array for the AASD template */
			try {
				MapUtils.addToBIPCode( ret , TemplateMaker.applyAAS( partsPerWR , true ) );
				MapUtils.addToMapEntry( ret , MapKeys.WRITE_PORTS , toExport );
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}
		}

		if ( count > 1 ) {
			MapUtils.addToBIPCode( ret , TemplateMaker.applyRDV( count , false ) );
			MapUtils.addToBIPCode( ret , TemplateMaker.applyANY( count , false ) );
			MapUtils.addToBIPCode( ret , TemplateMaker.applyRDV( count , true ) );
			MapUtils.addToBIPCode( ret , TemplateMaker.applyANY( count , true ) );
		}
		MapUtils.addToBIPCode( ret , TemplateMaker.applyRDV( 2 , false ) );
		MapUtils.addToBIPCode( ret , TemplateMaker.applyANY( 2 , false ) );
		MapUtils.addToBIPCode( ret , TemplateMaker.applyRDV( 2 , true ) );
		MapUtils.addToBIPCode( ret , TemplateMaker.applyANY( 2 , true ) );

		// add the empty_assign coordinator
		String empty_assign = TemplateMaker.applyEmptyTemplate( "empty_assign" , "EMPTYASSIGN" );
		MapUtils.addToBIPCode( ret , empty_assign );

	}


	@Override
	protected void prepareTemplate( String templateFile , TemplateList templateList ) {

		super.prepareTemplate( templateFile , templateList );
		// 1.
		for ( ArrayList < String > toTemplate : expWPorts ) {
			AttrFiller.addToTemplate( toTemplate , template , "expWPorts" );
		}
		// 2.
		if ( count == 1 ) {
			template.setAttribute(  "singleCopy" , 1);
		}

	}


	@Override
	public String getRole( ) {

		return "MA";
	}

}
