package gov.nih.nci.evs.report;

import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Qualifier;

import java.util.HashMap;
import java.util.Vector;

public class ReportWriterOwlProperty implements ReportWriterProperty {

	private String name;
	private String value;
	private String source;
	private String sourceCode;
	private String type;
	private Property internalProperty;

	public ReportWriterOwlProperty(String name, String value, String source){
		this.name = name;
		this.value = value;
		this.source = source;
		this.sourceCode = "";
		this.type = "";
	}
	
	public ReportWriterOwlProperty(Property prop){
		this.internalProperty= prop;
		this.name = prop.getName();
		this.value = prop.getValue();
		this.type="";
		this.source = "NCI";
		this.sourceCode = "";
		Vector<Qualifier> quals = prop.getQualifiers();
		if(quals != null){
		for(Qualifier qual:quals){
			if (qual.getName().contains("go-term")){
				//go-term, go-id,go-evi,source-date,go-source
//				System.out.println("debug");
				//TODO Do I need to do something with GO?
			}
			if (qual.getName().equals("term-source")|| (qual.getName().equals("def-source"))){
				this.source = qual.getValue();
			}
			else if (qual.getName().equals("term-group")){
				this.type = qual.getValue();
			}
			else if (qual.getName().equals("source-code")){
				this.sourceCode = qual.getValue();
			}
			else if (qual.getName().equals("term-name") || qual.getName().equals("def-definition")){
				this.value = qual.getValue();
			}
		}}
	}
	
	@Override
	public String getCode() {
		return this.sourceCode;
	}

	@Override
	public String getSource() {
		return this.source;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public String getParsedValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		//TODO compare this to LexEVSProperty
		return this.value;
	}

	@Override
	public HashMap<String, String> getQualifiers() {

		HashMap<String,String> ret_Quals = new HashMap<String,String>();
		Vector<Qualifier> qualifiers = internalProperty.getQualifiers();
		for(Qualifier qual:qualifiers){
			ret_Quals.put(qual.getName(), qual.getValue());
		}
		return ret_Quals;
	}

}
