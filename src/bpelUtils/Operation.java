package bpelUtils;

public class Operation {
	
	
	private String name;
	private String[] fault;
	private boolean hasOutput;
	
	

	public Operation(String name) {
		super();
		this.name = name;
		fault = new String[]{null,null};
		hasOutput = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getFault() {
		return fault;
	}
	public void setFault(String[] fault) {
		this.fault = fault;
	}

	public boolean isHasOutput() {
		return hasOutput;
	}

	public void setHasOutput(boolean hasOutput) {
		this.hasOutput = hasOutput;
	}

}
