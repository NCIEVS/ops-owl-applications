package gov.nih.nci.evs.report;

import java.util.HashMap;

public interface ReportWriterProperty {

	
	public String getCode();
	public String getSource();
	public String getType();
	public String getParsedValue();
	public String getName();
	public String getValue();
	public HashMap<String,String> getQualifiers();
	
}
