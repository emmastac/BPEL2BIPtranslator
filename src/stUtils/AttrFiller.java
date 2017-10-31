package stUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import translator.BPELCompiler;

public class AttrFiller {

	public static void addMapToTemplate( StringTemplate template , Map map , String attr ) {

		ArrayList al = new ArrayList( 1 );
		al.add( map );
		template.setAttribute( attr , al );
	}
	
	public static void addEnumerationToTemplate( StringTemplate template , String attribute , int start , int end ) {

		for ( int i = start ; i <= end ; i++ ) {
			template.setAttribute( attribute , i );
		}
	}


	public static void addEnumAndPWsetToTemplate( StringTemplate template , String attribute , int start , int end ) {

		// template = addEnumerationToTemplate(template, "intList", 1, intN);
		Set < Integer > p = new HashSet < Integer >( end - start + 1 );
		for ( int i = start ; i <= end ; i++ ) {
			template.setAttribute( attribute , i );
			p.add( i );
		}
		// find subsets
		Set < Set < Integer >> ps = powerSet( p );
		// arrange the sets in an arrayList
		ArrayList < Set < Integer >> al = new ArrayList < Set < Integer >>( ps.size( ) );
		for ( int i = end ; i > 0 ; i-- ) {
			// get all sets of size i
			for ( Set < Integer > set : ps ) {
				if ( set.size( ) == i ) {
					al.add( set );
				}
			}
		}
		template.setAttribute( "portSets" , al );
	}


	private static Set < Set < Integer >> powerSet( Set < Integer > originalSet ) {

		Set < Set < Integer >> sets = new HashSet < Set < Integer >>( );
		if ( originalSet.isEmpty( ) ) {
			sets.add( new HashSet < Integer >( ) );
			return sets;
		}
		List < Integer > list = new ArrayList < Integer >( originalSet );
		Integer head = list.get( 0 );
		Set < Integer > rest = new HashSet < Integer >( list.subList( 1 , list.size( ) ) );
		for ( Set < Integer > set : powerSet( rest ) ) {
			Set < Integer > newSet = new HashSet < Integer >( );
			newSet.add( head );
			newSet.addAll( set );
			sets.add( newSet );
			sets.add( set );
		}
		return sets;
	}


	public static void addToTemplate( ArrayList < String > list , StringTemplate template , String var ) {

		ArrayList al = new ArrayList( list.size( ) );
		al.add( list );
		template.setAttribute( var , al );
	}


	public static void addToTemplate( String [ ] arr , StringTemplate template , String var ) {

		ArrayList al = new ArrayList( 1 );
		al.add( Arrays.asList( arr ) );
		template.setAttribute( var , al );
	}


	public static StringTemplate getTemplate( String groupFile , String tmplName ) throws Exception {

		StringTemplateGroup group;

		BufferedReader br = BPELCompiler.resourceReader( groupFile );
		group = new StringTemplateGroup( br , DefaultTemplateLexer.class );
		if ( group.isDefinedInThisGroup( tmplName ) ) {

			return group.getInstanceOf( tmplName );
		} else {
			return null;
		}

	}

}
