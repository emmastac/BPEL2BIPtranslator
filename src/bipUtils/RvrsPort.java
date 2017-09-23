package bipUtils;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;

import translator.TemplateMaker;

public class RvrsPort extends Port {

	private boolean throwsFault = false; // whether there may occur a fault at compensation

	public RvrsPort(String compName, String compIndex) {
		super(compName, compIndex);
	}

	@Override
	public ArrayList<String> toArray4Template(String template) {
		ArrayList<String> array;

		if (template.equals("rvrsPort") || template.equals("expRvrsScope") ) {
			array = new ArrayList<String>(3);
			if(throwsFault){
				array.add("1");
			}
		} else {
			array = new ArrayList<String>(2);
		}
		this.toArrayBasic(array);

		return array;
	}

	// /////////////////////////////////////////////////

	
	public boolean throwsFault() {
		return throwsFault;
	}

	public void setThrowsFault(boolean throwsFault) {
		this.throwsFault = throwsFault;
	}


	
	

}
