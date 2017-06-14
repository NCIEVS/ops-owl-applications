package gov.nih.nci.evs.gobp.map;

import org.apache.log4j.Logger;

public class LogMapCell {

	String sourceEntityNamespace1;
	String sourceEntityCode1;
	String targetEntityNamespace2;
	String targetEntityCode2;
	String measure;
	String relation;
	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.map.LogMapCell.class);

	public String getSourceEntityNamespace1() {
		return sourceEntityNamespace1;
	}

	public void setSourceEntityNamespace1(String sourceEntityNamespace1) {
		this.sourceEntityNamespace1 = sourceEntityNamespace1;
	}

	public String getSourceEntityCode1() {
		return sourceEntityCode1;
	}

	public void setSourceEntityCode1(String sourceEntityCode1) {
		this.sourceEntityCode1 = sourceEntityCode1;
	}

	public String getTargetEntityNamespace2() {
		return targetEntityNamespace2;
	}

	public void setTargetEntityNamespace2(String targetEntityNamespace2) {
		this.targetEntityNamespace2 = targetEntityNamespace2;
	}

	public String getTargetEntityCode2() {
		return targetEntityCode2;
	}

	public void setTargetEntityCode2(String targetEntityCode2) {
		this.targetEntityCode2 = targetEntityCode2;
	}


	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public LogMapCell() {

	}
}
