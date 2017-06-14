package gov.nih.nci.evs.report;

import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;



public class ReportWriterOwlConcept implements ReportWriterConcept {

	private ConceptProxy internalConcept;
//	private String source;
//	private ReportWriterProperty sourcePreferredTerm;
//	private ReportWriterProperty nciPreferredTerm;
//	private ReportWriterProperty sourceDefinition;
//	private ReportWriterProperty nciDefinition;
//	private Vector<ReportWriterProperty> sourceSynonyms;
//	private Vector<ReportWriterProperty> nciSynonyms;
	
	private Vector<ReportWriterConcept> parents;
	private Vector<ReportWriterConcept> children;
	
//	private String FULL_SYN;
//	private String DEFINITION;
//	private String ALT_DEFINITION;
//	private String GO_ANNOTATION;

	public ReportWriterOwlConcept(ConceptProxy concept, String source) {
		this.internalConcept = concept;
//		this.source = source;
//		initTerms();
	}

//	private void initTerms() {
////		setByCode();
//		this.sourcePreferredTerm = getPreferredTerm(this.source);
//		this.nciPreferredTerm = getPreferredTerm("NCI");
//		this.sourceDefinition = getDefinition(this.source);
//		this.nciDefinition = getDefinition("NCI");
//		this.sourceSynonyms = getSynonyms(this.source);
//		this.nciSynonyms = getSynonyms("NCI");
//		
//	}
	
//	private void setByCode(){
//		FULL_SYN="P90";
//		DEFINITION="P97";
//		ALT_DEFINITION="P325";
//		GO_ANNOTATION="P211";
//	}
//	
//	private void setByName(){
//		FULL_SYN= "FULL_SYN";
//		DEFINITION="DEFINITION";
//		ALT_DEFINITION="ALT_DEFINITION";
//		GO_ANNOTATION="GO_Annotation";
//	}

//	@Override
//	public String getSource() {
//		return this.source;
//	}
//
//	@Override
//	public ReportWriterProperty getSourcePT() {
//		return sourcePreferredTerm;
//	}
//
//	@Override
//	public ReportWriterProperty getNCIPT() {
//		return nciPreferredTerm;
//	}
//
//	@Override
//	public ReportWriterProperty getSourceDef() {
//		return sourceDefinition;
//	}
//
//	@Override
//	public ReportWriterProperty getNciDef() {
//		return nciDefinition;
//	}
//
//	@Override
//	public Vector<ReportWriterProperty> getSourceSynonyms() {
//		return sourceSynonyms;
//	}
//
//	@Override
//	public Vector<ReportWriterProperty> getNciSynonyms() {
//		return nciSynonyms;
//	}
//


//	@Override
//	public ReportWriterProperty getPreferredTerm(String source) {
//		String name =  this.internalConcept.getName();
//		Vector<Property> props = this.internalConcept.getProperties(FULL_SYN);
//		for (Property prop:props){
//			ReportWriterProperty rwProp = new ReportWriterOwlProperty(prop);
//			
//			if (rwProp.getType().compareTo("PT")==0 && rwProp.getSource().compareTo(source)==0){
//				return rwProp;
//			}
//		}
//
//		return null;
//	}
//
//	@Override
//	public ReportWriterProperty getDefinition(String source) {
//		Vector<Property> allProps = this.internalConcept.getProperties();
//		for (Property prop:allProps){
//			if(prop.getCode().compareTo(DEFINITION)==0 || prop.getCode().compareTo(ALT_DEFINITION)==0){
//				ReportWriterProperty rwProp = new ReportWriterOwlProperty(prop);
//				if(rwProp.getSource().compareTo(source)==0){
//					return rwProp;
//				}
//			}
//		}
//
//		return null;
//	}
//
//	@Override
//	public Vector<ReportWriterProperty> getSynonyms(String source) {
//
//		Vector<Property> props = this.internalConcept.getProperties(FULL_SYN);
//		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
//		for (Property prop:props){
//			ReportWriterProperty rwProp = new ReportWriterOwlProperty(prop);
//			
//			if (rwProp.getType().compareTo("SY")==0 && rwProp.getSource().compareTo(source)==0){
//				synonyms.add(rwProp);
//			}
//		}
//
//		return synonyms;
//		
//	}

	@Override
	public String getName() {
		return internalConcept.getName();
	}

	@Override
	public String getCode() {
		return internalConcept.getCode();
	}

	@Override
	public Vector<ReportWriterProperty> getProperty(String propName) {
		Vector<Property> props = this.internalConcept.getProperties(propName);
		Vector<ReportWriterProperty> ret_Props = new Vector<ReportWriterProperty>();
		for(Property prop:props){
			ReportWriterOwlProperty rwProp = new ReportWriterOwlProperty(prop);
			ret_Props.add(rwProp);
		}
		return ret_Props;
	}



	@Override
	public Vector<ReportWriterConcept> getParents() {
		return parents;
	}

	@Override
	public void setParents(Vector<ReportWriterConcept> parents) {
		this.parents = parents;

	}

	@Override
	public Vector<ReportWriterConcept> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}

	@Override
	public void setChildren(Vector<ReportWriterConcept> children) {
		this.children = children;

	}

	@Override
	public Vector<ReportWriterProperty> getComplexProperty(String[] propInfo) {
		Vector<ReportWriterProperty> props = new Vector<ReportWriterProperty>();
		//THe expected array should be 3 fields long : prop name, qual name, qual value
		//The possible complex properties are FULL-SYN, DEFINITION, ALT_DEFINITION, GO_Annotation
//		if(propInfo.length==3){
//			String propName = propInfo[0];
//			String qualName = propInfo[1];
//			String qualValue = propInfo[2];
//			
//			if (propName.equals(FULL_SYN)){
//				props = getFullSynByQual(qualName, qualValue);
//			}
//			else if (propName.equals(DEFINITION)||propName.equals(ALT_DEFINITION)){
//				props = getDefinitionByQual(qualName, qualValue);
//			}
//			else if (propName.equals(GO_ANNOTATION)){
//				props = getGoByQual(qualName, qualValue);
//			}
//			else{
//				//TODO put GO_Anotation method in
//				System.out.println("Unrecognized Complex Property");
//			}
//			
//		}
//		else if (propInfo.length>3){
//			String propName = propInfo[0];
//			if (propName.equals(FULL_SYN)){
//				props = getFullSynByQual(propInfo);
//			}
//		}
//		return props;
		
		
		// TODO check that the propInfo is an odd number at load?
		Vector<Property> properties = this.internalConcept
				.getProperties(propInfo[0]);
		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
		int length = propInfo.length;
		// if correct, should have propID, then name/value pairs so length
		// should be odd
//		if ((length & 1) != 0) {
			HashMap<String,String> matchQuals = new HashMap<String,String>();
			int i = 1;
			while (i<length){
				String key = propInfo[i];
				String value = propInfo[++i];
				matchQuals.put(key, value);
				i++;
			}	
			
			for (Property tempProp : properties) {
				ReportWriterOwlProperty property = new ReportWriterOwlProperty(
						tempProp);
				HashMap<String, String> matchVals = property.getQualifiers();
				if (matchQualifiers(matchVals, matchQuals)){
					synonyms.add(property);
				} 

			}
			return synonyms;
		
	}
	

//	
//	@Override
//	public Vector<ReportWriterProperty> getFullSynByType(String value) {
//		Vector<Property> properties = this.internalConcept.getProperties(FULL_SYN);
//		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
//		for(Property prop:properties){
//			ReportWriterProperty rwProp = new ReportWriterOwlProperty(prop);
//			if (rwProp.getType().compareTo(value)==0){
//				synonyms.add(rwProp);
//			}
//		}
//		
//		return synonyms;
//	}
//
//	private Vector<ReportWriterProperty> getGoByQual(String qualName,
//			String qualValue) {
//
//		Vector<Property> properties = this.internalConcept.getProperties(GO_ANNOTATION);
//		Vector<ReportWriterProperty> goProps = new Vector<ReportWriterProperty>();
//		for(Property tempProp:properties){
//			ReportWriterOwlProperty property = new ReportWriterOwlProperty(
//					tempProp);
//			HashMap<String, String> matchVals = property.getQualifiers();
//			Set<String> keys = matchVals.keySet();
//			Iterator<String> iter = keys.iterator();
//			while (iter.hasNext()){
//				String name = iter.next();
//				String value = matchVals.get(name);
//				if(name.equals(qualName)&& value.equals(qualValue)){
//					goProps.add(property);
//					break;
//				}
//			}
//		}
//		return goProps;
//	}
//
//	private Vector<ReportWriterProperty> getDefinitionByQual(String qualName, String qualValue) {
//		Vector<Property> properties = this.internalConcept.getProperties();
//		Vector<ReportWriterProperty> defFiltered = new Vector<ReportWriterProperty>();
//		for (Property prop:properties) {
//			if ((prop.getCode().compareTo(DEFINITION) == 0)
//					|| (prop.getCode().compareTo(ALT_DEFINITION) == 0)) {
//				ReportWriterOwlProperty property = new ReportWriterOwlProperty(
//						prop);
//				
//				HashMap<String,String> quals = property.getQualifiers();
//				String qVal = quals.get(qualName);
//				if(qVal!=null  && qVal.compareTo(qualValue)==0)
//				{
//					defFiltered.add(property);
//				}
//			}
//		}
//		return defFiltered;
//		
//	}
	
	
	
//
//	@Override
//	public Vector<ReportWriterProperty> getFullSynByQual(String qualName,
//			String qualValue) {
//		
//		Vector<Property> properties = this.internalConcept.getProperties(FULL_SYN);
//		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
//		for (Property tempProp:properties) {
//				ReportWriterOwlProperty property = new ReportWriterOwlProperty(
//						tempProp);
//				if(qualName.equals("term-type")){
//					String matchVal = property.getType();
//					if(matchVal.equals(qualValue)){
//						synonyms.add(property);
//					}
//				}else if(qualName.equals("term-source")){
//					String matchVal = property.getSource();
//					if(matchVal.equals(qualValue)){
//						synonyms.add(property);
//					}
//				}else
//				{
//					HashMap<String, String> matchVals = property.getQualifiers();
//					Set<String> keys = matchVals.keySet();
//					Iterator<String> iter = keys.iterator();
//					while (iter.hasNext()){
//						String name = iter.next();
//						String value = matchVals.get(name);
//						if(name.equals(qualName)&& value.equals(qualValue)){
//							synonyms.add(property);
//							
//						}
//					}
//				}
//				
//		
//		}
//		return synonyms;
//		
//		
//	}

	private Vector<ReportWriterProperty> getPropertyByQual(String[] propInfo) {
		// TODO check that the propInfo is an odd number at load?
		Vector<Property> properties = this.internalConcept
				.getProperties(propInfo[0]);
		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
		int length = propInfo.length;
		// if correct, should have propID, then name/value pairs so length
		// should be odd
//		if ((length & 1) != 0) {
			HashMap<String,String> matchQuals = new HashMap<String,String>();
			int i = 1;
			while (i<length){
				String key = propInfo[i];
				String value = propInfo[++i];
				matchQuals.put(key, value);
				i++;
			}	
			
			for (Property tempProp : properties) {
				ReportWriterOwlProperty property = new ReportWriterOwlProperty(
						tempProp);
				HashMap<String, String> matchVals = property.getQualifiers();
				if (matchVals.equals(matchQuals)){
					synonyms.add(property);
				} else if (matchVals.size() > matchQuals.size()){
					
				}

			}
			return synonyms;

//		} else {
//			System.out.println("Qualifier format incorrect");
//			return null;
//		}
	}
	
	
	private boolean matchQualifiers(HashMap<String,String> propertyQuals, HashMap<String,String> configQuals){
		boolean matched = false;
		
		
		MapDifference<String,String> mapDiff = Maps.difference(propertyQuals, configQuals);
		if (mapDiff.areEqual())
		{ return true;}
		Map<String,String> mapCommon = mapDiff.entriesInCommon();
		if(mapCommon.equals(configQuals)){
			return true;
		}
		return matched;
	}
	
}
