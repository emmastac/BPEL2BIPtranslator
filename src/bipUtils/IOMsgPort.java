package bipUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.antlr.stringtemplate.StringTemplate;

import translator.TemplateMaker;

public class IOMsgPort extends Port {

	protected String operation = "";
	protected String partnerLink = "";
	protected String messageExchange = "";
	protected ArrayList<String[]> orderedCSs = new ArrayList<String[]>();
	protected HashMap<String, Integer> CSsNumPerScopeID = new HashMap<String, Integer>();
	protected ArrayList<String> orderedCSLabels = new ArrayList<String>();
	protected HashMap<String, Integer> CSLabelsNumPerScopeID = new HashMap<String, Integer>();
	protected ArrayList<String> varParts = new ArrayList<String>();

	protected String varPartsScope = "";
	protected String partnerRole = ""; // only for INVOMA
	protected boolean withCS;

	protected String initializePRole = ""; // only for INVOMA
	protected boolean isRcvIO = false;
	protected String openIMAScope = "";
	protected String partnerLinkScope = "";

	public IOMsgPort clonePort() {
		IOMsgPort port = new IOMsgPort();
		port.compIndex = this.compIndex;
		port.compName = this.compName;
		port.operation = this.operation;
		port.partnerLink = this.partnerLink;
		port.messageExchange = this.messageExchange;
		port.varPartsScope = this.varPartsScope;
		port.partnerRole = this.partnerRole;
		port.initializePRole = this.initializePRole;
		port.isRcvIO = this.isRcvIO;
		port.openIMAScope = this.openIMAScope;
		port.partnerLinkScope = this.partnerLinkScope;
		port.withCS = this.withCS;
		port.orderedCSs.addAll(this.orderedCSs);
		port.CSsNumPerScopeID.putAll(this.CSsNumPerScopeID);
		port.orderedCSLabels.addAll(this.orderedCSLabels);
		port.CSLabelsNumPerScopeID = new HashMap<String, Integer>();
		port.varParts.addAll(this.varParts);

		return port;
	}

	public String getPartnerLinkScope() {
		return partnerLinkScope;
	}

	public void setPartnerLinkScope(String partnerLinkScope) {
		this.partnerLinkScope = partnerLinkScope;
	}

	public String getVarPartsScope() {
		return varPartsScope;
	}

	public void setVarPartsScope(String varPartsScope) {
		this.varPartsScope = varPartsScope;
	}

	public ArrayList<String> getVarParts() {
		return varParts;
	}

	public boolean isWithCS() {
		return withCS;
	}

	public void setWithCS(boolean withCS) {
		this.withCS = withCS;
	}

	public String getOpenIMAScope() {
		return openIMAScope;
	}

	public void setOpenIMAScope(String openIMAScope) {
		this.openIMAScope = openIMAScope;
	}

	public IOMsgPort() {
	}

	public boolean isRcvIO() {
		return isRcvIO;
	}

	public void setRcvIO(boolean isRcvIO) {
		this.isRcvIO = isRcvIO;
	}

	public IOMsgPort(String compName, String compIndex, String operation, String partnerLink, String messageExchange) {
		super(compName, compIndex);
		this.operation = operation;
		this.partnerLink = partnerLink;
		this.messageExchange = messageExchange;
	}

	@Override
	public ArrayList<String> toArray4Template(String template) {
		ArrayList<String> array = null;
		if (template.equals("expIma") || template.equals("expOma")) {
			array = new ArrayList<String>(3);
			if (orderedCSs.size() > 0) {
				array.add(String.valueOf(orderedCSs.size())); // pos: 2
			}
		} else if (template.equals("corrIma") || template.equals("corrOma") || template.equals("corrInv")) {
			array = new ArrayList<String>(orderedCSs.size() + 3);
			array.add(String.valueOf(orderedCSs.size())); // pos: 2
			for (String[] corrSet : orderedCSs) {// pos: 3..
				array.add(corrSet[1]);
			}
		} else if(template.equals("sndMsgPorts") || template.equals("ioma")){
			array = new ArrayList<String>(3);
			array.add(getMessageExchangeLabel());
		}else if (template.equals("oma") ) {
			array = new ArrayList<String>(orderedCSs.size() + 4);
			array.add(getMessageExchangeLabel());// pos:2
				array.add(String.valueOf(orderedCSs.size())); // pos: 3
			for (String[] corrSet : orderedCSs) {// pos: 4..
				array.add(corrSet[1]);
			}
		} else if (template.equals("expInv")) {
			array = new ArrayList<String>(5);
			array.add(this.getPartnerLink());// pos:3
			array.add(this.getInitializePRole());// pos:4
			if (orderedCSs.size() > 0) {
				array.add(String.valueOf(orderedCSs.size())); // pos: 5
			}
		} else if (template.equals("inv")) {
			array = new ArrayList<String>(3);
			array.add(this.getPartnerLink());// pos:3
			array.add(this.getInitializePRole());// pos:4
		} else if (template.equals("rcvMsgPorts")) {
			//not such an entry in any template, but it is checked in the temlate among expRcvMsgPorts
			array = new ArrayList<String>(3);
			array.add(this.getMessageExchangeLabel());
		} else {
			// such as RcvMsgPort
			array = new ArrayList<String>(2);
		}
		this.toArrayBasic(array);
		return array;
	}

	public boolean isExported(String entry) {
		if (entry.equals("expIma")) {
			return (this.orderedCSLabels.size() + this.orderedCSs.size() > 0 || !this.partnerLinkScope.equals(""));

		} else if (entry.equals("disIma")) {
			return (this.orderedCSLabels.size() > 0 || !this.partnerLinkScope.equals(""));
		}
		return false;
	}

	public String getMessageExchangeLabel() {
		return partnerLink + "_" + operation + "_" + messageExchange;
	}

	public String getCorrelationSetLabel(String CS) {
		if (CS.equals("_")) {
			CS = ""; // local assignment
		}
		return partnerLink + "_" + operation + "_" + CS;
	}

	// public static void main(String[] args) {
	// IOMAPort oma = new IOMAPort();
	// oma.setCorrelationSets(new TreeMap<String, String>());
	// oma.getCorrelationSets().put("la", "lo");
	// oma.getCorrelationSets().put("li", "le");
	// System.out.println(oma.toArray4Template(null).toString());
	//
	// }

	// ///////////////////////////////////////////////////////////////////

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getPartnerLink() {
		return partnerLink;
	}

	public void setPartnerLink(String partnerLink) {
		this.partnerLink = partnerLink;
	}

	public String getMessageExchange() {
		return messageExchange;
	}

	public void setMessageExchange(String messageExchange) {
		this.messageExchange = messageExchange;
	}

	public ArrayList<String[]> getOrderedCSs() {
		return orderedCSs;
	}

	public void setOrderedCSs(ArrayList<String[]> orderedCSs) {
		this.orderedCSs = orderedCSs;
	}

	public HashMap<String, Integer> getCSsNumPerScopeID() {
		return CSsNumPerScopeID;
	}

	public ArrayList<String> getOrderedCSLabels() {
		return orderedCSLabels;
	}

	public HashMap<String, Integer> getCSLabelsNumPerScopeID() {
		return CSLabelsNumPerScopeID;
	}

	public String getPartnerRole() {
		return partnerRole;
	}

	public void setPartnerRole(String partnerRole) {
		this.partnerRole = partnerRole;
	}

	public String getInitializePRole() {
		return initializePRole;
	}

	public void setInitializePRole(String initializePRole) {
		this.initializePRole = initializePRole;
	}

}
