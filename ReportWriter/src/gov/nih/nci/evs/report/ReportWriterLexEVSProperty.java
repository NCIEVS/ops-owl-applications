package gov.nih.nci.evs.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.PropertyQualifier;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Presentation;

public class ReportWriterLexEVSProperty implements ReportWriterProperty{
	private String source = "";

	private String code = "";

	private String type = "";

	private String value = "";

	private Property internalProperty;
	
	private HashMap<String, String> qualifiers = new HashMap<String, String>();

	/**
	 * 
	 * @param inProp
	 */
	public ReportWriterLexEVSProperty(Property inProp) {
		this.internalProperty = inProp;
		if (inProp.getPropertyType().equals("presentation"))
		{
			initPresentation();
		}
		else if (inProp.getPropertyType().equals("definition")){
			initDefinition();
		}
		else {
		initRWProperty();
		}
	}

	public ReportWriterLexEVSProperty(Definition inProp) {
		this.internalProperty = inProp;
		initDefinition();
	}

	public ReportWriterLexEVSProperty(Presentation inProp) {
		this.internalProperty = inProp;
		initPresentation();
	}

	/**
	 * 
	 * @return
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * 
	 * @return
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * 
	 * @return
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * 
	 * @return
	 */
	public String getParsedValue() {
		return this.value;
	}

	private void initPresentation() {
		Presentation pres = (Presentation) this.internalProperty;
		try{
		if (pres.getSource().length>0){
		this.source = pres.getSource(0).getContent();}
		this.type = pres.getRepresentationalForm();
		this.value = pres.getValue().getContent();
//		qualifiers.put("source-code", value);
		if (pres.getPropertyName().compareTo("FULL_SYN") == 0) {
		qualifiers.put("term-source", source);
		qualifiers.put("term-group", type);
		qualifiers.put("term-name", value);}
		

			HashMap<String, String> matchVals = getQualifiers();
			Set<String> keys = matchVals.keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()){
					String name = iter.next();
					String value = matchVals.get(name);
				if (name.compareTo("source-code") == 0) {
					this.code = value;
				} else if (name.compareTo(
						"term-group") == 0) {
					this.type = value;
				} else if (name.compareTo(
						"term-source") == 0) {
					this.source = value;
				} else if (name.compareTo(
						"term-name") == 0) {
					this.value = value;
				}
			}
//		}
		
		}
		catch (Exception e){
			System.out.println("Issue instantiating "+ internalProperty.getPropertyName());
		}
	}

	private void initDefinition() {

		Definition def = (Definition)this.internalProperty;
		this.source = def.getSource(0).getContent();
		this.value = def.getValue().getContent();
		qualifiers.put("def-source", source);
		qualifiers.put("def-value", value);
//		if (def.getPropertyName().compareTo("DEFINITION") == 0) {
//			HashMap<String, String> matchVals = getQualifiers();
//			Set<String> keys = matchVals.keySet();
//			Iterator<String> iter = keys.iterator();
//			while (iter.hasNext()){
//					String name = iter.next();
//					String value = matchVals.get(name);
//				if (name.compareTo("def-source") == 0) {
//					this.source = value;
//				} else if (name.compareTo(
//						"def-definition") == 0) {
//					this.value = value;
//				}
//			}
//		} else if (def.getPropertyName().compareTo(
//				"ALT-DEFINITION") == 0) {
//			HashMap<String, String> matchVals = getQualifiers();
//			Set<String> keys = matchVals.keySet();
//			Iterator<String> iter = keys.iterator();
//			while (iter.hasNext()){
//					String name = iter.next();
//					String value = matchVals.get(name);
//				if (name.compareTo("def-source") == 0) {
//					this.source =value;
//				} else if (name.compareTo(
//						"def-definition") == 0) {
//					this.value = value;
//				}
//			}
//		}
	}

	/*
 * 
 */
	private void initRWProperty() {

//		this.source = this.internalProperty.getSource(0).getContent();
		this.value = this.internalProperty.getValue().getContent();
//		if (this.internalProperty.getPropertyName().compareTo("Use_For") == 0) {
		HashMap<String, String> matchVals = getQualifiers();
		Set<String> keys = matchVals.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()){
				String name = iter.next();
				String value = matchVals.get(name);
				if (name.compareTo("use-code") == 0) {
					this.code = value;
				} else if (name.compareTo(
						"use-source") == 0) {
					this.source = value;
				} else if (name.compareTo(
						"use-value") == 0) {
					this.value = value;
				} else { this.value = value;}
				
			}
//		}
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String,String> getQualifiers() {
//		PropertyQualifier[] quals = this.internalProperty
//				.getPropertyQualifier();
//		HashMap<String,String> qualifiers = new HashMap<String,String>();
//		
//		for (int i = 0; i < quals.length; i++) {
//			PropertyQualifier qual = quals[i];
//			String key = qual.getPropertyQualifierName();
//			String value = qual.getValue().getContent();
//			qualifiers.put(key, value);
//		}

		return qualifiers;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return this.internalProperty.getPropertyName();
	}

	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return this.internalProperty.getValue().getContent();
	}


}
