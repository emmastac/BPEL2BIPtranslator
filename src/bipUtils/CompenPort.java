package bipUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.antlr.stringtemplate.StringTemplate;

import translator.TemplateMaker;


public class CompenPort extends Port{
	
	private String targetScope;
	private HashSet<String> targetRoles = new HashSet<String>();
	private HashSet<String> faultRoles = new HashSet<String>();
	

	public CompenPort(){}
	public CompenPort(String compName, String compIndex, String scopeName) {
		super(compName,compIndex);
		this.targetScope = scopeName;
	}

	@Override
	public ArrayList<String> toArray4Template(String template) {
		ArrayList<String> array = null;
		if(template.equals("compensatePort")){
			array = new ArrayList<String>(targetRoles.size()+4);
			if(targetScope.equals("")){
				array.add(targetScope); 
			}else{
				array.add("_"+targetScope); 
			}
			array.add(String.valueOf(targetRoles.size()));
			array.addAll(targetRoles);
		}else if(template.equals("expComp")){
			array = new ArrayList<String>(3);
			array.add(targetScope); 
		}else if(template.equals("faultCmp")){
			array = new ArrayList<String>(faultRoles.size()+2);
			array.addAll(faultRoles); 
		}else{ 
			array = new ArrayList<String>(2);
		}
		this.toArrayBasic(array);
		return array;
	}



	/////////////////////////////////////////////////////////////////////
	
	
	public HashSet<String> getTargetRoles() {
		return targetRoles;
	}
	public void setTargetRoles(HashSet<String> rvrsScopeRoles) {
		this.targetRoles = rvrsScopeRoles;
	}
	public String getTargetScope() {
		return targetScope;
	}
	public void setTargetScope(String scopeName) {
		this.targetScope = scopeName;
	}
	public HashSet<String> getFaultRoles() {
		return faultRoles;
	}
	public void setFaultRoles(HashSet<String> faultRoles) {
		this.faultRoles = faultRoles;
	}

	
}
