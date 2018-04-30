package gov.nih.nci.evs.ctcae;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import gov.nih.nci.evs.ctcae.data.CTCAE_Hierarchy_Parser;
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
import gov.nih.nci.evs.owl.proxy.RoleProxy;

public class CTCAEMainProcessor {

	private CTCAE_Ontology ontology;
	static String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/ctcae5.owl";
	private HashMap<String, Vector<MedDRA_LLT>> meddraLLTData = new HashMap<>();
	private HashMap<String, Vector<MedDRA_LLT>> meddraMedCodeData = new HashMap<>();
	private HashMap<String, String> MedDRA_SOC = new HashMap<>();
	private HashMap<String, String> MedDRA_Code = new HashMap<>();
	TreeMap<String, String> hierarchy = new TreeMap<String,String>();
	CTCAE_parser ctcaeParser = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		try {
			String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
			URI outputURI = new URI("file:///" + currentPath + "/ctcae5.owl");
			File ctcaeFile = new File(args[0]);

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

	public CTCAEMainProcessor(URI outputURI, File ctcaeFile, File meddraLLT, File meddraSOC) throws Exception {
		ontology = new CTCAE_Ontology(outputURI, namespace);
		
		ctcaeParser = new CTCAE_parser(ctcaeFile);
		
		hierarchy = new CTCAE_Hierarchy_Parser(ctcaeFile).getHierarchy();
		meddraLLTData = new MedDRA_LLT_parser(meddraLLT).getLltLookUp();
		meddraMedCodeData = new MedDRA_LLT_parser(meddraLLT).getMedLookUp();
		MedDRA_SOC = new MedDRA_SOC_parser(meddraSOC).getSocLookUp();
		createConcepts(meddraLLTData);
//		ontology.sortConceptsBySOC();
		ontology.saveOntology(outputURI);
	}

	private void createConcepts(
			HashMap<String, Vector<MedDRA_LLT>> meddraFileData) {

		Iterator<String> conceptCodes = ctcaeParser.getRowData().keySet().iterator();
		while (conceptCodes.hasNext()) {
			String conceptCode = conceptCodes.next();
			TreeMap<String, String> properties = ctcaeParser.getRowData().get(conceptCode);
			URI conceptURI = createURI(conceptCode);
			String conceptName = properties.get(CTCAE_parser.messages.getString("Preferred_Name"));
			if (!(conceptName == null)) {

				createConcept(conceptCode, properties);
			} else { createNavigationalNode(conceptCode, properties.get(CTCAE_parser.messages.getString("NCI_PT")));
				}
		}
	}
	
	private ConceptProxy createConcept(String conceptCode, TreeMap<String, String> properties){
		if(ontology.conceptExists(createURI(conceptCode))){
			return ontology.getConcept(createURI(conceptCode));
		}
		URI conceptURI = createURI(conceptCode);
		String conceptName = properties.get("CTCAE PT");
		System.out.println("Creating concept " + conceptURI.toString());
		if(conceptCode.equals("C144077")){
			String debug = "Stop here";
		}
		ConceptProxy concept = ontology.createConcept(conceptURI, conceptName);
		String parent = findParent(conceptCode);
		createSubClassRestriction(concept,parent);
		Set<String> propertyCodes = properties.keySet();
		for (String propertyCode : propertyCodes) {
			Vector<Property> props = new Vector<>();
			try {

//				if (propertyCode.equals(CTCAE_parser.messages.getString("MedDRA_SOC"))) {
////					createSubClassRestriction(concept, properties.get(propertyCode));
//					props = parseProperty(propertyCode, properties.get(propertyCode));
//				} else {
					props = parseProperty(propertyCode, properties.get(propertyCode));

					// URI propURI = createURI(propertyCode);
					// Property prop = new Property(propURI,
					// propertyCode ,
					// properties.get(propertyCode), ontology);
//				}
				for (Property prop : props) {
					// ontology.addAnnotationPropertyToConcept(conceptURI,
					// prop);
					concept.addProperty(prop);
				}
			} catch (PropertyException e) {
				System.out.println("Problem adding property " + propertyCode + " to concept " + conceptCode);
				e.printStackTrace();
			}
		}
		ontology.assignRdfLabelToConcept(concept.getURI(), concept.getProperty("Preferred_Name").getValue());
//		try {
//			processGrade(concept);
//		} catch (PropertyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return concept;
	}

	private String findParent(String conceptCode) {
		// TODO Auto-generated method stub
		return hierarchy.get(conceptCode);
	}

	private ConceptProxy createNavigationalNode(String conceptCode, String conceptName) {
		System.out.println("Concept has no name " + conceptCode);
		URI conceptURI = createURI(conceptCode);
		System.out.println("Creating concept " + conceptURI.toString());
		ConceptProxy concept = ontology.createConcept(conceptURI, conceptName);
		String parent = findParent(conceptCode);
		if(parent==null){
			System.out.println("Root Node");
		} else {
		createSubClassRestriction(concept,parent);
		try {
			Vector<Property> props = parseProperty(CTCAE_parser.messages.getString("NCI_PT"), conceptName);
			for (Property prop : props) {
				// ontology.addAnnotationPropertyToConcept(conceptURI,
				// prop);
				concept.addProperty(prop);
			}
			URI propURI = createURI("Preferred_Name");
			String preferredName = conceptName;
			if(preferredName.contains(", CTCAE")){
				preferredName = preferredName.substring(0, preferredName.lastIndexOf(","));
			}
			Property prop = new Property(propURI, "Preferred_Name", preferredName, ontology);
			concept.addProperty(prop);
			
			propURI = createURI("NCIt_Code");
			Property propCode = new Property(propURI, "NCIt_Code", conceptCode, ontology);
			concept.addProperty(propCode);
			ontology.assignRdfLabelToConcept(concept.getURI(), concept.getProperty("Preferred_Name").getValue());
			return concept;
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} return null;
	}

	public URI createURI(String code) {
		String finalCode = code.replace(" ", "_");
		// finalCode.replaceAll(",", "_");
		return URI.create(namespace + "#" + finalCode.replaceAll(",_", "_"));
	}

	private Vector<Property> parseProperty(String propCode, String propertyValue) throws PropertyException {

		Vector<Property> props = new Vector<>();
		if (propertyValue.length() == 0)
			return props;
		if (propCode.equals(CTCAE_parser.messages.getString("id"))) {
			// assign
		}
		if (propCode.equals(CTCAE_parser.messages.getString("MedDRA_Code"))) {
			// assign

			// return getLLTs(propertyValue);
			URI propURI = createURI("MedDRA_Code");
			Property prop = new Property(propURI, "MedDRA_Code", propertyValue, ontology);
			props.add(prop);
			return props;

		}
		if (propCode.equals(CTCAE_parser.messages.getString("Definition"))) {

			URI propURI = createURI("DEFINITION");

			URI qualURI = createURI("def-source");
			Vector<Qualifier> quals = new Vector<>();
			Qualifier qual = new Qualifier(qualURI, "def-source", "CTCAE_v5", ontology);
			quals.add(qual);
			Property prop = new Property(propURI, "DEFINITION", propertyValue, ontology, quals);
			props.add(prop);
			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("NCI_Def"))) {
			URI propURI = createURI("ALT_DEFINITION");

			URI qualURI = createURI("def-source");
			Vector<Qualifier> quals = new Vector<>();
			Qualifier qual = new Qualifier(qualURI, "def-source", "NCI", ontology);
			quals.add(qual);
			Property prop = new Property(propURI, "ALT_DEFINITION", propertyValue, ontology, quals);
			props.add(prop);
			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("Preferred_Name"))) {
			// assign PN and Full_Syn
			URI propURI = createURI("Preferred_Name");
			Property prop = new Property(propURI, "Preferred_Name", propertyValue, ontology);
			props.add(prop);

			propURI = createURI("FULL_SYN");
			Vector<Qualifier> quals = new Vector<>();
			URI qualURI = createURI("term-group");
			Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", ontology);
			quals.add(qual1);
			qualURI = createURI("term-source");
			Qualifier qual2 = new Qualifier(qualURI, "term-source", "CTCAE_v5", ontology);
			quals.add(qual2);
			Property prop2 = new Property(propURI, "FULL_SYN", propertyValue, ontology, quals);
			props.add(prop2);
			// TODO create rdfsLabel

			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("NCI_PT"))) {
			URI propURI = createURI("FULL_SYN");
			Vector<Qualifier> quals = new Vector<>();
			URI qualURI = createURI("term-group");
			Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", ontology);
			quals.add(qual1);
			qualURI = createURI("term-source");
			Qualifier qual2 = new Qualifier(qualURI, "term-source", "NCI", ontology);
			quals.add(qual2);
			Property prop2 = new Property(propURI, "FULL_SYN", propertyValue, ontology, quals);
			props.add(prop2);
			// TODO create rdfsLabel
			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("code"))) {
			URI propURI = createURI("code");
			Property prop = new Property(propURI, "code", propertyValue, ontology);
			props.add(prop);
			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("NCIt_Code"))) {
			URI propURI = createURI("NCIt_Code");
			Property prop = new Property(propURI, "NCIt_Code", propertyValue, ontology);
			props.add(prop);
			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("Navigational_Note"))) {
			URI propURI = createURI("Navigational_Note");
			Property prop = new Property(propURI, "Navigational_Note", propertyValue, ontology);
			props.add(prop);
			return props;
		}
		if (propCode.equals(CTCAE_parser.messages.getString("MedDRA_SOC"))) {
			URI propURI = createURI("MedDRA_SOC");
			Property prop = new Property(propURI, "MedDRA_SOC", propertyValue, ontology);
			props.add(prop);
			return props;
		}
		return props;
	}

	private Vector<Property> getLLTs(String propertyValue) throws PropertyException {
		// Check the meddra data and find LLTs for the code
		Vector<Property> props = new Vector<>();
		URI propURI = createURI("MedDRA_Code");
		Property prop = new Property(propURI, "MedDRA_Code", propertyValue, ontology);
		props.add(prop);
		Vector<MedDRA_LLT> meds = meddraLLTData.get(propertyValue);
		if (meds == null)
			return props;
		for (MedDRA_LLT med : meds) {
			propURI = createURI("FULL_SYN");

			Vector<Qualifier> quals = new Vector<>();
			URI qualURI = createURI("term-group");
			Qualifier qual1 = new Qualifier(qualURI, "term-group", "LLT", ontology);
			quals.add(qual1);
			qualURI = createURI("term-source");
			Qualifier qual2 = new Qualifier(qualURI, "term-source", "MedDRA", ontology);
			quals.add(qual2);
			Qualifier qual3 = new Qualifier(qualURI, "source-code", med.getLltCode(), ontology);
			quals.add(qual3);
			Property prop2 = new Property(propURI, "FULL_SYN", med.getLLT(), ontology, quals);
			props.add(prop2);
		}
		return props;
	}

	private void processGrade(ConceptProxy gradeConcept)
			throws PropertyException {
		// Create Grade subconcepts for concept
		/**
		 * <owl:Class rdf:about="#Grade_1_Abdominal_distension">
		 * <rdfs:label rdf:datatype="&xsd;string" >Grade 1 Abdominal
		 * distension</rdfs:label>
		 * <rdfs:subClassOf rdf:resource="#Abdominal_distension"/>
		 * <rdfs:subClassOf> <owl:Restriction>
		 * <owl:onProperty rdf:resource="#Is_Grade"/>
		 * <owl:allValuesFrom rdf:resource="#Grade_1_Adverse_Event"/>
		 * </owl:Restriction> </rdfs:subClassOf> <DEFINITION rdf:parseType=
		 * "Literal"><ncicp:ComplexDefinition><ncicp:def-source>NCI</ncicp:def-source><ncicp:def-definition>Asymptomatic;
		 * clinical or diagnostic observations only; intervention not
		 * indicated</ncicp:def-definition></ncicp:ComplexDefinition></DEFINITION>
		 * <FULL_SYN rdf:parseType=
		 * "Literal"><ncicp:ComplexTerm><ncicp:term-name>Grade 1 Abdominal
		 * distension</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>CTCAE</ncicp:term-source></ncicp:ComplexTerm></FULL_SYN>
		 * <code rdf:datatype="&xsd;string">E10457</code>
		 * <Preferred_Name rdf:datatype="&xsd;string" >Grade 1 Abdominal
		 * distension</Preferred_Name> </owl:Class>
		 */
//		if (propertyValue.length() == 0)
//			return;
//		String gradeID = propertyCode.replace(" ", "_") + "_" + concept.getCode();
//		String gradeLabel = gradeID.replace("_", " ");
//		ConceptProxy gradeConcept = ontology.createConcept(createURI(gradeID), gradeLabel);
//		// Add subClassOf to original concept
//
//		gradeConcept.addParent(concept.getURI());
//		// Create Definition
//		URI propURI = createURI("DEFINITION");
//		Vector<Qualifier> quals = new Vector<>();
//		URI qualURI = createURI("def-source");
//		Qualifier qual = new Qualifier(qualURI, "def-source", "NCI", ontology);
//		quals.add(qual);
//		Property prop = new Property(propURI, "DEFINITION", propertyValue, ontology, quals);
//		gradeConcept.addProperty(prop);
//
//		// create Preferred_Name
//		gradeConcept.addProperty(prop);
//		propURI = createURI("Preferred_Name");
//		Property prop2 = new Property(propURI, "Preferred_Name", gradeLabel, ontology);
//		gradeConcept.addProperty(prop2);
//
//		// Create PT
//		propURI = createURI("FULL_SYN");
//		quals = new Vector<>();
//		qualURI = createURI("term-group");
//		Qualifier qual1 = new Qualifier(qualURI, "term-group", "PT", ontology);
//		quals.add(qual1);
//		qualURI = createURI("term-source");
//		Qualifier qual2 = new Qualifier(qualURI, "term-source", "NCI", ontology);
//		quals.add(qual2);
//		Property prop3 = new Property(propURI, "FULL_SYN", gradeLabel, ontology);
//		gradeConcept.addProperty(prop3);

		// TODO Add restriction pointed at Grade tree
		if(gradeConcept.getName().startsWith("Grade")){
			String gradeLabel = gradeConcept.getName();
		RoleProxy rp = new RoleProxy(createURI("Is_Grade"), ontology);
		if (gradeLabel.startsWith("Grade 1")) {
			Role role = new Role(rp, gradeConcept, ontology.getConcept(createURI("Grade_1_Adverse_Event")));
			gradeConcept.addRole(role);
		} else if (gradeLabel.startsWith("Grade 2")) {
			Role role = new Role(rp, gradeConcept, ontology.getConcept(createURI("Grade_2_Adverse_Event")));
			gradeConcept.addRole(role);
		} else if (gradeLabel.startsWith("Grade 3")) {
			Role role = new Role(rp, gradeConcept, ontology.getConcept(createURI("Grade_3_Adverse_Event")));
			gradeConcept.addRole(role);
		} else if (gradeLabel.startsWith("Grade 4")) {
			Role role = new Role(rp, gradeConcept, ontology.getConcept(createURI("Grade_4_Adverse_Event")));
			gradeConcept.addRole(role);
		} else if (gradeLabel.startsWith("Grade 5")) {
			Role role = new Role(rp, gradeConcept, ontology.getConcept(createURI("Grade_5_Adverse_Event")));
			gradeConcept.addRole(role);
		} else {
			System.out.println("Invalid grade for " + gradeConcept.getCode());
		}}
	}

	private void createSubClassRestriction(ConceptProxy concept, String code) {
		if (code.equals("C146699")) {
			String debug = "Stop here";
		}
		// Check if SOC concept exists. If not, create it. Then create
		// subClassOf restriction
//		String medDRA_SOC = MedDRA_SOC.get(code);
//		if (medDRA_SOC == null) {
//			Vector<MedDRA_LLT> llts = meddraMedCodeData.get(code);
//			if (!(llts == null)) {
//				medDRA_SOC = llts.get(0).getLLT();
//			} else {
//				System.out.println("No LLT for " + code);
//			}
//		}
		if (ontology.conceptExists(createURI(code))) {
			// ConceptProxy socConcept = ontology.getConcept(createURI(code));
			concept.addParent(createURI(code));
		}

		else {
			if(ctcaeParser.getRowData().get(code).size()==10){
			ConceptProxy conceptParent = createConcept(code, ctcaeParser.getRowData().get(code));
			concept.addParent(conceptParent.getURI());
			} else {
			ConceptProxy socConcept = createNavigationalNode(code,ctcaeParser.socName(code) );
//			ConceptProxy socConcept = ontology.createSOCConcept(createURI(code), medDRA_SOC, code);
			concept.addParent(socConcept.getURI());}
		}
		// if(socConcept == null){
		// ontology.createConcepts(createURI(string), string);
		// }
		// ontology.addParent(concept.getConcept(), socConcept.getConcept());
		// concept.addParent(socConcept.getConcept().getURI());

	}
	
	private void arrangeSocHierarchy(HashMap<String, Vector<ConceptProxy>> conceptsBySOC){
		//take the conceptsBySOC and check against SOC.  
		//If not there, check against LLT. Create a structure.
		//Then put the CTCAE concepts with grades as children
		//Each concept should have only one parent.
		Iterator<String> iter = conceptsBySOC.keySet().iterator();
		String socCode = iter.next();
		if(this.MedDRA_SOC.containsKey(socCode)){
			//Create SOC concept (check if unique)
			//Create subClass Of to SOC concept from main
			//Create subClass Of from Grades to main
		} else if (this.meddraLLTData.containsKey(socCode)){
			
		} else {
			
		}
		
	}
}
