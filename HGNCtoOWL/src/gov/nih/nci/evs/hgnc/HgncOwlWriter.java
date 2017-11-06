package gov.nih.nci.evs.hgnc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

//import org.coode.owl.rdf.rdfxml.RDFXMLOntologyStorer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
//import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
//import org.semanticweb.owlapi.io.WriterOutputTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
//import org.semanticweb.owlapi.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.model.OWLSubClassAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
//import org.semanticweb.owlapi.model.OWLTypedConstant;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
//import org.semanticweb.owlapi.util.SimpleURIMapper;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLStorer;

public class HgncOwlWriter {

	/** The manager. */
	private OWLOntologyManager manager;

	/** The ontology. */
	private OWLOntology ontology;

	private URI saveURI;
	private IRI ontologyIRI;
	private OWLDataFactory factory;
	String typeConstantString = "http://www.w3.org/2001/XMLSchema#string";

	/** The type constant literal. */
	private final String typeConstantLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

	public HgncOwlWriter(HgncOntology hugoOntology, URI inSaveURI) {
		try {
			// this.saveURI = new URI(
			// "file:///C:/Documents%20and%20Settings/wynner/workspace/HugoToOwl/Hugo.owl");
			this.saveURI = inSaveURI;
			this.manager = OWLManager.createOWLOntologyManager();
//			this.manager = OntologyManagement.ontology.getOWLOntologyManager();
			ontologyIRI = IRI
			        .create("http://ncicb.nci.nih.gov/genenames.org/HGNC.owl");
//			SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, saveURI);
//			manager.addIRIMapper(mapper);

			this.ontology = manager.createOntology(ontologyIRI);
			factory = manager.getOWLDataFactory();
			createHierarchyConcepts(hugoOntology.getLocusHierarchy());
			createConcepts(hugoOntology);

			saveOntology();
			URI owl2 = URI.create(this.saveURI + "2");
			System.out.print(owl2.toString());
			
			manager.saveOntology(ontology, IRI.create(saveURI));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem creating OWL output.");
			System.exit(0);
		}
	}

	private void createHierarchyConcepts(HashMap<String, String> locusHierarchy) {
		Set<String> locusTypes = locusHierarchy.keySet();
		Vector<String> locusGroups = new Vector<String>();
		for (String locusType : locusTypes) {
			String locusGroup = locusHierarchy.get(locusType);
			if (!locusGroups.contains(locusGroup)) {
				locusGroups.add(locusGroup);
			}
			OWLClass locusTypeParent = factory
			        .getOWLClass(createIRI(locusType));
			OWLClass locusGroupParent = factory
			        .getOWLClass(createIRI(locusGroup));
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(locusTypeParent,
			        locusGroupParent);
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			try {
				manager.applyChange(addAxiom);
			} catch (Exception e) {
				System.out.println("Error adding axiom :" + locusType);
			}
		}

		for (String locusGroup : locusGroups) {
			OWLClass locusGroupParent = factory
			        .getOWLClass(createIRI(locusGroup));
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(locusGroupParent,
			        factory.getOWLThing());
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			try {
				manager.applyChange(addAxiom);
			} catch (Exception e) {
				System.out.println("Error adding axiom :" + locusGroup);
			}
		}

	}

	private void createConcepts(HgncOntology hugoOntology) {
		Vector<HgncConcept> conceptVector = hugoOntology.getConcepts();
		for (HgncConcept concept : conceptVector) {
			// Assume hierarchy is unknown
			// OWLClass clz = factory.getOWLClass(URI.create(ontologyURI
			// + concept.code));
			OWLClass clz = factory.getOWLClass(createIRI(concept.code.replace(
			        ":", "_")));
			OWLClass parent = factory
			        .getOWLClass(createIRI(concept.getParent()));
			// OWLAxiom axiom = factory.getOWLSubClassAxiom(clz, factory
			// .getOWLThing());
			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clz, parent);
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			try {
				manager.applyChange(addAxiom);
			} catch (Exception e) {
				System.out.println("Error adding axiom :" + concept.code);
			}
			addSimpleProperties(concept.getSimpleProperties(), addAxiom);
			addDelimitedProperties(concept.getDelimitedProperties(), addAxiom);
//			addSpecializedProperties(concept, addAxiom);
			addLSDBhyperlinks(concept, addAxiom);
//			addLRG_ID(concept, addAxiom);
//			addlsdbProperties(concept);

		}
	}

//	private void addSpecializedProperties(HgncConcept concept, AddAxiom ax) {
//		OWLEntity ent = null;
//		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();
//		// for (OWLEntity e : ax.getEntities()) {
//		// if (e != factory.getOWLThing()) {
//		// ent = e;
//		// break;
//		// }
//		// }
//		ent = subAx.getSubClass().asOWLClass();
//		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
//		OWLDatatype odt = factory.getOWLDatatype(IRI
//		        .create(typeConstantLiteral));
//		Set<String> propNames = concept.getSpecialistDatabaseIds().keySet();
//		for (String prop : propNames) {
//			// System.out.println("\tSpecialist_Database_Id\t" + prop + "\t"
//			// + concept.getSpecialistDatabaseIds().get(prop));
////			OWLTypedConstant otc = factory.getOWLTypedConstant(concept
////			        .getSpecialistDatabaseIds().get(prop), odt);
////			OWLAnnotation anno = factory.getOWLConstantAnnotation(
////			        createIRI(prop + "_ID"), otc);
//			
//			
//			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI(prop+"_ID"));
//		    OWLAnnotation anno = factory.getOWLAnnotation(
//		        aProp, factory.getOWLLiteral(concept
//				        .getSpecialistDatabaseIds().get(prop)));
////			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
////			        ent, anno);
////			
//			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
//			        ent.getIRI(), anno);
//			
//
//			
//			changes.add(new AddAxiom(ontology, ax1));
//		}
//
//		propNames = concept.getSpecialistDatabaseLinks().keySet();
//		for (String prop : propNames) {
//			String value = concept.getSpecialistDatabaseLinks().get(prop);
//			value = value.replace("> <", " ");
//			value = value.trim();
//			// System.out.println("\tSpecialist_Database_Link\t" + prop + "\t"
//			// + value);
////			OWLTypedConstant otc = factory.getOWLTypedConstant(value, odt);
////			OWLAnnotation anno = factory.getOWLConstantAnnotation(
////			        createIRI(prop + "_LINK"), otc);
//			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI(prop+"_LINK"));
////			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
////			        ent, anno);
//		    OWLAnnotation anno = factory.getOWLAnnotation(
//			        aProp, factory.getOWLLiteral(concept
//					        .getSpecialistDatabaseIds().get(prop)));
//			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
//			        ent.getIRI(), anno);
//			
//			changes.add(new AddAxiom(ontology, ax1));
//		}
//
//		if (!changes.isEmpty()) {
//			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
//			        changes);
//			manager.applyChanges(list);
//		}
//
//	}
	
	private void addSpecializedProperties(HgncConcept concept, AddAxiom ax) {
		OWLEntity ent = null;
		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();
		ent = subAx.getSubClass().asOWLClass();
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLDatatype odt = factory.getOWLDatatype(IRI
		        .create(typeConstantLiteral));
		Set<String> props = concept.getLsdb();
		for (String prop : props) {
			
			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI("lsdb"));
		    OWLAnnotation anno = factory.getOWLAnnotation(
		        aProp, factory.getOWLLiteral(prop));		
			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
			        ent.getIRI(), anno);			
			changes.add(new AddAxiom(ontology, ax1));
		}


		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			manager.applyChanges(list);
		}

	}
	
	private void addLSDBhyperlinks(HgncConcept concept, AddAxiom ax){
		OWLEntity ent = null;
		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();
		ent = subAx.getSubClass().asOWLClass();
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLDatatype odt = factory.getOWLDatatype(IRI
		        .create(typeConstantLiteral));
		Set<String> props = concept.getLsdb();
		for (String prop : props) {
			
			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI("lsdb"));
			
			//create the URL
			String[] nvp = prop.split("\\|");
			String propID = nvp[0];
			String propLink = nvp[1];
			String propURL = "<a href=\""+propLink+"\" target=\"_blank\">"+propID+"</a>";
			
			
			
			
			
		    OWLAnnotation anno = factory.getOWLAnnotation(
		        aProp, factory.getOWLLiteral(propURL));		
			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
			        ent.getIRI(), anno);			
			changes.add(new AddAxiom(ontology, ax1));
		}


		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			manager.applyChanges(list);
		}
	}
	
	
	private void addLRG_ID(HgncConcept concept, AddAxiom ax) {
		OWLEntity ent = null;
		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();
		ent = subAx.getSubClass().asOWLClass();
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLDatatype odt = factory.getOWLDatatype(IRI
		        .create(typeConstantLiteral));
		Set<String> props = concept.getLsdb();
		for (String prop : props) {
			
			String[] nvp = prop.split("\\|");
			String propID = nvp[0];
			String propLink = nvp[1];

			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI("LRG_ID"));
		    OWLAnnotation anno = factory.getOWLAnnotation(
		        aProp, factory.getOWLLiteral(propID));
//			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
//			        ent, anno);
//			
			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
			        ent.getIRI(), anno);
			
			changes.add(new AddAxiom(ontology, ax1));

			
//			String value = concept.getSpecialistDatabaseLinks().get(prop);
//			value = value.replace("> <", " ");
//			value = value.trim();
			// System.out.println("\tSpecialist_Database_Link\t" + prop + "\t"
			// + value);
//			OWLTypedConstant otc = factory.getOWLTypedConstant(value, odt);
//			OWLAnnotation anno = factory.getOWLConstantAnnotation(
//			        createIRI(prop + "_LINK"), otc);
			OWLAnnotationProperty aProp_link = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI("LRG"));
//			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
//			        ent, anno);
		    OWLAnnotation anno_link = factory.getOWLAnnotation(
			        aProp_link, factory.getOWLLiteral(propLink));
			final OWLAxiom ax1_link = factory.getOWLAnnotationAssertionAxiom(
			        ent.getIRI(), anno_link);
			
			changes.add(new AddAxiom(ontology, ax1_link));
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			manager.applyChanges(list);
		}

	}
	
	
	
	private void addlsdbComplexProperties(HgncConcept concept) {

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (SpecialistDatabase ldsbEntry : concept.getSpecialistDatabase()) {
			Set<OWLAnnotation> annoAnnotations = new HashSet<OWLAnnotation>();
			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(createIRI("ldsb"));
			OWLAnnotation anno = factory.getOWLAnnotation(aProp,
					factory.getOWLLiteral(ldsbEntry.getDbURL().toString()));

			OWLAnnotationProperty dProp = this.manager.getOWLDataFactory()
					.getOWLAnnotationProperty(createIRI("DatabaseName"));
			if (ldsbEntry == null) {
				String debug = "stop";
			}
			OWLAnnotation newAnno = factory.getOWLAnnotation(dProp, factory.getOWLLiteral(ldsbEntry.getDbName()));
			annoAnnotations.add(newAnno);
			OWLAxiom annotatedAxiom = factory.getOWLAnnotationAssertionAxiom(createIRI(concept.code), anno,
					annoAnnotations);
			changes.add(new AddAxiom(this.ontology, annotatedAxiom));
		}

		if (changes.size() > 0) {
			this.manager.applyChanges(changes);
		}

	}

	private void addSimpleProperties(HashMap<String, String> properties,
	        AddAxiom ax) {

		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLEntity ent = null;
		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();

		ent = subAx.getSubClass().asOWLClass();
		OWLDatatype odt = factory
		        .getOWLDatatype(IRI.create(typeConstantString));
		Set<String> propNames = properties.keySet();
		for (String propName : propNames) {
//			OWLTypedConstant otc = factory.getOWLTypedConstant(
//			        properties.get(propName), odt);
//			OWLAnnotation anno = factory.getOWLConstantAnnotation(
//			        createIRI(propName), otc);
//			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
//			        ent, anno);
			
			
			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
					.getOWLAnnotationProperty(createIRI(propName));
		    OWLAnnotation anno = factory.getOWLAnnotation(
			        aProp, factory.getOWLLiteral(properties.get(propName)));
			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
			        ent.getIRI(), anno);
			
			
			changes.add(new AddAxiom(ontology, ax1));
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			manager.applyChanges(list);
		}
	}

	private void addDelimitedProperties(
	        HashMap<String, Vector<String>> properties, AddAxiom ax) {
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLEntity ent = null;
		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();
		// for (OWLEntity e : ax.getEntities()) {
		// if (e != factory.getOWLThing()) {
		// ent = e;
		// break;
		// }
		// }
		ent = subAx.getSubClass().asOWLClass();
		OWLDatatype odt = factory
		        .getOWLDatatype(IRI.create(typeConstantString));
		Set<String> propNames = properties.keySet();
		for (String propName : propNames) {
			Vector<String> propValueSet = properties.get(propName);
			for (String propValue : propValueSet) {
//				OWLTypedConstant otc = factory.getOWLTypedConstant(propValue,
//				        odt);
//				OWLAnnotation anno = factory.getOWLConstantAnnotation(
//				        createIRI(propName), otc);
//				OWLEntityAnnotationAxiom ax1 = factory
//				        .getOWLEntityAnnotationAxiom(ent, anno);
				
				
				OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
						.getOWLAnnotationProperty(createIRI(propName));
			    OWLAnnotation anno = factory.getOWLAnnotation(
				        aProp, factory.getOWLLiteral(propValue));
				final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
				        ent.getIRI(), anno);
				
				
				
				changes.add(new AddAxiom(ontology, ax1));
			}
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			manager.applyChanges(list);
		}
	}

	/**
	 * Creates an OWL uri from the ontology namespace and a class name
	 * 
	 * @param className
	 *            the class name
	 * 
	 * @return the uRI
	 */
	public IRI createIRI(String className) {
		String urlCompliantClassName = HgncToOwl.underscoredString(className);
		return IRI.create(ontologyIRI + "#" + urlCompliantClassName);
	}

	/**
	 * Save ontology to the file specified in the properties By default encodes
	 * to utf-8
	 */
	private void saveOntology() {
		try {
			RDFXMLStorer storer = new RDFXMLStorer();
			File newFile = new File(saveURI);
			FileOutputStream out = new FileOutputStream(newFile);
			WriterDocumentTarget target = new WriterDocumentTarget(
			        new BufferedWriter(new OutputStreamWriter(out, "UTF8")));
			OWLXMLDocumentFormat format = new OWLXMLDocumentFormat();
			storer.storeOntology(ontology, target, format);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
