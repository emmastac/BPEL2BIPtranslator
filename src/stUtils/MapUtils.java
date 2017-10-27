package stUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;

import bpelUtils.NodeName;


public class MapUtils {
	

	public static HashMap < String , ArrayList > addToBIPCode( String s , HashMap < String , ArrayList > ret ) {

		/* add the aggregated bip code at the end of the string */
		if ( s != null && !s.equals( "" )) {
			s = '\n'+s;
			if ( ret.get( MapKeys.BIP_CODE ) != null && ret.get( MapKeys.BIP_CODE ).size( ) > 0 ) {
				String tail = ( String ) ret.get( MapKeys.BIP_CODE ).remove( 0 );
				ret.get( MapKeys.BIP_CODE ).add( tail + s );
			} else {
				ret.put( MapKeys.BIP_CODE , new ArrayList < Object >( 1 ) );
				ret.get( MapKeys.BIP_CODE ).add(  s );
			}
		}
		return ret;
	}


	public static HashMap < String , ArrayList > addToCPPCode( String s , HashMap < String , ArrayList > ret ) {

		/* add the aggregated bip code at the end of the string */
		if ( s != null && !s.equals( "" )) {
			if ( ret.get( MapKeys.CPP_CODE ) != null && ret.get( MapKeys.CPP_CODE ).size( ) > 0 ) {
				String tail = ( String ) ret.get( MapKeys.CPP_CODE ).remove( 0 );
				ret.get( MapKeys.CPP_CODE ).add( tail + s );
			} else {
				ret.put( MapKeys.CPP_CODE , new ArrayList < Object >( 1 ) );
				ret.get( MapKeys.CPP_CODE ).add( s );
			}

		}
		return ret;
	}


	public static HashMap < String , ArrayList > addToHPPCode( String s , HashMap < String , ArrayList > ret ) {

		/* add the aggregated bip code at the end of the string */
		if ( s != null && !s.equals( "" )) {
			if ( ret.get( MapKeys.HPP_CODE ) != null && ret.get( MapKeys.HPP_CODE ).size( ) > 0 ) {
				String tail = ( String ) ret.get( MapKeys.HPP_CODE ).remove( 0 );
				ret.get( MapKeys.HPP_CODE ).add( tail + s );
			} else {
				ret.put( MapKeys.HPP_CODE , new ArrayList < Object >( 1 ) );
				ret.get( MapKeys.HPP_CODE ).add( s );
			}
		}
		return ret;
	}
	
	public static void addToMapEntry( HashMap < String , ArrayList > map , String entryKey , Object newEntryItem) {
		if ( !map.containsKey( entryKey ) ) {
			map.put( entryKey , new ArrayList < Object >( ) );
		}
		map.get( entryKey ).add( newEntryItem );
	}
	
	public static Object getMapSingleEntry( HashMap < String , ArrayList > map , String entryKey ) {

		ArrayList < Object > tmp;
		if ( ( tmp = ( ArrayList < Object > ) map.get( entryKey ) ) == null || tmp.size( ) == 0 ) {
			return null;
		}
		return ( Object ) tmp.get( 0 );

	}
	/**
	 * Read TAG in a hashMap (i.e. searches in one stack entry).
	 * @param vars
	 * @return
	 */
	public static NodeName readTag( HashMap < String , ArrayList > vars  ) {
		// tag -> ArrayList(ArrayList(1))
		ArrayList wrapper = ( ArrayList ) MapUtils.getMapSingleEntry( vars , MapKeys.TAG );
		NodeName tagName = null;
		if ( wrapper==null || ( tagName = (NodeName) wrapper.get( 0 ) ) == null ) {
			return null;
		}
		return tagName;

	}
	

	public static ArrayList < String > getVariableParts( ArrayDeque < HashMap < String , ArrayList >> stack , String varName ) {

		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );
			HashMap < String , ArrayList < String >> variables = ( HashMap < String , ArrayList < String >> ) MapUtils.getMapSingleEntry( vars ,
					MapKeys.VARIABLES );
			ArrayList < String > parts;
			if ( variables != null && ( parts = variables.get( varName ) ) != null ) {
				if ( parts.size( ) == 0 ) {
					parts.add( varName );
				}
				return parts;
			}
		}
		return new ArrayList < String >( 0 );
	}

	/**
	 * Applies a template which creates a component wrapping various fault
	 * handlers.
	 * 
	 * 
	 * @param fhList
	 *            the list of types of the enclosed fault handler components in
	 *            the format {faultName1, faultName2 etc} to be transformed into
	 *            faultLists:{faultList_1,faultList_2} , WHERE
	 *            faultList_i:{faultName, id_respFH, id_otherFH_1,...}
	 * @param faults
	 *            the list of fault names handled by the enclosed fault handler
	 *            components in the format {faultName1, faultName2 etc} ->
	 *            faultLists:{faultList_1,faultList_2} , WHERE
	 *            faultList_i:{faultName, id_respFH, id_otherFH_1, ..}
	 * @param expFaults
	 *            the list of fault names raised by enclosed fault handlers in
	 *            the format { faultName_i , {id_1, id_j}
	 * @param expPorts
	 *            the list of port names which are exported by the FH component
	 *            in the format {connType, port_name, child_num, expPort_type}
	 * @return the resulting string of the templates instantiated
	 */
	/*
	 * String[] fhList, String[] faults, String[][] expFaults, String[][]
	 * expPorts
	 */

	public static void addToMap( Map < String , ArrayList > map , String entry , Object obj , int pos ) {

		if ( !map.containsKey( entry ) ) {
			map.put( entry , new ArrayList( ) );
		}
		if ( pos == -1 ) {
			map.get( entry ).add( obj );
		} else {
			map.get( entry ).add( pos , obj );
		}
	}

	public static String getSuppressJoinFailure( ArrayDeque < HashMap < String , ArrayList >> stack ) {

		Iterator it = stack.iterator( );
		while ( it.hasNext( ) ) {
			HashMap < String , ArrayList > vars = ( HashMap < String , ArrayList > ) it.next( );

			String suppressJoinFailure = ( String ) MapUtils.getMapSingleEntry( vars , "suppressJoinFailure" );
			if ( suppressJoinFailure != null ) {
				return ( suppressJoinFailure.equals( "yes" ) ) ? "true" : "false";
			}
		}
		// default value for suppressJoinFailure is no
		return "false";
	}
	
	public static String getComponentIndex( HashMap < String , ArrayList > vars , StringTemplate template , Integer p ) {

		if ( template.getName( ).startsWith( "SCOPE" ) || template.getName( ).startsWith( "PROC" ) ) {
			return getScopeRole( ( ArrayList ) vars.get( MapKeys.SCOPE_ROLES ) , p );
		} else {
			return String.valueOf( p + 1 );
		}
	}
	
	public static String getScopeRole( ArrayList childrenTag , int i ) {

		return ( String ) ( ( ArrayList < String > ) childrenTag.get( i ) ).get( 0 );
	}
	

}
