package gov.nih.nci.evs;

public class Change {
	private String changeType;
	private ValueSet vs;
	
	public Change(String type, ValueSet vs) {
		this.changeType = type;
		this.vs = vs;
	}
	
	public String getChangeType() {
		return changeType;
	}
	
	public ValueSet getValueSet() {
		return vs;
	}
}
