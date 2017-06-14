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

import org.coode.owl.rdf.rdfxml.RDFXMLOntologyStorer;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLXMLOntologyFormat;
import org.semanticweb.owl.io.WriterOutputTarget;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.util.SimpleURIMapper;

public class HgncOwlWriter {

	/** The manager. */
	private OWLOntologyManager manager;

	/** The ontology. */
	private OWLOntology ontology;

	private URI saveURI;
	private URI ontologyURI;
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
			ontologyURI = URI
			        .create("http://ncicb.nci.nih.gov/genenames.org/HGNC.owl");
			SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, saveURI);
			manager.addURIMapper(mapper);

			this.ontology = manager.createOntology(ontologyURI);
			factory = manager.getOWLDataFactory();
			createHierarchyConcepts(hugoOntology.getLocusHierarchy());
			createConcepts(hugoOntology);

			saveOntology();
			URI owl2 = URI.create(this.saveURI + "2");
			System.out.print(owl2.toString());
			manager.saveOntology(ontology, owl2);

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
			        .getOWLClass(createURI(locusType));
			OWLClass locusGroupParent = factory
			        .getOWLClass(createURI(locusGroup));
			OWLAxiom axiom = factory.getOWLSubClassAxiom(locusTypeParent,
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
			        .getOWLClass(createURI(locusGroup));
			OWLAxiom axiom = factory.getOWLSubClassAxiom(locusGroupParent,
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
			OWLClass clz = factory.getOWLClass(createURI(concept.code.replace(
			        ":", "_")));
			OWLClass parent = factory
			        .getOWLClass(createURI(concept.getParent()));
			// OWLAxiom axiom = factory.getOWLSubClassAxiom(clz, factory
			// .getOWLThing());
			OWLAxiom axiom = factory.getOWLSubClassAxiom(clz, parent);
			AddAxiom addAxiom = new AddAxiom(ontology, axiom);
			try {
				manager.applyChange(addAxiom);
			} catch (Exception e) {
				System.out.println("Error adding axiom :" + concept.code);
			}
			addSimpleProperties(concept.getSimpleProperties(), addAxiom);
			addDelimitedProperties(concept.getDelimitedProperties(), addAxiom);
			// addSpecializedProperties(concept.getSpecialistDatabaseId(),
			// concept.getSpecialistDatabaseLink(), addAxiom);
			// System.out.println(concept.code);
			addSpecializedProperties(concept, addAxiom);
			// addHierarchyByGeneFamily(concept, axiom, clz);
		}
	}

	private void addSpecializedProperties(HgncConcept concept, AddAxiom ax) {
		OWLEntity ent = null;
		OWLSubClassAxiom subAx = (OWLSubClassAxiom) ax.getAxiom();
		// for (OWLEntity e : ax.getEntities()) {
		// if (e != factory.getOWLThing()) {
		// ent = e;
		// break;
		// }
		// }
		ent = subAx.getSubClass().asOWLClass();
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLDataType odt = factory.getOWLDataType(URI
		        .create(typeConstantLiteral));
		Set<String> propNames = concept.getSpecialistDatabaseIds().keySet();
		for (String prop : propNames) {
			// System.out.println("\tSpecialist_Database_Id\t" + prop + "\t"
			// + concept.getSpecialistDatabaseIds().get(prop));
			OWLTypedConstant otc = factory.getOWLTypedConstant(concept
			        .getSpecialistDatabaseIds().get(prop), odt);
			OWLAnnotation anno = factory.getOWLConstantAnnotation(
			        createURI(prop + "_ID"), otc);
			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
			        ent, anno);
			changes.add(new AddAxiom(ontology, ax1));
		}

		propNames = concept.getSpecialistDatabaseLinks().keySet();
		for (String prop : propNames) {
			String value = concept.getSpecialistDatabaseLinks().get(prop);
			value = value.replace("> <", " ");
			value = value.trim();
			// System.out.println("\tSpecialist_Database_Link\t" + prop + "\t"
			// + value);
			OWLTypedConstant otc = factory.getOWLTypedConstant(value, odt);
			OWLAnnotation anno = factory.getOWLConstantAnnotation(
			        createURI(prop + "_LINK"), otc);
			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
			        ent, anno);
			changes.add(new AddAxiom(ontology, ax1));
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			try {
				manager.applyChanges(list);
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}

	}

	private void addSimpleProperties(HashMap<String, String> properties,
	        AddAxiom ax) {

		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLEntity ent = null;
		OWLSubClassAxiom subAx = (OWLSubClassAxiom) ax.getAxiom();

		ent = subAx.getSubClass().asOWLClass();
		OWLDataType odt = factory
		        .getOWLDataType(URI.create(typeConstantString));
		Set<String> propNames = properties.keySet();
		for (String propName : propNames) {
			OWLTypedConstant otc = factory.getOWLTypedConstant(
			        properties.get(propName), odt);
			OWLAnnotation anno = factory.getOWLConstantAnnotation(
			        createURI(propName), otc);
			OWLEntityAnnotationAxiom ax1 = factory.getOWLEntityAnnotationAxiom(
			        ent, anno);
			changes.add(new AddAxiom(ontology, ax1));
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			try {
				manager.applyChanges(list);
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
	}

	private void addDelimitedProperties(
	        HashMap<String, Vector<String>> properties, AddAxiom ax) {
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		OWLEntity ent = null;
		OWLSubClassAxiom subAx = (OWLSubClassAxiom) ax.getAxiom();
		// for (OWLEntity e : ax.getEntities()) {
		// if (e != factory.getOWLThing()) {
		// ent = e;
		// break;
		// }
		// }
		ent = subAx.getSubClass().asOWLClass();
		OWLDataType odt = factory
		        .getOWLDataType(URI.create(typeConstantString));
		Set<String> propNames = properties.keySet();
		for (String propName : propNames) {
			Vector<String> propValueSet = properties.get(propName);
			for (String propValue : propValueSet) {
				OWLTypedConstant otc = factory.getOWLTypedConstant(propValue,
				        odt);
				OWLAnnotation anno = factory.getOWLConstantAnnotation(
				        createURI(propName), otc);
				OWLEntityAnnotationAxiom ax1 = factory
				        .getOWLEntityAnnotationAxiom(ent, anno);
				changes.add(new AddAxiom(ontology, ax1));
			}
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			try {
				manager.applyChanges(list);
			} catch (OWLException e) {
				e.printStackTrace();
			}
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
	public URI createURI(String className) {
		String urlCompliantClassName = HgncToOwl.underscoredString(className);
		return URI.create(ontologyURI + "#" + urlCompliantClassName);
	}

	/**
	 * Save ontology to the file specified in the properties By default encodes
	 * to utf-8
	 */
	private void saveOntology() {
		try {
			RDFXMLOntologyStorer storer = new RDFXMLOntologyStorer();
			File newFile = new File(saveURI);
			FileOutputStream out = new FileOutputStream(newFile);
			WriterOutputTarget target = new WriterOutputTarget(
			        new BufferedWriter(new OutputStreamWriter(out, "UTF8")));
			OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
			storer.storeOntology(manager, ontology, target, format);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
