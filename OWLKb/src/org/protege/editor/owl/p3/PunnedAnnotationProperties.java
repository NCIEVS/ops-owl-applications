package org.protege.editor.owl.p3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.RemoveAxiom;

/**
 * One of the sins of Protege 3 was that it encouraged users to create
 * annotation properties that were also object or data properties. RDF doesn't
 * handle this very well and it is nice to have a tool that will remove this
 * stuff from a file that has been touched by Protege 3.
 *
 * @author tredmond
 *
 */
public class PunnedAnnotationProperties {
	// private static final Logger LOGGER = Logger
	// .getLogger(PunnedAnnotationProperties.class);
	private final OWLOntology ontology;
	private final OWLDataFactory factory;

	private final List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	private final Set<OWLProperty> punnedProperties = new HashSet<OWLProperty>();
	private final Set<OWLNamedIndividual> punnedIndividuals = new HashSet<OWLNamedIndividual>();

	private int punnedPropertiesCount = 0;
	private int removedDataAssertionsCount = 0;
	private int addedDataAnnotationsCount = 0;
	private int removedObjectAssertionsCount = 0;
	private int addedObjectAnnotationsCount = 0;
	private int removedPropertyDeclarationsCount = 0;

	public PunnedAnnotationProperties(OWLOntology ontology) {
		this.ontology = ontology;
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	}

	public synchronized void fixOntology() {
		// long startTime = System.currentTimeMillis();

		this.reset();
		this.convertPropertyAssertions();
		this.commitChanges();
		this.removeBadPropertyDeclarations();
		this.removeBadIndividuals();
		this.commitChanges();
		this.displayStats();

		// LOGGER.info("Removal of the bogus object and data assertions took "
		// + (System.currentTimeMillis() - startTime) + "ms.");
	}

	private void reset() {
		this.changes.clear();
		this.punnedProperties.clear();
		this.punnedIndividuals.clear();

		this.punnedPropertiesCount = 0;
		this.removedDataAssertionsCount = 0;
		this.addedDataAnnotationsCount = 0;
		this.removedObjectAssertionsCount = 0;
		this.addedObjectAnnotationsCount = 0;
		this.removedPropertyDeclarationsCount = 0;
	}

	private void convertPropertyAssertions() {
		// LOGGER
		// .info("Converting bogus owl data/object property assertions to owl annotation property assertions");
		for (OWLAnnotationProperty ap : this.ontology
		        .getAnnotationPropertiesInSignature()) {
			IRI iri = ap.getIRI();
			if (this.ontology.containsDataPropertyInSignature(iri)) {
				this.punnedPropertiesCount++;
				OWLDataProperty dp = this.factory.getOWLDataProperty(iri);
				if (this.convertPropertyAssertions(ap, dp)) {
					this.punnedProperties.add(dp);
				}
			}
			if (this.ontology.containsObjectPropertyInSignature(iri)) {
				this.punnedPropertiesCount++;
				OWLObjectProperty op = this.factory.getOWLObjectProperty(iri);
				if (this.convertPropertyAssertions(ap, op)) {
					this.punnedProperties.add(op);
				}
			}
		}
	}

	private boolean convertPropertyAssertions(OWLAnnotationProperty ap,
	        OWLDataProperty dp) {
		boolean changed = false;
		for (OWLAxiom axiom : this.ontology.getReferencingAxioms(dp)) {
			if ((axiom instanceof OWLDataPropertyAssertionAxiom)
			        && !((OWLDataPropertyAssertionAxiom) axiom).getSubject()
			                .isAnonymous()) {
				OWLDataPropertyAssertionAxiom assertion = (OWLDataPropertyAssertionAxiom) axiom;
				OWLAnnotation annotation = this.factory.getOWLAnnotation(ap,
				        assertion.getObject());
				OWLNamedIndividual subject = assertion.getSubject()
				        .asOWLNamedIndividual();
				IRI subjectIRI = subject.getIRI();
				OWLAnnotationAssertionAxiom replacementAxiom = this.factory
				        .getOWLAnnotationAssertionAxiom(subjectIRI, annotation);
				this.changes.add(new RemoveAxiom(this.ontology, assertion));
				this.removedDataAssertionsCount++;
				if (!this.ontology.containsAxiom(replacementAxiom)) {
					this.changes.add(new AddAxiom(this.ontology,
					        replacementAxiom));
					this.addedDataAnnotationsCount++;
				}
				this.punnedIndividuals.add(subject);
				changed = true;
			}
		}
		return changed;
	}

	private boolean convertPropertyAssertions(OWLAnnotationProperty ap,
	        OWLObjectProperty op) {
		boolean changed = false;
		for (OWLAxiom axiom : this.ontology.getReferencingAxioms(op)) {
			if ((axiom instanceof OWLObjectPropertyAssertionAxiom)
			        && !((OWLObjectPropertyAssertionAxiom) axiom).getSubject()
			                .isAnonymous()) {
				OWLObjectPropertyAssertionAxiom assertion = (OWLObjectPropertyAssertionAxiom) axiom;
				if (!(assertion.getObject().isAnonymous())) {
					this.convertPropertyAssertionsNamedValue(ap, op, assertion);
					changed = true;
				} else {
					/* TODO anonymous individual case goes here */
					// LOGGER.warn("Could not replace axiom " + axiom);
				}
			}
		}
		return changed;
	}

	private void convertPropertyAssertionsNamedValue(OWLAnnotationProperty ap,
	        OWLObjectProperty op, OWLObjectPropertyAssertionAxiom assertion) {
		OWLNamedIndividual subject = assertion.getSubject()
		        .asOWLNamedIndividual();
		IRI subjectIRI = subject.getIRI();
		OWLAnnotation annotation = this.factory.getOWLAnnotation(ap, assertion
		        .getObject().asOWLNamedIndividual().getIRI());
		OWLAnnotationAssertionAxiom replacementAxiom = this.factory
		        .getOWLAnnotationAssertionAxiom(subjectIRI, annotation);
		this.changes.add(new RemoveAxiom(this.ontology, assertion));
		this.removedObjectAssertionsCount++;
		this.changes.add(new AddAxiom(this.ontology, replacementAxiom));
		this.addedObjectAnnotationsCount++;
		this.punnedIndividuals.add(subject);
	}


	private void removeBadPropertyDeclarations() {
		// LOGGER.info("Removing axioms about bogus object/data properties");
		for (OWLProperty p : this.punnedProperties) {
			OWLAnnotationProperty ap = this.factory.getOWLAnnotationProperty(p
			        .getIRI());
			this.removeBadPropertyDeclaration(ap, p);
		}
	}

	private void removeBadPropertyDeclaration(OWLAnnotationProperty ap,
	         OWLProperty e) {
		for (OWLAxiom axiom : this.ontology.getReferencingAxioms(e)) {
			if (axiom instanceof OWLDeclarationAxiom) {
				this.changes.add(new AddAxiom(this.ontology, this.factory
				        .getOWLDeclarationAxiom(ap)));
			} else if (axiom instanceof OWLDataPropertyRangeAxiom) {
				OWLDataRange range = ((OWLDataPropertyRangeAxiom) axiom)
				        .getRange();
				if (range instanceof OWLDatatype) {
					OWLAnnotationPropertyRangeAxiom replacementRangeAxiom = this.factory
					        .getOWLAnnotationPropertyRangeAxiom(ap,
					                ((OWLDatatype) range).getIRI());
					this.changes.add(new AddAxiom(this.ontology,
					        replacementRangeAxiom));
				} else {
					// LOGGER
					// .warn("Could not find replacement for axiom "
					// + axiom);
				}
			} else {
				// LOGGER.warn("Could not find replacement for axiom " + axiom);
			}
			this.changes.add(new RemoveAxiom(this.ontology, axiom));
		}
		this.removedPropertyDeclarationsCount++;
	}

	private void removeBadIndividuals() {
		for (OWLNamedIndividual punned : this.punnedIndividuals) {
			if (this.ontology.getReferencingAxioms(punned).size() != 0) {
				// LOGGER.warn("found individual axioms including some for "
				// + punned);
				break;
			}
		}
	}

	private void commitChanges() {
		OWLOntologyManager manager = this.ontology.getOWLOntologyManager();
		manager.applyChanges(this.changes);
		this.changes.clear();
	}

	private void displayStats() {
		// LOGGER.info("Found " + punnedPropertiesCount
		// + " annotation property puns");
		// LOGGER.info("Removed " + removedDataAssertionsCount
		// + " data assertions and added " + addedDataAnnotationsCount
		// + " annotation replacements");
		// LOGGER.info("Removed " + removedObjectAssertionsCount
		// + " object assertions and added " + addedObjectAnnotationsCount
		// + " annotation replacements");
		// LOGGER.info("Removed " + removedPropertyDeclarationsCount
		// + " property declarations");
		System.out.println("Found " + this.punnedPropertiesCount
		        + " annotation property puns");
		System.out.println("Removed " + this.removedDataAssertionsCount
		        + " data assertions and added "
		        + this.addedDataAnnotationsCount + " annotation replacements");
		System.out
		        .println("Removed " + this.removedObjectAssertionsCount
		                + " object assertions and added "
		                + this.addedObjectAnnotationsCount
		                + " annotation replacements");
		System.out.println("Removed " + this.removedPropertyDeclarationsCount
		        + " property declarations");
	}

	/**
	 * @param args
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException,
	        OWLOntologyStorageException, FileNotFoundException {
		String input = args[0];
		String output = args[1];
		// long startTime = System.currentTimeMillis();
		// LOGGER.info("Loading ontology from " + input);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
		config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		// manager.setSilentMissingImportsHandling(true);
		InputStream stream = new FileInputStream(input);
		OWLOntologyDocumentSource source = new StreamDocumentSource(stream);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(source,
		        config);
		// LOGGER.info("Loaded - doing repairs...");
		PunnedAnnotationProperties mrFixIt = new PunnedAnnotationProperties(
		        ontology);
		mrFixIt.fixOntology();
		// LOGGER.info("Repairs completed.  Saving as " + output);
		FileOutputStream out = new FileOutputStream(new File(output));
		manager.saveOntology(ontology, new StreamDocumentTarget(out));
		// LOGGER.info("Done. Took " + (System.currentTimeMillis() - startTime)
		// + "ms");
	}



}
