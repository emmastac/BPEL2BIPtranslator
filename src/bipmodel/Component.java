package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import stUtils.MapKeys;
import stUtils.MapUtils;
import translator.TemplateList;
import translator.TemplateMaker;


public abstract class Component extends BIPtemplate{
	
	public String componentName;
	public int id;
	
	
	protected String getInstanceName(){
		this.id = TemplateMaker.templateList.getSize( ) + 1;
		this.componentName = getStaticTemplateName( ).toLowerCase( ) + "_" +this.id;
		return this.componentName;
	}
	
	public abstract String getRole();

	@SuppressWarnings("rawtypes")
	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack  , HashMap < String , ArrayList > ret  , Element element , TemplateList templateList  ) {
		//super.parseElement( vars , ret , element, templateList );
		MapUtils.addToMap( ret , MapKeys.SCOPE_ROLES , this.getRole( ) , -1 );
		
		//id = templateList.getSize( ) + 1;
		//componentName = getStaticTemplateName( ).toLowerCase( ) + "_" + ( templateList.getSize( ) + 1 );

		MapUtils.addToMap( ret , MapKeys.CHILD_COMP , componentName , -1 );
	}

	@Override
	protected void prepareTemplate( String templateFile , TemplateList templateList ) {
		super.prepareTemplate( templateFile , templateList );
		template.setAttribute( "id" , this.id );
		//template.setAttribute( "compName" , this.componentName );
	}

}
