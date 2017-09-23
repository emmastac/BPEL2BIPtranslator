package translator;

import java.util.HashSet;
import java.util.List;

public class TemplateList {

	private HashSet < String > tmplInst = new HashSet < String >( );


	public boolean addInstance( String instName ) {

		if ( tmplInst.contains( instName ) ) {
			return false;
		}
		tmplInst.add( instName );
		return true;
	}


	public void addInstances( List<String> instNames ) {
		tmplInst.addAll( instNames );
	}
	
	public void rmvInstance( String instName ) {
		tmplInst.remove( instName );
	}
	
	public int getSize(){
		return tmplInst.size( );
	}

}
