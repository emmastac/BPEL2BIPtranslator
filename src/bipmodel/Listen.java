package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

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

public class Listen extends Receive {
	
	public int numMsg;
	public boolean withExit;

	@Override
	public String getStaticTemplateName( ) {

		return "LISTN";
	}

	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret , Element element ,
			TemplateList templateList ) {

		super.parseElement( stack , ret , element , templateList );
		this.numMsg = ( TemplateMaker.needsDuplicateComponent( element ) ) ? 2 : 1;
		if ( NodeName.getNodeName( ( (Element) element.getParentNode( ) ).getTagName( ) ).equals( NodeName.PICK ) ) {
			withExit = true;
		}
	}

	@Override
	protected void prepareTemplate( String templateFile , TemplateList templateList ) {

		super.prepareTemplate( templateFile , templateList );
		
		
		template.setAttribute( "numMsg" , numMsg );
		if(withExit){
			template.setAttribute( "withExit" , true );
		}
		
	}
	
	


}
