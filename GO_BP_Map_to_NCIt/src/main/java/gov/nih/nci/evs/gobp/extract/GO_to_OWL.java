package gov.nih.nci.evs.gobp.extract;

import gov.nih.nci.evs.gobp.print.PrintOWL1;
import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.exceptions.QualifierException;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class GO_to_OWL {
	/**
	 * read in OBO file, or maybe OWL eventually, and convert to an OWL that
	 * meets our requirements
	 * 
	 * Note, our OWL will be changing to OWL2 eventually so the writing should
	 * be isolated so it can be swapped out when we are ready.
	 * 
	 * The main problem we have with the current GO owl is the funky synonyms.
	 * The hasExactSynonym, hasBroadSynonym, hasNarrowSynonym and
	 * hasRelatedSynonym could all be hasSynonym with a qualifier of exact,
	 * broad, narrow or related
	 * 
	 * export as the pretty print equivalent version of owl
	 * 
	 * For right now: 1. Read in go obo file 2. create go owl file 3. extract
	 * biological_process branch 4. remove all outgoing roles from BP branch 5.
	 * Save with date
	 * 
	 */

	final static private Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.extract.GO_to_OWL.class);

	private String goNamespace;
	OWLKb owlkb;
	private String nciNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";

	public String load(String fileLoc) {
		String owlFileLoc = "";
		String prettyOwl = "";
		String owlFileTemp = "";
		String inputString = fileLoc;
		owlFileLoc = inputString + ".owl";
		owlFileTemp = inputString + "_temp.owl";
		prettyOwl = inputString + "_pretty.owl";
		try {
			// owl1 version

			testOBOParser(inputString, owlFileLoc);

			if (!owlFileLoc.startsWith("file")) {
				owlFileLoc = "file://" + owlFileLoc;

			}
			owlkb = new OWLKb(owlFileLoc, goNamespace);

			// owl2 version
			// owlkb = new OWLKb(fileLoc, goNamespace);

			// necessary to have a base to which to add Deprecate
			owlkb.getAllConcepts();
			URI deprecatedBranchURI = new URI(goNamespace + "#Deprecated" );
			owlkb.setDeprecatedBranch(deprecatedBranchURI);

			boolean debug = owlkb.getHasLiterals();

			processHeader();

			// processDeclarations();

			processDeprecated();

			// createPTandPN();

			for (URI conceptCode : owlkb.getAllConcepts().keySet()) {
				processConcept(conceptCode);
			}

			owlkb.saveOntology(owlFileTemp);
			owlkb = new OWLKb(owlFileTemp, goNamespace);
			// owlkb.reloadOntology();

			logger.info("Printing OWLKb conversion ontology " + prettyOwl);
			new PrintOWL1(owlkb, prettyOwl);

		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument. File location must be a valid and absolute filepath.  Location passed in:"
					+ fileLoc);
			return null;
		} catch (NullPointerException e) {
			logger.error("Null Pointer. Cannot continue: ");
			e.printStackTrace();
			logger.error("Null pointer exception", e);
			System.exit(0);
		} catch (Exception e) {
			logger.error("Something bad", e);
		}
		return owlFileTemp;
		// return owlFileLoc;
	}

	private void processDeprecated() {
		// TODO Review all root concepts and see if they are deprecated
		// If they are, make them a subClass of the retired branch

		// boolean hasDeprecated = false;
		// check if there are any deprecated root concepts
		// TODO make deprecated concepts children of Deprecated

		// Check if the Deprecated concept already exists. If not, create it
		if (!owlkb.conceptExists(owlkb.getDeprecatedBranch())) {
			owlkb.createConcept(owlkb.getDeprecatedBranch(), goNamespace);
			logger.info("Created deprecated branch");
		}

		Vector<ConceptProxy> roots = owlkb.getRootConcepts();
		for (ConceptProxy root : roots) {
			if (root.isDeprecated()) {
				owlkb.addParent(root.getURI(), owlkb.getDeprecatedBranch());

				// hasDeprecated = true;
			}
		}
		owlkb.refreshRootNodes();
		// Remove the deprecated concepts from the root concept vector
		Vector<ConceptProxy> rootCheck = owlkb.getRootConcepts();
		// owlkb.removeBranch("Deprecated");
	}

	// private void createPTandPN(){
	// //TODO - take the rdfs:label and create a NCI|PT and PN
	// // Also create a GO|PT?
	// //See where extra label is coming from and remove it
	//
	//
	// }

	public GO_to_OWL(String go_Namespace) {
		goNamespace = go_Namespace;
	}

	private void processConcept(URI conceptCode) {
		ConceptProxy concept = owlkb.getConcept(conceptCode);
		if (conceptCode.toString().contains("GO_0003806")) {
			@SuppressWarnings("unused")
			String debug = "Stop Here";
		}

		Vector<Property> props = concept.getProperties();
		// System.out.println("Debug original props size " + props.size());
		// for (Property prop : props) {
		// // check for string FULL-SYN - for debugging. TODO remove at some
		// // point
		// if (prop.getName().equals("FULL_SYN")) {
		// prop.getValue();
		//
		// }
		// }

		// TODO take concept and convert it to a desired OWL form and output
		// discard anything not in the biological_process namespace? where not
		// is_obsolete=true
		// call process namespace, name, synonyms, defs, restrictions, subClass,
		// other properties
		try {
			Property PN = concept.getProperty("Preferred_Name");
			URI.create(nciNamespace+"#"+"Preferred_Name");
			if (PN == null || PN.getValue() == null) {
//				URI.create(nciNamespace+"#"+"Preferred_Name");
				PN = new Property(URI.create(nciNamespace+"#Preferred_Name"),
						"Preferred_Name",
						concept.getName(), this.owlkb);
				// PN.setDomain(nciNamespace);
				// props.add(PN);
				// owlkb.addPropertyToConcept(conceptCode, "Preferred_Name",
				// concept.getName());
				concept.addProperty(PN);
			} else {
				PN.setURI(URI.create(nciNamespace+"#Preferred_Name"));
			}

			Property FS = concept.getProperty("FULL_SYN");
			if (FS == null || FS.getValue() == null) {

				Vector<Qualifier> quals = new Vector<Qualifier>();

				Qualifier termType = new Qualifier("term-group", "PT");
				quals.add(termType);
				Qualifier termSource = new Qualifier("term-source", "NCI");
				quals.add(termSource);
				Property nciPT = new Property(URI.create(nciNamespace+"#FULL_SYN"),
						"FULL_SYN",
						concept.getName(),this.owlkb);
				nciPT.setQualifiers(quals);
				nciPT.setPropertyType(PropertyType.SYNONYM);
//				nciPT.setDomain(nciNamespace);
				// props.add(nciPT);
				// <FULL_SYN
				// rdf:parseType="Literal"><ncicp:ComplexTerm><ncicp:term-name><![CDATA[larval
				// behaviour]]></ncicp:term-name><ncicp:term-group><![CDATA[EXACT]]></ncicp:term-group></ncicp:ComplexTerm></FULL_SYN>
				// String propertyString =
				// "<ncicp:ComplexTerm><ncicp:term-name>"
				// + nciPT.getValue()
				// +
				// "</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source></ncicp:ComplexTerm>";
				// owlkb.addPropertyToConcept(conceptCode, "nci:FULL_SYN", //
				// propertyString);
				concept.addProperty(nciPT);
			} else {
				FS.setURI(URI.create(nciNamespace+"#FULL_SYN"));
			}
			// System.out.println("Debug props final size :" + props.size());
		} catch (QualifierException e) {

			logger.error("Unable to create PT for " + conceptCode);
		} catch (NullPointerException e) {
			logger.error("Unable to create PT for " + conceptCode);
		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument" + e);
		}

	}

	private void processHeader() {
		// TODO write out the header in proper form
		/*
		 * Most will be static but a few fields will be dynamic
		 * 
		 * format-version: 1.2 will become <oboInOwl:hasOBOFormatVersion
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string">1.2</oboInOwl:
		 * hasOBOFormatVersion>
		 * 
		 * data-version: releases/2015-02-27 will become <owl:versionIRI
		 * rdf:resource
		 * ="http://purl.obolibrary.org/obo/go/releases/2015-03-19/go.owl"/>
		 * 
		 * date: 25:02:2015 12:50 will become <oboInOwl:date
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string">18:03:2015
		 * 17:12</oboInOwl:date>
		 */
		// TODO Do we need this?
		owlkb.getNamespacePrefixes();
	}

	// private void processDeclarations() {
	// // TODO process the property declarations and output in a desired form
	// }

	private void testOBOParser(String fileLoc, String outputLoc) {
		OBOFormatParser p = new OBOFormatParser();
		OBODoc obodoc;
		try {

			obodoc = p.parse(fileLoc);

			Obo2Owl bridge = new Obo2Owl();
			OWLOntologyManager manager = bridge.getManager();
			OWLOntology ontology = bridge.convert(obodoc);

			logger.info("Saving OBOFormatParser ontology " + outputLoc);
			// manager.saveOntology(ontology, IRI.create(file));
			manager.saveOntology(ontology, IRI.create(outputLoc));
			logger.info("Completed saving to " + outputLoc);

		} catch (OBOFormatParserException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Could not parse OBO format ", e);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Could not create OWL ontology ", e);
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Unable to save OWL Ontology ", e);
		}

	}

	public static void main(String[] args) {
		String fileLoc = args[0];
		String goNamespace = args[1];
		String outputFileLoc = new GO_to_OWL(goNamespace).load(fileLoc);
		System.out.println("New file located at " + outputFileLoc);
	}
}
