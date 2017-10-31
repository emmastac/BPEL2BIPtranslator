package bipUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import stUtils.MapKeys;
import stUtils.MapUtils;
import translator.TemplateMaker;
import bpelUtils.XMLFile;

public class RwPort extends Port {

	private int contNums;
	private int hereNums; // number of variables written in the current scope
								// (set only before template application)
	private ArrayList<ArrayList<String>> scopeVarParts = new ArrayList<ArrayList<String>>(); // in writePorts
	private HashMap<String,Integer> varNumsPerScope = new HashMap<String,Integer>();
	private ArrayList<String> varParts = new ArrayList<String>();

	public RwPort() { }

	public RwPort(String compName, String compIndex) {
		super(compName,compIndex);
	}
	
	public RwPort clone(){
		RwPort port = new RwPort(this.compName, this.compIndex);
		port.parts().addAll(this.parts());
		port.setHereNums(this.getHereNums());
		return port;
	}

	public HashMap<String, Integer> getVarNumsPerScope() {
		return varNumsPerScope;
	}

	public ArrayList<ArrayList<String>> getPartsInScope() {
		return scopeVarParts;
	}
	
	public String getVariable(){
		return varParts.get( 0 ).split( "_" )[0];
	}


	@Override
	public ArrayList<String> toArray4Template(String template) {
		ArrayList<String> array = null;
		if (template.equals("intWPorts") || template.equals("intRPorts")) {
			array = new ArrayList<String>(varParts.size() + 4);
			array.add(String.valueOf( contNums ));
			array.add(String.valueOf( hereNums ));
			array.addAll(varParts);
		}else if(template.equals("expWPorts")|| template.equals("expRPorts")){
			array = new ArrayList<String>(varParts.size() + 3);
			array.add(String.valueOf(varParts.size()));
			array.addAll(varParts);
		}else if(template.equals("rdLnkPorts") ){
			array = new ArrayList<String>(varParts.size()+4);
			array.add(String.valueOf(varParts.size()));
			array.add(String.valueOf( hereNums ));
			array.addAll(varParts);
		}else if(template.equals("expRdLnkPorts")){
			array = new ArrayList<String>(3);
			array.add(String.valueOf(varParts.size()));
		}else if(template.equals("wrtLnkPorts")|| template.equals("expWrtLnkPorts")){
			array = new ArrayList<String>(3);
			array.addAll(varParts);
		}else{ // including rtLnkPorts
			array = new ArrayList<String>(2);
		}
		this.toArrayBasic(array);
		return array;
	}
	


	// ///////////////////////////////////////////////////////////////////

	public void applyTemplate( StringTemplate template ){
		template.setAttribute( "varsWPorts" , this.varParts.size( ) ); /* partNames */

		for ( String part :  this.varParts ) {
			template.setAttribute( "varsWPorts" , part );
		}
		
	}
	
	public void parsePort( String compName, ArrayDeque < HashMap < String , ArrayList >> stack , Element elem, HashMap<String, ArrayList> ret) throws Exception{
			this.compName = compName;
			MapUtils.addToMapEntry( ret, MapKeys.WRITE_PORTS, this );
			// if the openIMAScope is declared, then this is a port for writing a
			// message

			/* receive will either have a variable attribute or a fromParts element */
			if ( elem.hasAttribute( "variable" ) ) { // in 'receive', 'reply' or
														// 'copy'
				if ( elem.hasAttribute( "part" ) ) { // in'copy'
					this.varParts.add( elem.getAttribute( "variable" ) + "_" + elem.getAttribute( "part" ) );
				} else if ( elem.hasAttribute( "property" ) ) { // in'copy'
					this.varParts.add( elem.getAttribute( "variable" ) + "_" + elem.getAttribute( "property" ) );
				} else { // in all
					this.varParts.addAll( MapUtils.getVariableParts( stack , elem.getAttribute( "variable" ) ) );
				}
			} else if ( elem.hasAttribute( "outputVariable" ) ) { // in 'invoke'
				this.varParts.addAll( MapUtils.getVariableParts( stack , elem.getAttribute( "outputVariable" ) ) );
			} else if ( elem.hasAttribute( "partnerLink" ) ) { // in 'copy'
				String partnerLink = TemplateMaker.partnerLinkToBIP( elem.getAttribute( "partnerLink" ) );// TODO

				ArrayList < String > partnerLinkList = ( ArrayList < String > )  TemplateMaker.findInMapOfEnclosingScopes( stack , partnerLink , MapKeys.PARTNER_LINKS );

				if ( partnerLinkList == null ) {
					throw new Exception( "partner link not found" );
				}
				// String partnerRole = ( String ) partnerLinkList.get( 1 );
				this.varParts.add( partnerLink + "_pRole" );

			} else {
				ArrayList < Element > toPartElems = XMLFile.getChildrenInWrapper( elem , "fromParts" , "fromPart" );

				// bcompiler.bpelFile.getChildrenInWrapper( elem , "fromParts" ,
				// "fromPart" );
				// ArrayList < Element > toPartsElems =
				// XMLFile.getChildrenByTagLocalName( elem , "fromParts" );
				if ( toPartElems.size( ) > 0 ) {
					for ( Element toPart : toPartElems ) {
						String part = toPart.getAttribute( "toVariable" ) + "_" + toPart.getAttribute( "part" );
						this.varParts.add( part );
					}

				} else { // in 'copy'
					String variableString = elem.getTextContent( );
					if ( variableString != null ) { // only in
						int ind = variableString.indexOf( "." );
						if ( ind > 0 ) {
							String variable = variableString.replace( "$" , "" );
							String part = variable.replace( '.' , '_' );
							this.varParts.add( part );
						} else {
							String variable = variableString.replace( "$" , "" );
							this.varParts.addAll( MapUtils.getVariableParts( stack , variable ) );
						}
					}
				}
			}
			ret = MapUtils.addToBIPCode( ret , TemplateMaker.applyPortTypeDecl( this.varParts.size( ) , 0 ) ); // not
		
	}


	public ArrayList<String> parts() {
		return varParts;
	}

	public void setVarParts(ArrayList<String> varParts) {
		this.varParts = varParts;
	}

	public int getHereNums() {
		return hereNums;
	}

	public void setHereNums(int hereNums) {
		this.hereNums = hereNums;
	}
	
	public void setContNums(int contNums) {
		this.contNums = contNums;
	}
	
	


}
