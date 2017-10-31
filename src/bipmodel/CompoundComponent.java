package bipmodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Element;

import stUtils.AttrFiller;
import stUtils.MapKeys;
import stUtils.MapUtils;
import translator.TemplateList;

public abstract class CompoundComponent extends Component {

	public ArrayList childComp;
	public int count;
	public HashMap templatePortsMap;


	public CompoundComponent() {

		super( );
		childComp = new ArrayList( );
		templatePortsMap = new HashMap( );
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected void parseElement( ArrayDeque < HashMap < String , ArrayList >> stack , HashMap < String , ArrayList > ret , Element element ,
			TemplateList templateList ) {

		super.parseElement( stack , ret , element , templateList );

		ArrayList components = stack.peek( ).get( MapKeys.CHILD_COMP );
		for ( Object component : components ) {
			if ( component == null ) {
				continue;
			}
			childComp.add( component );
		}
		count = childComp.size( );

	}


	@Override
	protected void prepareTemplate( String templateFile , TemplateList templateList ) {

		super.prepareTemplate( templateFile , templateList );

		AttrFiller.addToTemplate( childComp , this.template , MapKeys.CHILD_COMP );
		// if ( count > 1 ) {
		template.setAttribute( "count" , count );
		// }

		template.setAttribute( "ports" , Arrays.asList( new HashMap[] {templatePortsMap} ) );
	}

}
