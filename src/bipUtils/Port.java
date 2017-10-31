package bipUtils;

import java.util.ArrayList;

import bipmodel.BIPtemplate;


public abstract class Port implements Cloneable{
	
	protected String compIndex = null;
	protected String compName = null;
	
	public Port(String compName, String compIndex){
		this.compIndex = compIndex;
		this.compName=compName;
	}
	
	public Port(){
		
	}
	public abstract ArrayList<String> toArray4Template(String template);
	
	public void toArrayBasic (ArrayList<String> array){
			array.add(0,compName);
			array.add(1,compIndex);
		
	}

	public String getCompIndex() {
		return compIndex;
	}

	public void setCompIndex(String id) {
		this.compIndex = id;
	}

	public String getCompName() {
		return compName;
	}

	public void setCompName(String compName) {
		this.compName = compName;
	}
	
	
	

}
