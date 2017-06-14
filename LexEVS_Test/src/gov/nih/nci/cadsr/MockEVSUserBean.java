package gov.nih.nci.cadsr;

import java.util.Hashtable;
import java.util.Vector;

public class MockEVSUserBean {

	
	public static Vector getVocabNameList(){
		Vector<String> vocabName = new Vector<String>();
		vocabName.add("NCI Thesaurus");
		vocabName.add("Common Terminology Criteria for Adverse Events");
		vocabName.add("Gene Ontology");
		vocabName.add("HUGO Gene Nomenclature Committee Ontology");
		vocabName.add("International Classification of Diseases, Ninth Revision, Clinical Modification, ICD-10");
		vocabName.add("Logical Observation Identifier Names and Codes");
		vocabName.add("MedDRA (Medical Dictionary for Regulatory Activities Terminology)");
		vocabName.add("The MGED Ontology");
		vocabName.add("NCI Metathesaurus");
		vocabName.add("Nanoparticle Ontology");
		vocabName.add("Ontology for Biomedical Investigations");
		vocabName.add("Radiology Lexicon");
		vocabName.add("SNOMED Clinical Terms");
		vocabName.add("UMLS Semantic Network");
		vocabName.add("National Drug File -Reference Terminology");
		vocabName.add("Zebrafish");
		
		return vocabName;
	}
	
	public static Hashtable getMetaCodeType(){
		Hashtable metaCodeTypes = new Hashtable();
		metaCodeTypes.put("NCI_META_CUI", "gov.nih.nci.cadsr.cdecurate.tool.EVS_METACODE_BEAN@6b24a494");
		metaCodeTypes.put("UMLS_CUI", "gov.nih.nci.cadsr.cdecurate.tool.EVS_METACODE_BEAN@32b12780");
		metaCodeTypes.put("UMLS_CUI" , "gov.nih.nci.cadsr.cdecurate.tools.EVS_METACODE_Bean@c985e80");
		
		return metaCodeTypes;
	}
	
	public static Vector getVocabDisplayList(){
		Vector<String> vocabDisplay = new Vector<String>();
		
		vocabDisplay.add("Logical Observation Identifier Names and Codes");
		vocabDisplay.add("Radiology Lexicon");
		vocabDisplay.add("Zebrafish");
		vocabDisplay.add("The MGED Ontology");
		vocabDisplay.add("HUGO Gene Nomenclature Committee Ontology");
		vocabDisplay.add("UMLS Semantic Network");
		vocabDisplay.add("SNOMED Clinical Terms");
		vocabDisplay.add("ICD-10");
		vocabDisplay.add("MedDRA (Medical Dictionary for Regulatory Activities Terminology)");
		vocabDisplay.add("Nanoparticle Ontology");
		vocabDisplay.add("Gene Ontology");
		vocabDisplay.add("NCI Thesaurus");
		vocabDisplay.add("National Drug File - Reference Terminology");
		vocabDisplay.add("NCI Metathesaurus");
		vocabDisplay.add("Ontology for Biomedical Investigations");
		vocabDisplay.add("International Classification of Diseases, Ninth Revision, Clinical Modification");
		vocabDisplay.add("Common Terminology Criteria for Adverse Events");

		return vocabDisplay;
	}
}
