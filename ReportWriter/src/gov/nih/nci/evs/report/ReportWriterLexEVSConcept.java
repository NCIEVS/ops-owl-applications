package gov.nih.nci.evs.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.PropertyQualifier;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class ReportWriterLexEVSConcept implements ReportWriterConcept{

//	private ReportWriterLexEVSProperty sourcePreferredTerm;
//
//	private ReportWriterLexEVSProperty nciPreferredTerm;
//
//	private Vector<ReportWriterProperty> sourceSynonyms;
//
//	private Vector<ReportWriterProperty> nciSynonyms;
//	
//	private String source;
//
//	private ReportWriterProperty sourceDefinition;
//
//	private ReportWriterProperty nciDefinition;

	private Entity internalEntity;
	
	private Vector<ReportWriterConcept> parents;
	private Vector<ReportWriterConcept> children;

	/**
	 * 
	 * @param ent
	 * @param source
	 */
	public ReportWriterLexEVSConcept(Entity ent, String source) {
		this.internalEntity = ent;
//		this.source= source;
		initTerms();
	}
	
	public ReportWriterLexEVSConcept(Entity ent, String source, HashMap<String, String> props){
		this.internalEntity = ent;
//		this.source= source;
		initTerms();
		initRequested(props);
	}

	/**
	 * 
	 * @param inSource
	 */
	private void initTerms() {
//		this.sourcePreferredTerm = getPreferredTerm(this.source);
//		this.nciPreferredTerm = getPreferredTerm("NCI");
//		this.sourceDefinition = getDefinition(this.source);
//		this.nciDefinition = getDefinition("NCI");
//		this.sourceSynonyms = getSynonyms(this.source);
//		this.nciSynonyms = getSynonyms("NCI");
//		this.sourceUseFor = getUseFor(this.source);
	}
	
	private void initRequested(HashMap<String, String> reqProps){
		Set<String> keys = reqProps.keySet();
		Iterator<String> iter = keys.iterator();
		Vector<String> rawPropInfo = new Vector<String>();
		while (iter.hasNext()){
			rawPropInfo.add(reqProps.get(iter.next()));
		}
		
	}
	

	/**
	 * 
	 * @return
	 */
//	public String getSource() {
//		return this.source;
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	public ReportWriterLexEVSProperty getSourcePT() {
//		return this.sourcePreferredTerm;
//	}

	/**
	 * 
	 * @return
//	 */
//	public ReportWriterLexEVSProperty getNCIPT() {
//		return this.nciPreferredTerm;
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	public ReportWriterProperty getSourceDef() {
//		return this.sourceDefinition;
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	public ReportWriterProperty getNciDef() {
//		return this.nciDefinition;
//	}

	/**
	 * 
	 * @return
	 */
//	public Vector<ReportWriterProperty> getSourceSynonyms() {
//		return this.sourceSynonyms;
//	}
//
//	/**
//	 * 
//	 * @return
//	 */
//	public Vector<ReportWriterProperty> getNciSynonyms() {
//		return this.nciSynonyms;
//	}



	/**
	 * 
	 * @param source
	 * @return
	 */
//	public ReportWriterLexEVSProperty getPreferredTerm(String source) {
//
//		Presentation[] properties = this.internalEntity.getPresentation();
//		for (int i = 0; i < properties.length; i++) {
//			Presentation tempProp = properties[i];
//			if (tempProp.getPropertyName().compareTo("FULL_SYN") == 0) {
//				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
//						tempProp);
//
//				if ((property.getType().compareTo("PT") == 0)
//						&& (property.getSource().compareTo(source) == 0))
//					return property;
//			}
//		}
//		return null;
//	}

	/**
	 * 
	 * @param source
	 * @return
//	 */
//	public ReportWriterLexEVSProperty getDefinition(String source) {
//		Definition[] properties = this.internalEntity.getDefinition();
//		for (int i = 0; i < properties.length; i++) {
//			Definition tempProp = properties[i];
//			if ((tempProp.getPropertyName().compareTo("DEFINITION") == 0)
//					|| (tempProp.getPropertyName().compareTo("ALT_DEFINITION") == 0)) {
//				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
//						tempProp);
//				// property.initRWProperty();
//				if (property.getSource().compareTo(source) == 0)
//					return property;
//			}
//		}
//		return null;
//	}

	/**
	 * 
	 * @param source
	 * @return
	 */
//	public Vector<ReportWriterProperty> getSynonyms(String source) {
//
//		Presentation[] properties = this.internalEntity.getPresentation();
//		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
//		for (int i = 0; i < properties.length; i++) {
//			Property tempProp = properties[i];
//			if (tempProp.getPropertyName().equals("FULL_SYN")) {
//				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
//						tempProp);
//
//				if ((property.getType().compareTo("SY") != 0)
//						&& (property.getSource().compareTo(source) == 0)) {
//					synonyms.add(property);
//				}
//			}
//		}
//		return synonyms;
//	}

//	public Vector<ReportWriterProperty> getFullSynByQual(String qualName, String qualValue) {
//
//		Presentation[] properties = this.internalEntity.getPresentation();
//		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
//		for (int i = 0; i < properties.length; i++) {
//			Property tempProp = properties[i];
//			if (tempProp.getPropertyName().equals("FULL_SYN")) {
//				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
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
//							break;
//						}
//					}
//				}
//				
//
//			}
//		}
//		return synonyms;
//	}
	

	

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return this.internalEntity.getEntityDescription().getContent();
	}

	/**
	 * 
	 * @return
	 */
	public String getCode() {

		return this.internalEntity.getEntityCode();
	}
	
	public Vector<ReportWriterProperty> getComplexProperty(String[] propInfo){
		Vector<ReportWriterProperty> props = new Vector<ReportWriterProperty>();
		//THe expected array should be 3 fields long : prop name, qual name, qual value
		//The possible complex properties are FULL-SYN, DEFINITION, ALT_DEFINITION, GO_Annotation
//		if(propInfo.length>=3){
//			String propName = propInfo[0];
//			String qualName = propInfo[1];
//			String qualValue = propInfo[2];
//			
//			if (propName.equals("FULL-SYN")){
//				props = getFullSynByQual(qualName, qualValue);
//			}
//			else if (propName.equals("DEFINITION")||propName.equals("ALT_DEFINITION")){
//				props = getDefinitionByQual(qualName, qualValue);
//			}
//			else{
//				//TODO put GO_Anotation method in
//			}
//		}
//		return props;
		
		
		
		// TODO check that the propInfo is an odd number at load?
		Property[] requestedProps = this.internalEntity.getAllProperties();
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
			
			for(int j=0; j< requestedProps.length;j++){
				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
						requestedProps[j]);
				HashMap<String, String> matchVals = property.getQualifiers();
				if (matchQualifiers(matchVals, matchQuals)){
					synonyms.add(property);
				} 
			}
			return synonyms;
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
	
	
	public Vector<ReportWriterProperty> getProperty(String propName) {
		Vector<ReportWriterProperty> propVec = new Vector<ReportWriterProperty>();
		Property[] requestedProps = this.internalEntity.getAllProperties();
		
		for(int i=0; i< requestedProps.length;i++){
			Property prop = requestedProps[i];
//			System.out.println(prop.getPropertyName());
			if (prop.getPropertyName().equals(propName)){
				ReportWriterLexEVSProperty rwProp = new ReportWriterLexEVSProperty(prop);
				propVec.add(rwProp);
			}
		}
		
		
		return propVec;
	}
	
//	private Vector<ReportWriterProperty> getDefinitionByQual(String qualName, String qualValue) {
//		Definition[] properties = this.internalEntity.getDefinition();
//		Vector<ReportWriterProperty> defFiltered = new Vector<ReportWriterProperty>();
//		for (int i = 0; i < properties.length; i++) {
//			Definition tempProp = properties[i];
//			if ((tempProp.getPropertyName().compareTo("DEFINITION") == 0)
//					|| (tempProp.getPropertyName().compareTo("ALT_DEFINITION") == 0)) {
//				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
//						tempProp);
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


	
//	public Vector<ReportWriterProperty> getFullSynByType(String value){
//		Presentation[] properties = this.internalEntity.getPresentation();
//		Vector<ReportWriterProperty> synonyms = new Vector<ReportWriterProperty>();
//		for (int i = 0; i < properties.length; i++) {
//			Property tempProp = properties[i];
//			if (tempProp.getPropertyName().equals("FULL_SYN")) {
//				ReportWriterLexEVSProperty property = new ReportWriterLexEVSProperty(
//						tempProp);
//
//				if ((property.getType().compareTo(value) != 0)) {
//					synonyms.add(property);
//				}
//			}
//		}
//		return synonyms;
//	}
	
	public Vector<ReportWriterConcept> getParents(){
		return parents;
	}

	public void setParents(Vector<ReportWriterConcept> parents) {
		this.parents = parents;
	}

	public Vector<ReportWriterConcept> getChildren() {
		return children;
	}

	public void setChildren(Vector<ReportWriterConcept> children) {
		this.children = children;
	}


	

}
