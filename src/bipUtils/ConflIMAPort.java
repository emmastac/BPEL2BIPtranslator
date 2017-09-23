package bipUtils;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;

import translator.TemplateMaker;

public class ConflIMAPort extends Port{
	
	protected String pLink_oper;
	protected String conflsNum;
	protected ArrayList<String> compIndices;
	

	public ConflIMAPort(String compName, String compIndex, String pLink_oper, String conflsNum, ArrayList<String> compIndices) {
		super(compName, compIndex);
		this.pLink_oper = pLink_oper;
		this.conflsNum = conflsNum;
		this.compIndices = compIndices;
	}
	@Override
	public ArrayList<String> toArray4Template(String template) {
		ArrayList<String> array = new ArrayList<String>();
		this.toArrayBasic(array); // here does nothing (compName and compInd are null)
//		if(template.equals("expConflIma")){
			array.add(conflsNum);
			array.add(pLink_oper);
			array.addAll(compIndices);
//		}
		
		return array;
	}
/////////////////////////////////////////////////////////////////////
	public String getPLink_oper() {
		return pLink_oper;
	}

	public void setPLink_oper(String pLink_oper) {
		this.pLink_oper = pLink_oper;
	}

	public String getConflsNum() {
		return conflsNum;
	}

	public void setConflsNum(String conflsNum) {
		this.conflsNum = conflsNum;
	}
	
	

}
