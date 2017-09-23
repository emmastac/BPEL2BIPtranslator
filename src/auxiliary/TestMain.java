package auxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.net.www.content.audio.x_aiff;

public class TestMain {

	public TestMain() {

		// TODO Auto-generated constructor stub
	}


	public static void main( String [ ] args ) {
		HashMap < Integer , ArrayList> map1 = new HashMap < Integer , ArrayList>();
		ArrayList<String> w = new ArrayList<String>(2);
		w.add( "m" );
		map1.put( 1 , w );
		
		HashMap < Integer , ArrayList> map2 = new HashMap < Integer , ArrayList>(map1);
		String w2 =  ((ArrayList<String>) map2.get( 1 )).get( 0 );
		w2 = "f";
		
		
		for( ArrayList<String> s : map1.values( ) ){
			for( String s1 : s ){
				System.out.print(s1 +" ");
			}
			System.out.println();
		}
		
		for( ArrayList<String> s : map2.values( ) ){
			for( String s1 : s ){
				System.out.print(s1 +" ");
			}
			System.out.println();
		}
		

	}
	
	void  testRegExp(){
		String expression_text = "inputVariable.a";
		String varName = "inputVariable";

		//String regex = "[^a-zA-Z_]|\\A '" + varName + "' [^a-zA-Z_]|\\z";
		String regex =  "([^a-zA-Z_]|\\A)"+varName+"([^a-zA-Z_]|\\z)" ;

		Pattern pattern = Pattern.compile( regex );
		Matcher matcher = pattern.matcher( expression_text );
		// check all occurance
		while ( matcher.find( ) ) {
			System.out.println( "SUCC" );
			System.out.print( "Start index: " + matcher.start( ) );
			System.out.print( " End index: " + matcher.end( ) + " " );
			System.out.println( " group: " + matcher.group( ) + " " );
		}
		System.out.println( regex );

	}
	

}
