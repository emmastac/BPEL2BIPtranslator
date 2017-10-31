package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import stUtils.AttrFiller;
import stUtils.MapUtils;
import translator.TemplateList;
import translator.TemplateMaker;

public abstract class BIPtemplate {

	// public String getTemplateName( ) {
	// return this.templateName;
	// }

	StringTemplate template;
	


	private boolean templateHasIt( ){
		String instanceName = this.getInstanceName(  ) ;
		
		if ( TemplateMaker.templateList.addInstance( instanceName ) == false ) {
			return true;
		}
		return false;
	}
	
	public final void toTemplate( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList> ret , Element element ,
			String templateFile , TemplateList templateList ) {

		if ( templateHasIt( ) ) {
			return;
		} else {
			try {
				template = AttrFiller.getTemplate( templateFile , this.getStaticTemplateName( ) );
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}
			parseElement( stack , ret , element, templateList );
			prepareTemplate( templateFile , templateList );
			MapUtils.addToBIPCode( ret , template.toString( ) );
		}
	
	}


	protected abstract void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack  , HashMap < String , ArrayList > ret  , Element element , TemplateList templateList  );


	protected void prepareTemplate( String templateFile , TemplateList templateList ) {

		
	}
	

	public abstract String getStaticTemplateName( );


	protected String getInstanceName() {

		return this.getStaticTemplateName( );
	}

}
