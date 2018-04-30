package gov.nih.nci.evs.ctcae;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import gov.nih.nci.evs.ctcae.data.CTCAE_parser;
import gov.nih.nci.evs.ctcae.data.MedDRA_LLT;
import gov.nih.nci.evs.ctcae.data.MedDRA_LLT_parser;
import gov.nih.nci.evs.ctcae.data.MedDRA_SOC_parser;
import gov.nih.nci.evs.ctcae.owl.CTCAE_Ontology;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.exceptions.PropertyException;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.owl.proxy.PropertyProxy;
import gov.nih.nci.evs.owl.proxy.RoleProxy;




public class CTCAEMainProcessor {
	Messages messages;
	
	private CTCAE_Ontology ontology;
	static String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/ctcae.owl";
	private HashMap<String,Vector<MedDRA_LLT>> meddraLLTData = new HashMap<String,Vector<MedDRA_LLT>>();
	private HashMap<String,String> MedDRA_SOC = new HashMap<String,String>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
		    String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		    URI outputURI = new URI("file:///" + currentPath + "/ctcae.owl");
		    File ctcaeFile  = new File(args[0]);
		    
		    File meddraLLT = new File(args[1]);
		    File meddraSOC = new File(args[2]);
		    final CTCAEMainProcessor tester = new CTCAEMainProcessor(outputURI, ctcaeFile, meddraLLT, meddraSOC);

		
		
		} catch (URISyntaxException e) {
			System.out.println("Output location not valid URI");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error reading from source files");
			e.printStackTrace();
		}
	}
	

	
	public CTCAEMainProcessor(URI outputURI, File ctcaeFile, File meddraLLT, File meddraSOC) throws Exception{
		ontology = new CTCAE_Ontology(outputURI, namespace);
		this.messages = new Messages("./config/");
		HashMap<String, HashMap<String, String>> ctcaeFileData = new CTCAE_parser(ctcaeFile).getRowData();		
		meddraLLTData = new MedDRA_LLT_parser(meddraLLT).getLltLookUp();
		MedDRA_SOC = new MedDRA_SOC_parser(meddraSOC).getSocLookUp();
		createConcepts(ctcaeFileData, meddraLLTData);
		ontology.saveOntology(outputURI);
	}
	
	private void createConcepts(HashMap<String, HashMap<String, String>> ctcaeFileData,
			HashMap<String, Vector<MedDRA_LLT>> meddraFileData) {
		Set<String> conceptCodes = ctcaeFileData.keySet();
		for (String conceptCode : conceptCodes) {
			HashMap<String, String> properties = ctcaeFileData.get(conceptCode);
			URI conceptURI = createURI(conceptCode);
			String conceptName = conceptCode.replace("_", " ");
			ConceptProxy concept = ontology.createConcept(conceptURI, conceptName);
			Set<String> propertyCodes = properties.keySet();
			for (String propertyCode : propertyCodes) {
				Vector<Property> props;
				try {

					if (propertyCode.startsWith("Grade")){
						processGrade(propertyCode, properties.get(propertyCode), concept);
					} else if (propertyCode.equals(messages.getString("MedDRA_SOC"))) {
						createSubClassRestriction(concept, properties.get(propertyCode));
					}else {
					props = parseProperty(propertyCode, properties.get(propertyCode));

					// URI propURI = createURI(propertyCode);
					// Property prop = new Property(propURI, propertyCode ,
					// properties.get(propertyCode), ontology);
					for (Property prop : props) {
//						ontology.addAnnotationPropertyToConcept(conceptURI, prop);
						concept.addProperty(prop);
					}}
				} catch (PropertyException e) {
					System.out.println("Problem adding property " + propertyCode + " to concept " + conceptCode);
					e.printStackTrace();
				}
			}
			ontology.assignLabel(concept.getURI(), concept.getProperty("Preferred_Name").getValue());
		}
	}
	




	public URI createURI(String code){
		String finalCode = code.replace(" ", "_");
//		finalCode.replaceAll(",", "_");
		return URI.create(namespace+"#"+finalCode.replaceAll(",_", "_"));
	}
	
	private Vector<Property> parseProperty(String propCode, String propertyValue) throws PropertyException{

		Vector<Property> props= new Vector<Property>();
		if(propertyValue.length()==0){
			return props;
		}
		if(propCode.equals(messages.getString("id"))){
			//assign 
		}
		if(propCode.equals(messages.getString("MedDRA_Code"))){
			//assign 

			return getLLTs(propertyValue);
			
		}
		if(propCode.equals(messages.getString("Definition"))){
			
			URI propURI = createURI("DEFINITION");
			Property prop = new Property(propURI, "DEFINITION",propertyValue, ontology);
			URI qualURI = createURI("def-source");
			Qualifier qual = new Qualifier(qualURI, "def-source", "NCI", ontology);
			prop.addQualifier(qual);
			props.add(prop);
			return props;
		}
		if(propCode.equals(messages.getString("Preferred_Name"))){
			//assign PN and Full_Syn
			URI propURI = createURI("Preferred_Name");
			Property prop = new Property(propURI, "Preferred_Name",propertyValue, ontology);
			props.add(prop);
			propURI = createURI("FULL_SYN");
			Property prop2 = new Property(propURI, "FULL_SYN",propertyValue, ontology);
			URI qualURI = createURI("term-group");
			Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", ontology);
			prop2.addQualifier(qual1);
			qualURI = createURI("term-source");
			Qualifier qual2 = new Qualifier(qualURI, "term-source", "NCI", ontology);
			prop2.addQualifier(qual2);
			props.add(prop2);
			//TODO create rdfsLabel
			return props;
		}
		if(propCode.equals(messages.getString("code"))){
			URI propURI = createURI("code");
			Property prop = new Property(propURI, "code",propertyValue, ontology);
			props.add(prop);
			return props;
		}
		if(propCode.equals(messages.getString("NCIt_Code"))){
			URI propURI = createURI("NCIt_Code");
			Property prop = new Property(propURI, "NCIt_Code",propertyValue, ontology);
			props.add(prop);
			return props;
		}
		if(propCode.equals(messages.getString("Navigational_Note"))){
			URI propURI = createURI("Navigational_Note");
			Property prop = new Property(propURI, "Navigational_Note",propertyValue, ontology);
			props.add(prop);
			return props; 
		}
		if(propCode.equals(messages.getString("MedDRA_SOC"))){
			URI propURI = createURI("MedDRA_SOC");
			Property prop = new Property(propURI, "MedDRA_SOC",propertyValue, ontology);
			props.add(prop);
			return props;
		}
		return props;
	}
	
	private Vector<Property> getLLTs(String propertyValue) throws PropertyException {
		//Check the meddra data and find LLTs for the code
		Vector<Property> props = new Vector<Property>();
		URI propURI = createURI("MedDRA_Code");
		Property prop = new Property(propURI, "MedDRA_Code",propertyValue, ontology);
		props.add(prop);
		Vector<MedDRA_LLT> meds = 	meddraLLTData.get(propertyValue);
		if(meds == null){
			return props;
		}
		for(MedDRA_LLT med:meds){
			propURI = createURI("FULL_SYN");
			Property prop2 = new Property(propURI, "FULL_SYN",med.getLLT(), ontology);
			URI qualURI = createURI("term-group");
			Qualifier qual1 = new Qualifier(qualURI, "term-group", "LLT", ontology);
			prop2.addQualifier(qual1);
			qualURI = createURI("term-source");
			Qualifier qual2 = new Qualifier(qualURI, "term-source", "MedDRA", ontology);
			prop2.addQualifier(qual2);
			Qualifier qual3 = new Qualifier(qualURI, "source-code", med.getLltCode(), ontology);
			prop2.addQualifier(qual3);
			props.add(prop2);
		}
		return props;
	}



	private void processGrade(String propertyCode, String propertyValue, ConceptProxy concept) throws PropertyException{
		//Create Grade subconcepts for concept
		/**
		 *     <owl:Class rdf:about="#Grade_1_Abdominal_distension">
        <rdfs:label rdf:datatype="&xsd;string"
            >Grade 1 Abdominal distension</rdfs:label>
        <rdfs:subClassOf rdf:resource="#Abdominal_distension"/>
        <rdfs:subClassOf>
            <owl:Restriction>
                <owl:onProperty rdf:resource="#Is_Grade"/>
                <owl:allValuesFrom rdf:resource="#Grade_1_Adverse_Event"/>
            </owl:Restriction>
        </rdfs:subClassOf>
        <DEFINITION rdf:parseType="Literal"><ncicp:ComplexDefinition><ncicp:def-source>NCI</ncicp:def-source><ncicp:def-definition>Asymptomatic; clinical or diagnostic observations only; intervention not indicated</ncicp:def-definition></ncicp:ComplexDefinition></DEFINITION>
        <FULL_SYN rdf:parseType="Literal"><ncicp:ComplexTerm><ncicp:term-name>Grade 1 Abdominal distension</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>CTCAE</ncicp:term-source></ncicp:ComplexTerm></FULL_SYN>
        <code rdf:datatype="&xsd;string">E10457</code>
        <Preferred_Name rdf:datatype="&xsd;string"
            >Grade 1 Abdominal distension</Preferred_Name>
    </owl:Class>
		 */
		if(propertyValue.length()==0){
			return;
		}
		String gradeID = propertyCode.replace(" ", "_") +"_"+ concept.getCode();
		String gradeLabel = gradeID.replace("_", " ");
		ConceptProxy gradeConcept = ontology.createConcept(createURI(gradeID),gradeLabel);
		//Add subClassOf to original concept

		gradeConcept.addParent(concept.getURI());
		//Create Definition
		URI propURI = createURI("DEFINITION");
		Property prop = new Property(propURI, "DEFINITION",propertyValue, ontology);
		URI qualURI = createURI("def-source");
		Qualifier qual = new Qualifier(qualURI, "def-source", "NCI", ontology);
		prop.addQualifier(qual);
		//create Preferred_Name

		gradeConcept.addProperty(prop);
		propURI = createURI("Preferred_Name");
		Property prop2 = new Property(propURI, "Preferred_Name",gradeLabel, ontology);
		gradeConcept.addProperty(prop2);

		//Create PT
		propURI = createURI("FULL_SYN");
		Property prop3 = new Property(propURI, "FULL_SYN",gradeLabel, ontology);
		qualURI = createURI("term-group");
		Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", ontology);
		prop3.addQualifier(qual1);
		qualURI = createURI("term-source");
		Qualifier qual2 = new Qualifier(qualURI, "term-source", "NCI", ontology);
		prop3.addQualifier(qual2);
		gradeConcept.addProperty(prop3);

		//TODO Add restriction pointed at Grade tree
		RoleProxy rp = new RoleProxy(createURI("Is_Grade"),ontology);
		if(gradeLabel.startsWith("Grade 1")){
			Role role = new Role(rp,gradeConcept,ontology.getConcept(createURI("Grade_1_Adverse_Event")));
			gradeConcept.addRole(role);
		}else if (gradeLabel.startsWith("Grade 2")){
			Role role = new Role(rp,gradeConcept,ontology.getConcept(createURI("Grade_2_Adverse_Event")));
			gradeConcept.addRole(role);
		}else if (gradeLabel.startsWith("Grade 3")){
			Role role = new Role(rp,gradeConcept,ontology.getConcept(createURI("Grade_3_Adverse_Event")));
			gradeConcept.addRole(role);
		}else if (gradeLabel.startsWith("Grade 4")){
			Role role = new Role(rp,gradeConcept,ontology.getConcept(createURI("Grade_4_Adverse_Event")));
			gradeConcept.addRole(role);
		}else if (gradeLabel.startsWith("Grade 5")){
			Role role = new Role(rp,gradeConcept,ontology.getConcept(createURI("Grade_5_Adverse_Event")));
			gradeConcept.addRole(role);
		}else {
			System.out.println("Invalid grade for "+ concept.getCode());
		}
	}

	private void createSubClassRestriction(ConceptProxy concept, String string) {
		// Check if SOC concept exists.  If not, create it.  Then create subClassOf restriction
		ConceptProxy socConcept = ontology.createSOCConcept(createURI(string), string,MedDRA_SOC.get(string));
//		if(socConcept == null){
//			ontology.createConcepts(createURI(string), string);
//		}
//		ontology.addParent(concept.getConcept(), socConcept.getConcept());
		concept.addParent(socConcept.getConcept());
		
	}
}
