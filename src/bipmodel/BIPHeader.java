package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import stUtils.AttrFiller;
import translator.TemplateList;


public class BIPHeader extends BIPtemplate {
	
	private String packageName;

	
	public void setPackageName( String packageName  ) {
		this.packageName = packageName;
	}
	
	
	
	@Override
	public String getStaticTemplateName(  ){
		return "header_BIP";
	}

	@Override
	protected void prepareTemplate( String templateFile , TemplateList templateList  ) {
		super.prepareTemplate( templateFile , templateList );
		template.setAttribute( "name" , packageName );
		
		templateList.addInstances( Arrays.asList( new String [ ] { "e0b0port" , "e1b0port" , "e2b0port" , "fh_ctrl" , "scope_cntr" , "BRDCAST2" , "BRDCASTD2" ,
				"SingletonD" } ) );
		
	}



	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret , Element element ,
			TemplateList templateList ) {
		this.packageName = "check" ;

		
	}


}
