package gov.nih.nci.evs.gobp.diff;

import java.net.URI;

public class ChangeInstance implements Comparable<ChangeInstance> {
	// This is an instance of a change that has occurred in an OWL Class since
	// the last version

	private ChangeTypeEnum changeType = ChangeTypeEnum.None;
	private String oldValue, newValue = "";
	private URI conceptCode;
	private String propertyId = "";
	private String changeDescription = "";

	public ChangeInstance(ChangeTypeEnum changeType, URI code,
			String newValue, String oldValue) {
		this.changeType = changeType;
		this.conceptCode = code;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public ChangeInstance(ChangeTypeEnum changeType, URI code,
			String newValue, String oldValue, String changeDescription) {
		this.changeType = changeType;
		this.conceptCode = code;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.changeDescription = changeDescription;
	}


	public ChangeTypeEnum getChangeType() {
		return changeType;
	}

	public void setChangeType(ChangeTypeEnum changeType) {
		this.changeType = changeType;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public URI getConceptCode() {
		return conceptCode;
	}

	public void setConceptCode(URI conceptCode) {
		this.conceptCode = conceptCode;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String toString() {
		String output = this.getConceptCode() + " "
				+ this.getChangeType().toString() + " "
				+ this.getChangeDescription() + " " + this.getPropertyId()
				+ " " + this.getNewValue() + " " + this.getOldValue();
		return output;
	}

	@Override
	public int compareTo(ChangeInstance o) {
		// TODO Auto-generated method stub
		if (!this.getChangeType().equals(o.getChangeType())) {
			return this.changeType.compareTo(o.changeType);
		} else if (!this.getConceptCode().equals(o.getConceptCode())) {
			return this.getConceptCode().compareTo(o.getConceptCode());
		} else if (!this.getPropertyId().equals(o.getPropertyId())) {
			return this.getPropertyId().compareTo(o.getPropertyId());
		} else if (!this.getNewValue().equals(o.getNewValue())) {
			return this.getNewValue().compareTo(o.getNewValue());
		} else {
			return this.getOldValue().compareTo(this.getOldValue());
		}

	}

	public String getChangeDescription() {
		return changeDescription;
	}

	public void setChangeDescription(String changeDescription) {
		this.changeDescription = changeDescription;
	}

}
