package bipUtils;

import java.util.ArrayList;
import java.util.HashMap;

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
		port.getVarParts().addAll(this.getVarParts());
		port.setHereNums(this.getHereNums());
		return port;
	}

	public HashMap<String, Integer> getVarNumsPerScope() {
		return varNumsPerScope;
	}

	public ArrayList<ArrayList<String>> getScopeVarParts() {
		return scopeVarParts;
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
	


	public ArrayList<String> getVarParts() {
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
