package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import stUtils.AttrFiller;
import translator.TemplateList;


public class PortTypeDeclaration extends BIPtemplate{
	
	public int intVar = 0;
	public int boolVar = 0;

	@Override
	public String getStaticTemplateName( ) {
		return "portTypeDecl";
	}
	
	@Override
	protected String getInstanceName( ) {
		return "e" + intVar + "b" + boolVar + "port";
	}

	@Override
	protected void prepareTemplate( String  templateFile , TemplateList templateList ) {
		super.prepareTemplate( templateFile , templateList );
		AttrFiller.addEnumerationToTemplate( template , "intList" , 1 , intVar );
		AttrFiller.addEnumerationToTemplate( template , "boolList" , intVar + 1 , intVar + boolVar );
		template.setAttribute( "boolNum" , boolVar );
		
	}

	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret , Element element ,
			TemplateList templateList ) {
		
	}
	
	

}
