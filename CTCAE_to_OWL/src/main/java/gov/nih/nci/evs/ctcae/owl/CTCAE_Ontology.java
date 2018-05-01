package gov.nih.nci.evs.ctcae.owl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;

import gov.nih.nci.evs.ctcae.Messages;
import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Concept;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.exceptions.PropertyException;
import gov.nih.nci.evs.owl.meta.PropertyDef;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

public class CTCAE_Ontology extends OWLKb {
	/*
	 * Metadata should include NCIt and MedDRA versions linked
	 * 
	 * * Hierarchy will be AE_by_SOC>SOC>Class>Grade AE_by_Grade>list of grades
	 * 
	 * id which will match the rdfs:label rdfs:label MedDRA_Code DEFINITION
	 * CTCAE|PT FULL_SYN MedDRA|LLT FULL_SYNs Optional Preferred_Name NCIt_Code
	 * code - based on Ecode Navigational_Note - optional ?MedDRA SOC?
	 * 
	 * Grades will have the Is_Grade restriction Grades will have subClassOf the
	 * general class
	 * 
	 * Hierarchy will be AE_by_SOC>SOC>Class>Grade AE_by_Grade>list of grades
	 * 
	 * 
	 */
	private ConceptProxy AEbySOC_root;

	public CTCAE_Ontology(URI outputURI, String namespace) {
		super(outputURI, namespace, true, true);
		this.setDeprecatedBranch(createURI( "deprecated",namespace));
//		createAEseverityTree();
		createAEbySOC();
	}

	
	
	public void addAnnotationPropertyToConcept(URI uri, Property newProp) {

		this.addQualifiedAnnotationPropertytoConcept(IRI.create(uri), newProp);
	}

	public void addAnnotationPropertyToConcept(String conceptCode, Property newProp) {
		// check the property names versus the properties files then send to the
		// correct method(s)

		this.addAnnotationPropertyToConcept(URI.create(getDefaultNamespace() + "#" + conceptCode), newProp);
	}

	private void createAEbySOC() {
		/**
		 * <!--
		 * http://ncicb.nci.nih.gov/xml/owl/EVS/ctcae.owl#Adverse_Event_by_System_Organ_Class
		 * -->
		 * 
		 * <owl:Class rdf:about="#Adverse_Event_by_System_Organ_Class">
		 * <rdfs:label rdf:datatype="&xsd;string" >Adverse Event by System Organ
		 * Class</rdfs:label> <rdfs:subClassOf rdf:resource="#CTCAE4"/>
		 * <FULL_SYN rdf:parseType=
		 * "Literal"><ncicp:ComplexTerm><ncicp:term-name>Adverse Event by System
		 * Organ
		 * Class</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>CTCAE</ncicp:term-source></ncicp:ComplexTerm></FULL_SYN>
		 * <Preferred_Name rdf:datatype="&xsd;string" >Adverse Event by System
		 * Organ Class</Preferred_Name>
		 * <code rdf:datatype="&xsd;string">E10002</code> </owl:Class>
		 */
		String conceptName = "Adverse Event by System Organ Class";
		AEbySOC_root = createConcept(createURI( "C143163",this.getDefaultNamespace()),
				conceptName);
		URI propURI = createURI("Preferred_Name",this.getDefaultNamespace());
		Property prop = new Property(propURI, "Preferred_Name", conceptName, this);
		AEbySOC_root.addProperty(prop);

		try {
			Vector<Qualifier> quals = new Vector<Qualifier>();
			propURI = createURI("FULL_SYN",this.getDefaultNamespace());
			
			URI qualGroupURI = createURI( "term-group",this.getDefaultNamespace());
			URI qualSourceURI = createURI("term-source",this.getDefaultNamespace());
			
			Qualifier qual1 = new Qualifier(qualGroupURI, "term-group", "PT", this);
			Qualifier qual2 = new Qualifier(qualSourceURI, "term-source", "NCI", this);
			quals.add(qual1);
			quals.add(qual2);
			Property prop2 = new Property(propURI, "FULL_SYN", conceptName, this,quals);
			AEbySOC_root.addProperty(prop2);
			
			quals = new Vector<Qualifier>();
			qual1 = new Qualifier(qualSourceURI,"term-source", "CTCAE", this);
			qual2 = new Qualifier(qualGroupURI, "term-group", "PT", this);
			quals.addElement(qual1);
			quals.add(qual2);
			Property prop3 = new Property(propURI, "FULL_SYN", conceptName, this, quals);
			AEbySOC_root.addProperty(prop3);
			
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	
	private void createAEseverityTree() {
		/**
		 * Adverse Event Severity Grade Grade 1 Adverse Event .... Grade 5
		 * Adverse Event
		 */
		String conceptName = "Adverse Event Severity Grade";
		ConceptProxy AEbgrade_root = createConcept(createURI("Adverse_Event_Severity_Grade",this.getDefaultNamespace()),
				conceptName);
		URI propURI = createURI("Preferred_Name",this.getDefaultNamespace());
		Property prop = new Property(propURI, "Preferred_Name", conceptName, this);
		AEbgrade_root.addProperty(prop);

		try {
			propURI = createURI("FULL_SYN",this.getDefaultNamespace());
			Vector<Qualifier> quals = new Vector<Qualifier>();
			
			URI qualURI = createURI( "term-group",this.getDefaultNamespace());
			Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", this);
			quals.add(qual1);
			qualURI = createURI("term-source",this.getDefaultNamespace());
			Qualifier qual2;

			qual2 = new Qualifier(qualURI, "term-source", "NCI", this);
			quals.add(qual2);
			Property prop2 = new Property(propURI, "FULL_SYN", conceptName, this, quals);
			AEbgrade_root.addProperty(prop2);
			createGradeConcept(AEbgrade_root);
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void createGradeConcept(ConceptProxy root){
		
		for(Integer i=1;i<6;i++){
			String conceptCode = "Grade_"+i.toString()+"_Adverse_Event";
			String conceptName = "Grade "+i.toString()+" Adverse Event";
			ConceptProxy grade = createConcept(createURI( conceptCode,this.getDefaultNamespace()),
					conceptName);
			try {
				URI propURI = createURI("Preferred_Name",this.getDefaultNamespace());			
				Property prop = new Property(propURI, "Preferred_Name", conceptName, this);
				grade.addProperty(prop);
				
				
				propURI = createURI("FULL_SYN",this.getDefaultNamespace());
				Vector<Qualifier> quals = new Vector<Qualifier>();
				URI qualURI = createURI( "term-group",this.getDefaultNamespace());
				Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", this);
				quals.add(qual1);
				qualURI = createURI("term-source",this.getDefaultNamespace());
				Qualifier qual2;
				qual2 = new Qualifier(qualURI, "term-source", "NCI", this);
				quals.add(qual2);
				Property prop2 = new Property(propURI, "FULL_SYN", conceptName, this, quals);
				grade.addProperty(prop2);
				
				grade.addParent(root.getConcept().getURI());
			} catch (PropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

	

	public ConceptProxy createSOCConcept(URI code, String name, String medDRA_Code) {
		/**
		 * <owl:Class rdf:about="#Gastrointestinal_disorders">
		 * <rdfs:label rdf:datatype="&xsd;string" >Gastrointestinal
		 * disorders</rdfs:label> <rdfs:subClassOf rdf:resource=
		 * "#Adverse_Event_by_System_Organ_Class"/>
		 * <MedDRA_Code rdf:datatype="&xsd;string">10017947</MedDRA_Code>
		 * <FULL_SYN rdf:parseType=
		 * "Literal"><ncicp:ComplexTerm><ncicp:term-name>Gastrointestinal
		 * disorders</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>CTCAE</ncicp:term-source></ncicp:ComplexTerm></FULL_SYN>
		 * <code rdf:datatype="&xsd;string">E13859</code>
		 * <Preferred_Name rdf:datatype="&xsd;string" >Gastrointestinal
		 * disorders</Preferred_Name> </owl:Class>
		 */
		
		ConceptProxy socConcept2  = this.getConcept(code);
		if(!(socConcept2 == null)) {
			return socConcept2;
			}
		ConceptProxy socConcept = createConcept(code, name);
		if (socConcept.getParentCodes().size() == 0) {
			socConcept.addParent(AEbySOC_root.getConcept().getURI());
			// AddFULLSYN
			try {
				URI pnURI = createURI("Preferred_Name",this.getDefaultNamespace());			
				Property prop = new Property(pnURI, "Preferred_Name", name, this);
				socConcept.addProperty(prop);
				this.assignRdfLabelToConcept(code, name);
				
				URI propURI = createURI( "FULL_SYN",this.getDefaultNamespace());
				Vector<Qualifier> quals = new Vector<Qualifier>();
				
				URI qualURI = createURI( "term-group",this.getDefaultNamespace());
				Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", this);
				quals.add(qual1);
				qualURI = createURI( "term-source",this.getDefaultNamespace());
				Qualifier qual2;

				qual2 = new Qualifier(qualURI, "term-source", "NCI", this);
				quals.add(qual2);
				Property prop2 = new Property(propURI, "FULL_SYN", name, this, quals);
				socConcept.addProperty(prop2);
			} catch (PropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try{
			// Add MedDRA_Code
			URI propURI = createURI( "MedDRA_Code", this.getDefaultNamespace());
			Property medCode = new Property(propURI, "MedDRA_Code", medDRA_Code, this);
			socConcept.addProperty(medCode);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return socConcept;
	}
	
	public HashMap<String, Vector<ConceptProxy>> sortConceptsBySOC(){
		HashMap<URI, ConceptProxy> concepts = this.getAllConcepts();
		Iterator iter = concepts.keySet().iterator();
		HashMap<String, Vector<ConceptProxy>> conceptsBySOC = new HashMap<String, Vector<ConceptProxy>>();
		while (iter.hasNext()){
			ConceptProxy concept = concepts.get(iter.next());
			if(concept.getProperty("MedDRA_Code")==null){
				System.out.println("No MedDRA Code " + concept.getCode() + " " + concept.getName());
			} else {
			String medCode = concept.getProperty("MedDRA_Code").getValue();
			if(conceptsBySOC.containsKey(medCode)){
				//add this concept to the collection
				Vector<ConceptProxy> medConcepts = conceptsBySOC.get(medCode);
				medConcepts.addElement(concept);
				conceptsBySOC.put(medCode, medConcepts);
			} else {
				//create medCode reference and add concept
				Vector<ConceptProxy> medConcepts = new Vector<ConceptProxy>();
				medConcepts.add(concept);
				conceptsBySOC.put(medCode, medConcepts);
			}}
		}
		
		return conceptsBySOC;
		
	}
	


}
