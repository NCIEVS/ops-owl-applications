/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl OwlApiLayer.java May 4, 2009
 */
package gov.nih.nci.evs.owl.data;


import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.metrics.NCIt_metrics;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.owl.proxy.PropertyProxy;
import gov.nih.nci.evs.owl.proxy.QualifierProxy;
import gov.nih.nci.evs.owl.visitor.AssociationVisitor;
import gov.nih.nci.evs.owl.visitor.AxiomAnnotationsChanger;
import gov.nih.nci.evs.owl.visitor.RoleDescriptionVisitor;
import gov.nih.nlm.nls.lvg.Flows.ToMapSymbolToAscii;
import gov.nih.nlm.nls.lvg.Flows.ToMapUnicodeToAscii;
import gov.nih.nlm.nls.lvg.Flows.ToStripMapUnicode;
import gov.nih.nlm.nls.lvg.Lib.LexItem;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.rdf.model.RDFGraph;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLStorer;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplPlain;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class OwlApiLayer.
 */
/**
 * @author safrant
 *
 */
@SuppressWarnings("deprecation")
public class OwlApiLayer {

	/** The Constant typeConstantString. */
	static final String typeConstantString = "http://www.w3.org/2001/XMLSchema#string";

	/** The Constant typeConstantLiteral. */
	static final String typeConstantLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

	/** The manager. */
	private OWLOntologyManager manager;

	/** The ontology. */
	private OWLOntology ontology = null;

	/** The ontology namespace. */
	private static String defaultNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";

	// /** The default ontology namespace. */
	// private String defaultOntologyNamespace =
	// "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";



	/** The reasoner. */
	private OWLReasoner reasoner;


	/** The factory. */
	private final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	/** The property map. */
	HashMap<IRI, OWLDataProperty> propertyMap = new HashMap<IRI, OWLDataProperty>();

	/** The anno property map. */
	HashMap<IRI, OWLAnnotationProperty> annoPropertyMap = new HashMap<IRI, OWLAnnotationProperty>();

	/** The reverse property map. */
	HashMap<String, OWLDataProperty> reversePropertyMap = new HashMap<String, OWLDataProperty>();

	/** The reverse anno property map. */
	HashMap<String, OWLAnnotationProperty> reverseAnnoPropertyMap = new HashMap<String, OWLAnnotationProperty>();

	/** The object property map. */
	HashMap<IRI, OWLObjectProperty> objectPropertyMap = new HashMap<IRI, OWLObjectProperty>();

	/** The role map. */
	HashMap<IRI, OWLObjectProperty> roleMap = new HashMap<IRI, OWLObjectProperty>();

	/** The reverse role map. */
	HashMap<String, OWLObjectProperty> reverseRoleMap = new HashMap<String, OWLObjectProperty>();

	/** The association map. */
	HashMap<IRI, OWLAnnotationProperty> associationMap = new HashMap<IRI, OWLAnnotationProperty>();

	/** The root map. */
	Vector<OWLClass> rootMap = new Vector<OWLClass>();

	/** The removed classes. */
	Vector<IRI> removedClasses = new Vector<IRI>();
	
	gov.nih.nci.evs.owl.data.AssociationGraph associationGraph;
	NCIt_metrics metrics;

	/**
	 * Instantiates a new owl api layer.
	 *
	 * @param inOntology
	 *            the in ontology
	 */
	public OwlApiLayer(final OWLOntology inOntology) {
		// super();
		this(inOntology, defaultNamespace);
	}

	/**
	 * Instantiates a new owl api layer.
	 *
	 * @param inOntology
	 *            the in ontology
	 * @param namespace
	 *            the namespace
	 */
	public OwlApiLayer(final OWLOntology inOntology, String namespace) {
		// super();
		this.manager = OWLManager.createOWLOntologyManager();
		this.ontology = inOntology;
		this.defaultNamespace = namespace;
		this.instantiateMaps();
		this.metrics = new NCIt_metrics(this.ontology);
//		startReasoner();
	}
	

	/**
	 * Instantiates a new owl api layer.
	 *
	 * @param uri
	 *            the uri
	 * @throws OWLOntologyCreationException
	 *             the OWL ontology creation exception
	 */

	public OwlApiLayer(final URI uri) throws OWLOntologyCreationException {
		this(uri, defaultNamespace);
//		this.manager = OWLManager.createOWLOntologyManager();
//		try {
//			
//			// this.manager.setSilentMissingImportsHandling(true);
//			// this.ontology = manager.loadOntology(iri);
//			// IRI iri = createIRI(uri);
//			boolean debug = true;
//			this.ontology = this.manager.loadOntologyFromOntologyDocument(IRI
//			        .create(uri));
//			this.instantiateMaps();
//		} catch (OWLOntologyCreationIOException e) {
//			this.manager = null;
//			e.printStackTrace();
//			System.out.println("Failed to create OwlApiLayer");
//			throw e;
//		}
	}

	/**
	 * Instantiates a new owl api layer.
	 *
	 * @param uri
	 *            the uri
	 * @param namespace
	 *            the namespace
	 * @throws OWLOntologyCreationException
	 *             the OWL ontology creation exception
	 */
	public OwlApiLayer(final URI uri, String namespace) throws OWLOntologyCreationException
	        {
		this(OWLManager.createOWLOntologyManager().loadOntology(IRI.create(uri)), namespace);
	}
	
	public OwlApiLayer(URI uri, String namespace, boolean createNew) throws OWLOntologyCreationException{
		if(createNew){
			OWLManager.createOWLOntologyManager().createOntology(IRI.create(uri));
		}
		this.manager = OWLManager.createOWLOntologyManager();
		this.ontology = manager.loadOntology(IRI.create(uri));
		this.defaultNamespace = namespace;
		this.instantiateMaps();
		this.metrics = new NCIt_metrics(this.ontology);
//		startReasoner();
	}


	public void addAnnotationAssertionAxiom(IRI propCode, IRI conceptCode,
	        String value, Vector<Qualifier> qualifiers) {
		// TODO Auto-generated method stub
		OWLDataFactory fac = this.manager.getOWLDataFactory();
		// IRI classIRI = createIRI(conceptCode, concept_namespace);
		OWLClass sub = this.getOWLClass(conceptCode);
		Set<OWLAnnotation> annotations = this
		        .convertQualifiersToAnnotations(qualifiers);

		OWLAnnotationAssertionAxiom ax = fac.getOWLAnnotationAssertionAxiom(
		        this.trTagToAnnotationProp(propCode), sub.getIRI(),
		        this.trLiteral(value), annotations);

		this.manager.applyChange(new AddAxiom(this.ontology, ax));
	}


	public void addAnnotationAssertionAxiom(String propCode, IRI conceptCode,
	        String value, Vector<Qualifier> qualifiers) {

		IRI propIRI = this.createIRI(propCode, this.getNamespace());
		this.addAnnotationAssertionAxiom(propIRI, conceptCode, value,
		        qualifiers);
	}

	/**
	 * Adds the annotation assertion axiom.
	 *
	 * @param propCode
	 *            the prop code
	 * @param conceptCode
	 *            the concept code
	 * @param value
	 *            the value
	 * @param qualifiers
	 *            the qualifiers //
	 */
	// public void addAnnotationAssertionAxiom(String propCode,
	// String conceptCode, String value, Vector<Qualifier> qualifiers) {
	// // OWLDataFactory fac = this.manager.getOWLDataFactory();
	// // OWLClass sub = getOWLClass(conceptCode);
	// // Set<OWLAnnotation> annotations =
	// // convertQualifiersToAnnotations(qualifiers);
	// // OWLAnnotationAssertionAxiom ax = fac.getOWLAnnotationAssertionAxiom(
	// // trTagToAnnotationProp(propCode), sub.getIRI(),
	// // trLiteral(value), annotations);
	//
	// addAnnotationAssertionAxiom( propCode,
	// conceptCode, value, qualifiers);
	//
	// }

	/**
	 * Adds the annotation to axiom.
	 *
	 * @param prop_ax
	 *            the prop ax
	 * @param qualId
	 *            the qual id
	 * @param qualValue
	 *            the qual value
	 */
	public void addAnnotationToAxiom(OWLAxiom prop_ax, String qualId,
	        String qualValue) {
		final OWLDataFactory df = this.manager.getOWLDataFactory();
		OWLAnnotation anno = df.getOWLAnnotation(df
		        .getOWLAnnotationProperty(this.createIRI(this.getNamespace(),
		                qualId)), df.getOWLLiteral(qualValue));
		OWLAxiom qual_ax = df.getOWLAnnotationAssertionAxiom(
		        (OWLAnnotationSubject) prop_ax, anno);
		this.manager.applyChange(new AddAxiom(this.ontology, qual_ax));
	}

	/**
	 * Adds the parent.
	 *
	 * @param child
	 *            the child
	 * @param newParent
	 *            the new parent
	 */
	public void addParent(IRI child, IRI newParent) {
		OWLDataFactory df = this.manager.getOWLDataFactory();
		OWLAxiom axiom = df.getOWLSubClassOfAxiom(this.getOWLClass(child),
		        this.getOWLClass(newParent));
		AddAxiom addAxiom = new AddAxiom(this.ontology, axiom);
		this.manager.applyChange(addAxiom);

		// OWLClass childClass = getOWLClass(child);
		//
		// Stream<OWLClassExpression> supers =
		// EntitySearcher.getSuperClasses(childClass, ontology);
		// // Set<OWLClassExpression> supers =
		// childClass.getSuperClasses(ontology);
		//
		// Set<OWLSubClassOfAxiom> subs = ontology
		// .getSubClassAxiomsForSubClass(childClass);
		// OWLAxiom removeAxiom = df.getOWLSubClassOfAxiom(childClass,
		// childClass);
	}

	// /**
	// * Adds the parent.
	// *
	// * @param namespace
	// * the namespace
	// * @param child
	// * the child
	// * @param newParent
	// * the new parent
	// */
	// public void addParent(String namespace, String child, String newParent) {
	// // TODO implement this or delete it
	// }

	/**
	 * Adds the property.
	 *
	 * @param conceptId
	 *            the concept id
	 * @param propertyID
	 *            the property ID
	 * @param propertyValue
	 *            the property value
	 * @param hasLiterals
	 *            the has literals
	 */
	public void addProperty(IRI conceptId, IRI propertyID,
	        String propertyValue, boolean hasLiterals) {
		/*
		 * // We want to add a comment to the pizza class. 53 // First, we need
		 * to obtain a reference to the pizza class 54 OWLDataFactory df =
		 * man.getOWLDataFactory(); 55 OWLClass pizzaCls =
		 * df.getOWLClass(IRI.create
		 * (ont.getOntologyID().getOntologyIRI().toString() + "#Pizza")); 56 57
		 * // Now we create the content of our comment. In this case we simply
		 * want a plain string literal. 58 // We'll attach a language to the
		 * comment to specify that our comment is written in English (en). 59
		 * OWLAnnotation commentAnno = df.getOWLAnnotation( 60
		 * df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
		 * 61 df.getOWLStringLiteral("A class which represents pizzas", "en"));
		 * 62 63 // Specify that the pizza class has an annotation - to do this
		 * we attach an entity annotation using 64 // an entity annotation axiom
		 * (remember, classes are entities) 65 OWLAxiom ax =
		 * df.getOWLAnnotationAssertionAxiom(pizzaCls.getIRI(), commentAnno); 66
		 * 67 // Add the axiom to the ontology 68 man.applyChange(new
		 * AddAxiom(ont, ax));
		 */
		final OWLDataFactory df = this.manager.getOWLDataFactory();
		final OWLClass editedClass = df.getOWLClass(conceptId);
		// final OWLAnnotation fullSynAnno = df.getOWLAnnotation(
		// df.getOWLAnnotationProperty(propertyID),
		// df.getOWLStringLiteral(propertyValue));
		final OWLAnnotation fullSynAnno = df.getOWLAnnotation(
		        df.getOWLAnnotationProperty(propertyID),
		        df.getOWLLiteral(propertyValue));
		final OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(
		        editedClass.getIRI(), fullSynAnno);
		this.manager.applyChange(new AddAxiom(this.ontology, ax));
		// Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		// OWLDataFactory factory = manager.getOWLDataFactory();
		// for (OWLAxiom ax : ontology.getAxioms()) {
		// if (ax.isOfType(AxiomType.ANNOTATION_ASSERTION)) {
		//
		// OWLAnnotationAssertionAxiomImpl axImpl =
		// (OWLAnnotationAssertionAxiomImpl) ax;
		// OWLAnnotationSubject subject = axImpl.getSubject();
		// OWLAnnotationPropertyImpl prop = (OWLAnnotationPropertyImpl) axImpl
		// .getProperty();
		// String propString = prop.getURI().getFragment();
		//
		// IRI newPropURI = createIRI(cleanProperty);
		// OWLAnnotationPropertyImpl newProp = (OWLAnnotationPropertyImpl)
		// factory
		// .getOWLAnnotationProperty(newPropURI);
		// if (propString.equals(complexProperty)) {
		// OWLEntity ent = getReferencingClassEntity(ax, hasLiterals);
		// OWLLiteralImpl value = (OWLLiteralImpl) axImpl.getValue();
		// String termName = getTagValue(value.getLiteral(),
		// complexTagName);
		// if (termName != null) {
		//
		// OWLDatatype odt;
		// odt = factory.getOWLDatatype(IRI
		// .create(typeConstantString));
		//
		// OWLTypedLiteral otl = factory.getOWLTypedLiteral(
		// termName, odt);
		// OWLAnnotation newAnno = factory.getOWLAnnotation(
		// newProp, otl);
		//
		// OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(
		// subject, newAnno);
		// changes.add(new AddAxiom(ontology, ax1));
		// }
		// }
		// }
		// }
		// if (!changes.isEmpty()) {
		// List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
		// changes);
		// try {
		// manager.applyChanges(list);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
	}


	// public void addProperty(String concept_namespace, String conceptId,
	// String propertyID, String propertyValue, boolean hasLiterals) {
	//
	// final OWLDataFactory df = this.manager.getOWLDataFactory();
	// final OWLClass editedClass = df.getOWLClass(createIRI(conceptId,
	// concept_namespace));
	// final OWLAnnotation fullSynAnno = df.getOWLAnnotation(
	// df.getOWLAnnotationProperty(createIRI(propertyID)),
	// df.getOWLStringLiteral(propertyValue));
	// final OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(
	// editedClass.getIRI(), fullSynAnno);
	// this.manager.applyChange(new AddAxiom(this.ontology, ax));
	//
	// }

	public void addProperty(IRI conceptId, IRI propertyId, String propertyValue) {
		this.addProperty(conceptId, propertyId, propertyValue, false);
	}

	/**
	 * Adds the role.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param roleCode
	 *            the role code
	 */
	// public void addRole(IRI source, IRI target, String roleCode) {
	// // this.addRole(source, target, this.defaultOntologyNamespace, roleCode);
	// // OWLDataFactory df = manager.getOWLDataFactory();
	// //
	// // OWLClass sourceClass = getOWLClass(source);
	// // OWLClass targetClass = getOWLClass(target);
	// // OWLObjectProperty thisRole = df
	// // .getOWLObjectProperty(createIRI(roleCode));
	// // OWLClassExpression thisRestriction = df.getOWLObjectSomeValuesFrom(
	// // thisRole, targetClass);
	// //
	// // OWLAxiom axiom = df.getOWLSubClassOfAxiom(sourceClass,
	// // thisRestriction);
	// // AddAxiom addAx = new AddAxiom(ontology, axiom);
	// // manager.applyChange(addAx);
	// IRI roleIRI = createIRI(this.getNamespace(),roleCode);
	// this.addRole(source, target, roleIRI);
	//
	//
	// }

	public void addRole(IRI source, IRI target, IRI roleCode) {
		// this.addRole(source, target, this.defaultOntologyNamespace,
		// roleCode);
		// OWLDataFactory df = manager.getOWLDataFactory();
		//
		// OWLClass sourceClass = getOWLClass(source);
		// OWLClass targetClass = getOWLClass(target);
		// OWLObjectProperty thisRole = df
		// .getOWLObjectProperty(createIRI(roleCode));
		// OWLClassExpression thisRestriction = df.getOWLObjectSomeValuesFrom(
		// thisRole, targetClass);
		//
		// OWLAxiom axiom = df.getOWLSubClassOfAxiom(sourceClass,
		// thisRestriction);
		// AddAxiom addAx = new AddAxiom(ontology, axiom);
		// manager.applyChange(addAx);

		OWLDataFactory df = this.manager.getOWLDataFactory();

		OWLClass sourceClass = this.getOWLClass(source);
		OWLClass targetClass = this.getOWLClass(target);
		OWLObjectProperty thisRole = df.getOWLObjectProperty(roleCode);
		OWLClassExpression thisRestriction = df.getOWLObjectSomeValuesFrom(
		        thisRole, targetClass);

		OWLAxiom axiom = df.getOWLSubClassOfAxiom(sourceClass, thisRestriction);
		AddAxiom addAx = new AddAxiom(this.ontology, axiom);
		this.manager.applyChange(addAx);
	}

	/**
	 * Adds the role.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param targetNamespace
	 *            the target namespace
	 * @param roleCode
	 *            the role code
	 */
	// public void addRole(IRI source, IRI target, String targetNamespace,
	// String roleCode) {
	// OWLDataFactory df = manager.getOWLDataFactory();
	//
	// OWLClass sourceClass = getOWLClass(source);
	// OWLClass targetClass = getOWLClass(target);
	// OWLObjectProperty thisRole = df
	// .getOWLObjectProperty(createIRI(roleCode));
	// OWLClassExpression thisRestriction = df.getOWLObjectSomeValuesFrom(
	// thisRole, targetClass);
	//
	// OWLAxiom axiom = df.getOWLSubClassOfAxiom(sourceClass, thisRestriction);
	// AddAxiom addAx = new AddAxiom(ontology, axiom);
	// manager.applyChange(addAx);
	//
	// }

	/**
	 * Assign RDF label.
	 *
	 * @param cls
	 *            the cls
	 * @param name
	 *            the name
	 */
	public void assignRDFLabel(OWLClass cls, String name) {
		OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
		OWLLiteral lbl = dataFactory.getOWLLiteral(name);
		OWLAnnotation label = dataFactory.getOWLAnnotation(
		        dataFactory
		                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL
		                        .getIRI()), lbl);
		OWLAxiom axiom = dataFactory.getOWLAnnotationAssertionAxiom(
		        cls.getIRI(), label);
		this.manager.applyChange(new AddAxiom(this.ontology, axiom));
	}

	// /**
	// * Creates an OWL uri from the ontology namespace and a class name.
	// *
	// * @param className
	// * the class name
	// * @return the uRI
	// */
	// public URI createURI(String className) {
	// return URI.create(this.ontologyNamespace + "#" + className);
	// }

	/**
	 * Assign RDF label.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 */
	public void assignRDFLabel(IRI id, String name) {
		OWLClass cls = this.getOWLClass(id);

		this.assignRDFLabel(cls, name);
	}

	/**
	 * Assign sub class.
	 *
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 */
	public void assignSubClass(OWLClass parent, OWLClass child) {
		OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
		OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(child, parent);
		AddAxiom addAxiom = new AddAxiom(this.ontology, axiom);
		this.manager.applyChange(addAxiom);
	}

	/**
	 * Assign sub class.
	 *
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 */
	public void assignSubClass(IRI parent, IRI child) {
		OWLClass parentCls = this.getOWLClass(parent);
		OWLClass childCls = this.getOWLClass(child);
		this.assignSubClass(parentCls, childCls);
	}

	/**
	 * Change IRI.
	 *
	 * @param oldIRI
	 *            the old IRI
	 * @param newIRI
	 *            the new IRI
	 */
	public void changeIRI(IRI oldIRI, IRI newIRI) {
		Set<OWLOntology> owlSet = new HashSet<OWLOntology>();
		owlSet.add(this.ontology);
		OWLEntityRenamer oer = new OWLEntityRenamer(this.manager, owlSet);
		List<OWLOntologyChange> changes = oer.changeIRI(oldIRI, newIRI);
		this.manager.applyChanges(changes);
	}

	public static String getDomainFromIRI(IRI iri) {
		// String tempDomain = iri.getStart();
		String tempDomain = iri.getNamespace();
		if (tempDomain.endsWith("#") || tempDomain.endsWith("//")) {
			tempDomain = tempDomain.substring(0, tempDomain.length() - 1);
		}
		return tempDomain;
	}

	/**
	 * Change URI.
	 *
	 * @param oldURI
	 *            the old URI
	 * @param newURI
	 *            the new URI
	 */
	public void changeURI(URI oldURI, URI newURI) {
		this.changeIRI(IRI.create(oldURI), IRI.create(newURI));
	}

	/**
	 * Concept exists.
	 *
	 * @param code
	 *            the code
	 * @return true, if successful
	 */
	public boolean conceptExists(IRI code) {

		// IRI iri = createIRI(code);
		return this.ontology.containsClassInSignature(code);

	}

	/**
	 * Construct clean property.
	 *
	 * @param cleanProperty
	 *            the clean property
	 * @param complexProperty
	 *            the complex property
	 * @param complexTagName
	 *            the complex tag name
	 * @param hasLiterals
	 *            the has literals
	 */

	public void constructCleanProperty(String cleanProperty,
	        String complexProperty, String complexTagName, boolean hasLiterals) {
		final Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		final OWLDataFactory tmpFactory = this.manager.getOWLDataFactory();
		final IRI newPropURI = this.createIRI(cleanProperty,
		        this.getNamespace());
		for (final OWLAxiom ax : this.ontology.getAxioms()) {
			if (ax.isOfType(AxiomType.ANNOTATION_ASSERTION)) {

				final OWLAnnotationAssertionAxiomImpl axImpl = (OWLAnnotationAssertionAxiomImpl) ax;
				final OWLAnnotationSubject subject = axImpl.getSubject();
				final OWLAnnotationPropertyImpl prop = (OWLAnnotationPropertyImpl) axImpl
				        .getProperty();
				final String propString = prop.getIRI().getFragment();

				final OWLAnnotationPropertyImpl newProp = (OWLAnnotationPropertyImpl) tmpFactory
				        .getOWLAnnotationProperty(newPropURI);
				if (propString != null) {

					if (propString.equals(complexProperty)) {
						// OWLEntity ent = getReferencingClassEntity(ax,
						// hasLiterals);
						final OWLLiteralImpl value = (OWLLiteralImpl) axImpl
						        .getValue();
						final String termName = this.getTagValue(
						        value.getLiteral(), complexTagName);
						if (termName != null) {

							OWLDatatype odt;
							odt = tmpFactory.getOWLDatatype(IRI
							        .create(OwlApiLayer.typeConstantString));

							// OWLTypedLiteral otl =
							// tmpFactory.getOWLTypedLiteral(
							// termName, odt);
							final OWLLiteral otl = tmpFactory.getOWLLiteral(
							        termName, odt);

							final OWLAnnotation newAnno = tmpFactory
							        .getOWLAnnotation(newProp, otl);

							final OWLAxiom ax1 = tmpFactory
							        .getOWLAnnotationAssertionAxiom(subject,
							                newAnno);
							changes.add(new AddAxiom(this.ontology, ax1));
						}
					}
				}
			}
		}
		if (!changes.isEmpty()) {
			final List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			try {
				this.manager.applyChanges(list);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Convert qualifiers to annotations.
	 *
	 * @param qualifiers
	 *            the qualifiers
	 * @return the sets the
	 */
	private Set<OWLAnnotation> convertQualifiersToAnnotations(
	        Vector<Qualifier> qualifiers) {
		Set<OWLAnnotation> anns = new HashSet<OWLAnnotation>();
		OWLDataFactory fac = this.manager.getOWLDataFactory();
		for (Qualifier qual : qualifiers) {
			OWLAnnotationProperty ap = this.trTagToAnnotationProp(qual
			        .getName());
			OWLLiteral qVal = fac.getOWLLiteral(qual.getValue());
			OWLAnnotation ann = fac.getOWLAnnotation(ap, qVal);
			anns.add(ann);
		}
		return anns;
	}

	/**
	 * Convert unicode to ascii.
	 *
	 * @param unicodeMap
	 *            the unicode map
	 * @param symbolMap
	 *            the symbol map
	 * @param stripMap
	 *            the strip map
	 * @param hasLiterals
	 *            the has literals
	 */
	public void convertUnicodeToAscii(Hashtable<Character, String> unicodeMap,
	        Hashtable<Character, String> symbolMap,
	        Hashtable<Character, String> stripMap, boolean hasLiterals) {
		final Set<OWLOntologyChange> removeAx = new HashSet<OWLOntologyChange>();
		final Set<OWLOntologyChange> addAx = new HashSet<OWLOntologyChange>();
		// TODO it never checks the property id, just eliminates the tag.
		// Fix this.
		for (final OWLAxiom ax : this.ontology.getAxioms()) {
			if (ax.isOfType(AxiomType.ANNOTATION_ASSERTION)) {
				final OWLAnnotationAssertionAxiomImpl axImpl = (OWLAnnotationAssertionAxiomImpl) ax;
				final OWLAnnotationSubject subject = axImpl.getSubject();
				final OWLAnnotationPropertyImpl prop = (OWLAnnotationPropertyImpl) axImpl
				        .getProperty();
				final OWLLiteralImpl value = (OWLLiteralImpl) axImpl.getValue();

				final String origValue = value.getLiteral();

				LexItem in = new LexItem(origValue);
				Vector<LexItem> outs = ToMapUnicodeToAscii.Mutate(in,
				        unicodeMap, true, true);
				in = new LexItem(outs.get(0).GetTargetTerm());
				outs = ToMapSymbolToAscii.Mutate(in, symbolMap, true, true);
				in = new LexItem(outs.get(0).GetTargetTerm());
				outs = ToStripMapUnicode.Mutate(in, stripMap, true, true);

				final String newValue = outs.get(0).GetTargetTerm();

				if (newValue.compareTo(origValue) != 0) {
					System.out.println("Bad character found "
					        + this.getConceptCode(ax) + " " + origValue);

					// delete old axiom
					removeAx.add(new RemoveAxiom(this.ontology, ax));

					// create new axiom
					final OWLDataFactory factory = this.manager
					        .getOWLDataFactory();
					OWLDatatype odt;

					if (hasLiterals) {
						odt = factory.getOWLDatatype(IRI
						        .create(OwlApiLayer.typeConstantLiteral));
					} else {
						odt = factory.getOWLDatatype(IRI
						        .create(OwlApiLayer.typeConstantString));
					}

					// final OWLLiteral otl =
					// factory.getOWLTypedLiteral(newValue,
					// odt);
					final OWLLiteral otl = factory.getOWLLiteral(newValue, odt);
					final OWLAnnotation newAnno = factory.getOWLAnnotation(
					        prop, otl);

					final OWLAxiom ax1 = factory
					        .getOWLAnnotationAssertionAxiom(subject, newAnno);
					addAx.add(new AddAxiom(this.ontology, ax1));
				}

			}
		}
		if (!addAx.isEmpty() && !removeAx.isEmpty()) {
			final List<OWLOntologyChange> removeList = new ArrayList<OWLOntologyChange>(
			        removeAx);
			final List<OWLOntologyChange> addList = new ArrayList<OWLOntologyChange>(
			        addAx);
			try {
				// System.out.println("Applying changed complex properties...");
				this.manager.applyChanges(removeList);
				this.manager.applyChanges(addList);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	// public void createClass(String id, String name) {
	// OWLDataFactory dataFactory = manager.getOWLDataFactory();
	// IRI classIri = createIRI(id);
	// OWLClass newClass = dataFactory.getOWLClass(classIri);
	// assignRDFLabel(newClass, name);
	// OWLDeclarationAxiom declarationAxiom = dataFactory
	// .getOWLDeclarationAxiom(newClass);
	// manager.addAxiom(ontology, declarationAxiom);
	// }


	public void createClass(IRI classIri) {
		// This will create a dumb class with just an id and label
		OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
		// IRI classIri = createIRI(id);
		OWLClass newClass = dataFactory.getOWLClass(classIri);
		OWLDeclarationAxiom declarationAxiom = dataFactory
		        .getOWLDeclarationAxiom(newClass);
		this.manager.addAxiom(this.ontology, declarationAxiom);

	}

	/**
	 * Creates the class.
	 *
	 * @param id
	 *            the id
	 * @param namespace
	 *            the namespace
	 */
	public void createClass(String id, String namespace) {
		// used for imported classes

		IRI classIri = this.createIRI(id, namespace);
		this.createClass(classIri);
	}

	/**
	 * Creates the IRI.
	 *
	 * @param className
	 *            the class name
	 * @return the iri
	 */
	// private IRI createIRI(String className) {
	// return IRI.create(createURI(className));
	// }

	/**
	 * Creates the IRI.
	 *
	 * @param className
	 *            the class name
	 * @param namespace
	 *            the namespace
	 * @return the iri
	 */
	private IRI createIRI(String className, String namespace) {
		return IRI.create(createURI(className, namespace));
	}
	
	private IRI createIRI(String className, URI namespace) {
		String newURI = namespace.toString() + className;
		return IRI.create(newURI);
	}

	/**
	 * Creates the IRI.
	 *
	 * @param uri
	 *            the uri
	 * @return the iri
	 */
	// private IRI createIRI(URI uri) {
	// return IRI.create(uri);
	// }

	/**
	 * Creates the URI.
	 *
	 * @param className
	 *            the class name
	 * @return the uri
	 */
	// private URI createURI(String className) {
	//
	// // PrefixManager pm = new DefaultPrefixManager();
	// // IRI iri = pm.getIRI("rdf:about");
	// // String defaultPrefix = ontologyNamespace+"#";
	// // ((DefaultPrefixManager) pm).setDefaultPrefix(defaultPrefix);
	// // String expansion = defaultPrefix + 'A';
	// // IRI iri2 = pm.getIRI(":A");
	// //
	// // Set<String> set = pm.getPrefixNames();
	// //
	// // OWLDataFactory df = manager.getOWLDataFactory();
	// // OWLClass A = df.getOWLClass(":" + className, pm);
	//
	// return URI.create(this.ontologyNamespace + "#" + className);
	// }

	/**
	 * Creates the URI.
	 *
	 * @param className
	 *            the class name
	 * @param namespace
	 *            the namespace
	 * @return the uri
	 */
	public static URI createURI(String className, String namespace) {
		if (namespace.endsWith("#") || namespace.endsWith("/")) {
			namespace = namespace.substring(0, namespace.length() - 1);
		}
		return URI.create(namespace + "#" + className);
	}

	/**
	 * Fix associations.
	 */
	private void fixAssociations() {

//		final PunnedAnnotationProperties assocFix = new PunnedAnnotationProperties(
//		        this.ontology);
//		assocFix.fixOntology();
	}

	/**
	 * Fix references.
	 */
	public void fixReferences() {
		try {
			final Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();

			if (this.removedClasses != null) {
				for (final IRI conceptCode : this.removedClasses) {
					final OWLClass concept = this.getOWLClass(conceptCode);
					// Set<OWLAnnotationProperty> sigProps =
					// concept.getAnnotationPropertiesInSignature();
					Collection<OWLAnnotationAssertionAxiom> esAxioms = EntitySearcher
					        .getAnnotationAssertionAxioms(concept,
					                this.ontology);
					for (final OWLAnnotationAxiom axiom : esAxioms) {
						changes.add(new RemoveAxiom(this.ontology, axiom));
					}
				}
			}

			final List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			this.manager.applyChanges(list);

			if (this.removedClasses != null) {
				final OWLEntityRemover remover = new OWLEntityRemover(
				        Collections.singleton(this.ontology));
				Set<OWLAxiom> axiomsToRemove = new HashSet<OWLAxiom>();
				for (final IRI conceptCode : this.removedClasses) {
					OWLClass clz = this.getOWLClass(conceptCode);
					Set<OWLDeclarationAxiom> axiomsDec = this.ontology
					        .getDeclarationAxioms(clz);
					Set<OWLClassAxiom> axiomsGet = this.ontology.getAxioms(clz);
					Set<OWLAxiom> axiomsRef = this.ontology
					        .getReferencingAxioms(clz);
					for (OWLAxiom ax : this.ontology.getAxioms()) {
						if (ax.getSignature().contains(clz)) {
							axiomsToRemove.add(ax);
						}
					}
					clz.accept(remover);
				}
				ChangeApplied changed = this.manager.removeAxioms(
				        this.ontology, axiomsToRemove);
				this.manager.applyChanges(remover.getChanges());

			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the IR iwo hash get fragment.
	 *
	 * @param propIRI
	 *            the prop IRI
	 * @return the IR iwo hash get fragment
	 */
	private String get_IRIwoHash_getFragment(IRI propIRI) {
		final String iriString = propIRI.toString();
		final String[] iriArray = iriString.split("/");
		final int length = iriArray.length;
		final String fragment = iriArray[length - 1];
		return fragment;
	}

	/**
	 * Gets the all ancestors for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the all ancestors for concept
	 */
	public Vector<IRI> getAllAncestorsForConcept(IRI conceptCode) {
		// TODO compare to getAncestorList
		final Vector<IRI> parentCodes = new Vector<IRI>();
		final OWLClass concept = this.getOWLClass(conceptCode);

		final Vector<OWLClass> descendants = this.getSuperClasses(concept,
		        false);
		for (final OWLClass cls : descendants) {
			parentCodes.add(cls.getIRI());
		}
		return parentCodes;
	}

	/**
	 * Gets the all annotation properties.
	 *
	 * @return the all annotation properties
	 */
	public HashMap<IRI, String> getAllAnnotationPropertyNames() {
		final HashMap<IRI, String> names = new HashMap<IRI, String>();
		final HashMap<IRI, OWLAnnotationProperty> owlPropertyMap = this
		        .getAnnotationPropertyMap();
		final Set<IRI> keys = owlPropertyMap.keySet();
		for (final IRI key : keys) {

			final OWLAnnotationProperty owlProperty = owlPropertyMap.get(key);
			IRI code = owlProperty.getIRI();
			// if (code == null) {
			// code = get_IRIwoHash_getFragment(owlProperty.getIRI());
			// }
			final String name = this.getRDFSLabel(owlProperty);
			names.put(code, name);
		}
		return names;
	}

	/**
	 * Gets the all associations.
	 *
	 * @return the all associations
	 */
	public HashMap<IRI, String> getAllAssociationNames() {
		final HashMap<IRI, String> names = new HashMap<IRI, String>();
		final HashMap<IRI, OWLAnnotationProperty> owlPropertyMap = this
		        .getAssociationMap();
		final Set<IRI> keys = owlPropertyMap.keySet();
		for (final IRI key : keys) {
			final OWLAnnotationProperty owlProperty = owlPropertyMap.get(key);
			
			final String name = this.getRDFSLabel(owlProperty);
			names.put(key, name);
		}
		return names;
	}

	/**
	 * Gets the all concept ids.
	 *
	 * @return the all concept ids //
	 */
	// @Deprecated
	// public Vector<String> getAllConceptIds() {
	// final Vector<String> concepts = new Vector<String>();
	//
	// Integer counter = 0;
	// final Set<OWLClass> allClasses = this.ontology.getClassesInSignature();
	// allClasses.size();
	// for (final OWLClass owlClass : allClasses) {
	// counter++;
	// // if (isRootClass(owlClass)) {
	// final IRI uri = owlClass.getIRI();
	// if (uri.getFragment() != null) {
	// concepts.add(uri.getFragment());
	//
	// // uri.
	// } else {
	// System.out.println("Concept with no Fragment "
	// + owlClass.toString());
	// }
	// // }
	// }
	// // TODO sort vector by concept code
	// return concepts;
	// }

	public Vector<IRI> getAllConceptIRIs() {
		final Vector<IRI> concepts = new Vector<IRI>();

		Integer counter = 0;
		final Set<OWLClass> allClasses = this.ontology.getClassesInSignature();
		allClasses.size();
		for (final OWLClass owlClass : allClasses) {
			counter++;
			// if (isRootClass(owlClass)) {
			final IRI iri = owlClass.getIRI();
			concepts.add(iri);

			// }
		}
		// TODO sort vector by concept code
		return concepts;
	}

	/**
	 * Gets the all concepts.
	 *
	 * @return the all concepts
	 */
	public Set<OWLClass> getAllConcepts() {

		return this.ontology.getClassesInSignature();

	}

	/**
	 * Gets the all datatype properties.
	 *
	 * @return the all datatype properties
	 */
	public HashMap<IRI, String> getAllDatatypePropertyNames() {
		final HashMap<IRI, String> names = new HashMap<IRI, String>();
		final HashMap<IRI, OWLDataProperty> owlPropertyMap = this
		        .getDataPropertyMap();
		final Set<IRI> keys = owlPropertyMap.keySet();
		for (final IRI key : keys) {
			final OWLDataProperty owlProperty = owlPropertyMap.get(key);
			IRI code = owlProperty.getIRI();
			// if (code == null) {
			// code = get_IRIwoHash_getFragment(owlProperty.getIRI());
			// }
			final String name = this.getRDFSLabel(owlProperty);
			names.put(code, name);
		}
		return names;

	}

	/**
	 * Gets the all descendants for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the all descendants for concept
	 */
	public Vector<IRI> getAllDescendantsForConcept(IRI conceptCode) {
		final Vector<IRI> childrenCodes = new Vector<IRI>();
		final OWLClass concept = this.getOWLClass(conceptCode);

		final Vector<OWLClass> descendants = this.getSubClasses(concept, false);
		if(descendants ==null){
			return childrenCodes;
		}
		for (final OWLClass cls : descendants) {
			final IRI code = cls.getIRI();
			if (!childrenCodes.contains(code)) {
				childrenCodes.add(cls.getIRI());
			}
		}
		return childrenCodes;

	}

	/**
	 * Gets the all properties.
	 *
	 * @return the all properties
	 */
	public HashMap<IRI, String> getAllProperties() {

		final HashMap<IRI, String> names = new HashMap<IRI, String>();
		names.putAll(this.getAllAnnotationPropertyNames());
		names.putAll(this.getAllDatatypePropertyNames());
		return names;
	}

	/**
	 * Gets the all roles.
	 *
	 * @return the all roles
	 */
	public HashMap<IRI, String> getAllRoleNames() {
		final HashMap<IRI, String> names = new HashMap<IRI, String>();
		final HashMap<IRI, OWLObjectProperty> owlPropertyMap = this
		        .getRoleMap();
		final Set<IRI> keys = owlPropertyMap.keySet();
		for (final IRI key : keys) {
			final OWLObjectProperty owlProperty = owlPropertyMap.get(key);
			final IRI code = owlProperty.getIRI();
			final String name = this.getRDFSLabel(owlProperty);

			if (code != null) {
				names.put(code, name);
			} else if (name != null) {
				names.put(this.createIRI(this.getNamespace(), name), name);
				System.out.println("Property has no code " + name);
			}

			// System.out.println(name +"\t"+ rangeString +"\t"+ domainString);
		}
		return names;
	}

	/**
	 * Gets the ancestor list.
	 *
	 * @param code
	 *            the code
	 * @return the ancestor list
	 */
	public Vector<IRI> getAncestorList(IRI code) {
		NodeSet<OWLClass> supers = this.getPathToRoot(code);
		Vector<IRI> ancestors = new Vector<IRI>();

		Iterator<Node<OWLClass>> iter = supers.iterator();
		while (iter.hasNext()) {
			Node<OWLClass> node = iter.next();
			int size = node.getSize();
			if (size == 1) {
				OWLClass cls = (OWLClass) node.getEntities().toArray()[0];
				ancestors.add(cls.getIRI());
			} else {
				Set<OWLClass> classes = node.getEntities();
			}
		}
		return ancestors;
	}

	/**
	 * Gets the annotation assertion axioms for class.
	 *
	 * @param code
	 *            the code
	 * @return the annotation assertion axioms for class
	 */
	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxiomsForClass(
	        IRI code) {
		// final OWLClass cls = getOWLClass(code);
		return this.ontology.getAnnotationAssertionAxioms(code);
	}

	/**
	 * Gets the annotation properties for class.
	 *
	 * @param code
	 *            the code
	 * @return the annotation properties for class
	 */

	public Vector<OWLAnnotationProperty> getAnnotationPropertiesForClass(
	        IRI code) {
		final Vector<OWLAnnotationProperty> props = new Vector<OWLAnnotationProperty>();
		final OWLClass cls = this.getOWLClass(code);

		// final Set<OWLAnnotationAssertionAxiom> propAsserts = cls
		// .getAnnotationAssertionAxioms(this.ontology);
		//
		// final Set<OWLAnnotation> annotations = cls
		// .getAnnotations(this.ontology);

		Set<OWLAnnotationAssertionAxiom> oaaa = this.ontology
		        .getAnnotationAssertionAxioms(cls.getIRI());
		for (OWLAnnotationAssertionAxiom oaaax : oaaa) {
			Set<OWLAnnotation> quals = oaaax.getAnnotations();
			for (OWLAnnotation annoQual : quals) {
				OWLAnnotationProperty aqp = annoQual.getProperty();
				OWLAnnotationValue aqv = annoQual.getValue();
				String debug = "stop here";
			}
			AxiomType<?> debug2 = oaaax.getAxiomType();
			OWLAnnotationProperty anno = oaaax.getProperty();
			OWLAnnotationSubject debug4 = oaaax.getSubject();
			OWLAnnotationValue ov = oaaax.getValue();

			props.add(anno);
			String debug = "stop here";
		}

		return props;

	}

	/**
	 * Gets the annotation property map.
	 *
	 * @return the annotation property map
	 */
	private HashMap<IRI, OWLAnnotationProperty> getAnnotationPropertyMap() {
		if ((this.annoPropertyMap == null)
		        || (this.annoPropertyMap.size() == 0)) {
			this.loadAnnotationPropertyMap();
		}
		return this.annoPropertyMap;
	}

	/**
	 * Gets the annotation property name.
	 *
	 * @param propCode
	 *            the property code
	 * @return the annotation property name
	 */
	public String getAnnotationPropertyName(IRI propCode) {
		final OWLAnnotationProperty prop = this.getAnnotationPropertyMap().get(
		        propCode);
		final String name = this.getRDFSLabel(prop);
		return name;
	}

	/**
	 * Gets the association map.
	 *
	 * @return the association map
	 */
	private HashMap<IRI, OWLAnnotationProperty> getAssociationMap() {

		if ((this.associationMap == null) || (this.associationMap.size() == 0)) {
			this.loadAssociationMap();
		}
		return this.associationMap;
	}

	/**
	 * Gets the associations for source.
	 *
	 * @param code
	 *            the code
	 * @return the associations for source
	 */
	Vector<AssociationVisitor> getAssociationsForSource(IRI code) {
		// final Vector<OWLAnnotation> props = new Vector<OWLAnnotation>();
		final Vector<AssociationVisitor> visitors = new Vector<AssociationVisitor>();
		final OWLClass cls = this.getOWLClass(code);
		final Collection<OWLAnnotationAssertionAxiom> annotations = EntitySearcher
		        .getAnnotationAssertionAxioms(cls, this.ontology);
		for (final OWLAnnotationAssertionAxiom anno : annotations) {
			// String key = anno.getAnnotationURI().getFragment();
			final IRI key = anno.getProperty().getIRI();
			if (this.getAssociationMap().containsKey(key)) {
				// OWLDataProperty owlProperty = propertyMap.get(key);
				AssociationVisitor visitor = new AssociationVisitor();
				// props.add(anno);
				anno.accept(visitor);
				if (visitor.isAssociation()) {
					visitors.add(visitor);
				}
			}
		}

		return visitors;

	}
	
	Vector<AssociationVisitor> getAssociationsForSource(ConceptProxy source){
		return getAssociationsForSource(IRI.create(source.getURI()));
		
	}

	/**
	 * Gets the associations for source.
	 *
	 * @param code
	 *            the code
	 * @return the associations for source
	 */
	// public Vector<AssociationVisitor> getAssociationsForSource(String code) {
	//
	// return getAssociationsForSource(makeIriFromCode(code));
	// }

	/**
	 * Gets the axiom type.
	 *
	 * @param ax
	 *            the ax
	 * @return the axiom type
	 */
	private AxiomType<?> getAxiomType(OWLAxiom ax) {
		final AxiomType<?> type = ax.getAxiomType();
		return type;
	}

	/**
	 * Gets the children for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the children for concept
	 */
	public Vector<IRI> getChildrenForConcept(IRI conceptCode) {
		final Vector<IRI> children = new Vector<IRI>();
		final OWLClass concept = this.getOWLClass(conceptCode);

		final Vector<OWLClass> descendants = this.getSubClasses(concept, true);
		for (final OWLClass cls : descendants) {
			children.add(cls.getIRI());
		}
		return children;
	}

	/**
	 * Gets the class name.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the class name
	 */
	public String getClassName(IRI conceptCode) {
		String name = "";
		final OWLClass owlClass = this.getOWLClass(conceptCode);
		return this.getRDFSLabel(owlClass);
	
	}


	/**
	 * Gets the concept code.
	 *
	 * @param ax
	 *            the ax
	 * @return the concept code
	 */
	public String getConceptCode(OWLAxiom ax) {
		final OWLAnnotationAssertionAxiomImpl axImpl = (OWLAnnotationAssertionAxiomImpl) ax;
		final OWLAnnotationSubject subject = axImpl.getSubject();
		final String code = subject.toString();

		return code;
	}

	/**
	 * Gets the concept code.
	 *
	 * @param c
	 *            the c
	 * @return the concept code
	 */
	private String getConceptCode(final OWLClass c) {
		// TODO this is messy. There has to be a better way
		final IRI code = c.getIRI();
		final String conceptCode = code.getFragment();
		final String rawCode = code.toString();

		final int beginning = rawCode.indexOf("#");
		final int end = rawCode.length();
		final String value = rawCode.substring(beginning + 1, end);
		return value;
	}

	/**
	 * Gets the data properties for class.
	 *
	 * @param conceptCode
	 *            the code
	 * @return the data properties for class
	 */
	// public Vector<OWLAnnotation> getDataPropertiesForClass(IRI conceptCode) {
	//
	// return getDataPropertiesForClass(conceptCode);

	// final Vector<OWLAnnotation> props = new Vector<OWLAnnotation>();
	// final OWLClass cls = getOWLClass(code);
	//
	// // final Set<OWLAnnotationAssertionAxiom> propAsserts = cls
	// // .getAnnotationAssertionAxioms(this.ontology);
	//
	// final Set<OWLAnnotation> annotations = cls
	// .getAnnotations(this.ontology);
	// for (final OWLAnnotation anno : annotations) {
	//
	// final String key = anno.getProperty().getIRI().getFragment();
	// if (getDataPropertyMap().containsKey(key)) {
	// // OWLDataProperty owlProperty = propertyMap.get(key);
	//
	// props.add(anno);
	// }
	// }
	//
	// Set<OWLDataProperty> o = cls.getDataPropertiesInSignature();
	// Set<OWLClassExpression> e = cls.getNestedClassExpressions();
	// e = cls.getSuperClasses(ontology);
	// OWLAnnotationProperty p = cls
	// .getOWLEntity(EntityType.ANNOTATION_PROPERTY);
	// Set<OWLClassExpression> ex = cls.getEquivalentClasses(ontology);
	// EntityType<?> et = cls.getEntityType();
	//
	// if (props.size() < 1) {
	// int debug = 0;
	// }
	//
	// return props;

	// }

	public Vector<OWLAnnotationAssertionAxiom> getDataPropertiesForClass(
	        IRI conceptCode) {

		// IRI conceptIRI = createIRI(namespace,conceptCode);
		final Vector<OWLAnnotationAssertionAxiom> props = new Vector<OWLAnnotationAssertionAxiom>();
		final OWLClass cls = this.getOWLClass(conceptCode);

		// final Set<OWLAnnotationAssertionAxiom> propAsserts = cls
		// .getAnnotationAssertionAxioms(this.ontology);

		final Collection<OWLAnnotationAssertionAxiom> annotations = EntitySearcher
		        .getAnnotationAssertionAxioms(cls, this.ontology);
		for (final OWLAnnotationAssertionAxiom anno : annotations) {

			final IRI key = anno.getProperty().getIRI();
			if (this.getDataPropertyMap().containsKey(key)) {
				// OWLDataProperty owlProperty = propertyMap.get(key);

				props.add(anno);
			}
		}

		// Set<OWLDataProperty> o = cls.getDataPropertiesInSignature();
		// // Set<OWLClassExpression> e = cls.getNestedClassExpressions();
		// // e = cls.getSuperClasses(ontology);
		// Set<OWLClassExpression> e = (Set<OWLClassExpression>)
		// EntitySearcher.getSuperClasses(cls, ontology);
		// OWLAnnotationProperty p = cls
		// .getOWLEntity(EntityType.ANNOTATION_PROPERTY);
		// Set<OWLClassExpression> ex = cls.getEquivalentClasses(ontology);
		// EntityType<?> et = cls.getEntityType();

		if (props.size() < 1) {
			int debug = 0;
		}

		return props;

	}

	/**
	 * Gets the data property map.
	 *
	 * @return the data property map
	 */
	private HashMap<IRI, OWLDataProperty> getDataPropertyMap() {
		if ((this.propertyMap == null) || (this.propertyMap.size() == 0)) {
			this.loadDataPropertyMap();
		}
		return this.propertyMap;
	}

	/**
	 * Gets the data property name.
	 *
	 * @param propertyCode
	 *            the property code
	 * @return the data property name
	 */
	public String getDataPropertyName(IRI propertyCode) {
		final OWLDataProperty prop = this.getDataPropertyMap()
		        .get(propertyCode);
		final String name = this.getRDFSLabel(prop);
		return name;
	}

	/**
	 * Gets the domain for role.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the domain for role
	 */
	public Vector<IRI> getDomainForRole(IRI roleCode) {
		final Vector<IRI> domain = new Vector<IRI>();
		// RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
		final OWLObjectProperty prop = this.roleMap.get(roleCode);
		final Collection<OWLClassExpression> rangeSet = this
		        .getDomainsForRole(prop);
		for (final OWLClassExpression desc : rangeSet) {
			domain.add(desc.asOWLClass().getIRI());
		}
		return domain;
	}

	/**
	 * Gets the domains.
	 *
	 * @param role
	 *            the role
	 * @return the domains
	 */
	private Collection<OWLClassExpression> getDomainsForRole(
	        OWLObjectProperty role) {
		Collection<OWLClassExpression> domains = EntitySearcher.getDomains(
		        role, this.ontology);
		if (domains.size() > 0) return domains;
		final Collection<OWLObjectPropertyExpression> parentExpressionSet = EntitySearcher
		        .getSuperProperties(role, this.ontology);
		if (parentExpressionSet.size() > 0) {
			final OWLObjectPropertyExpression parentExpression = parentExpressionSet
			        .iterator().next();
			final Set<OWLObjectProperty> parentPropertySet = parentExpression
			        .getObjectPropertiesInSignature();
			final OWLObjectProperty parent = parentPropertySet.iterator()
			        .next();
			domains = this.getDomainsForRole(parent);
		}
		return domains;
	}

	/**
	 * Gets the equivalent class parents.
	 *
	 * @param c
	 *            the c
	 * @return the equivalent class parents
	 */
	public Vector<OWLClass> getEquivalentClassParents(OWLClass c) {
		final Vector<OWLClass> r = new Vector<OWLClass>();
		try {
			final Set<OWLEquivalentClassesAxiom> axioms = this.ontology
			        .getEquivalentClassesAxioms(c);
			if (axioms.size() > 0) {
				for (final OWLEquivalentClassesAxiom axiom : axioms) {
					final Set<OWLClassExpression> expressions = axiom
					        .getClassExpressions();
					for (final OWLClassExpression expre : expressions) {

						final Set<OWLClassExpression> testEx = expre
						        .asConjunctSet();
						for (final OWLClassExpression test : testEx) {
							final RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
							test.accept(visitor);

							if (visitor.isRole()) {
								// r.add(visitor);
								// System.out.println(visitor.getExpressionString()
								// + " " + visitor.getFillerString());
							} else if (!test.isAnonymous()) {
								// figure out what this is - the parent?
								test.asOWLClass();
								r.add(test.asOWLClass());
							} else {
								String debug = "TRUE";
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Gets the equivalent class roles.
	 *
	 * @param c
	 *            the c
	 * @return the equivalent class roles
	 */
	public Vector<RoleDescriptionVisitor> getEquivalentClassRoles(OWLClass c) {
		final Vector<RoleDescriptionVisitor> r = new Vector<RoleDescriptionVisitor>();
		try {
			final Set<OWLEquivalentClassesAxiom> axioms = this.ontology
			        .getEquivalentClassesAxioms(c);
			if (axioms.size() > 0) {
				for (final OWLEquivalentClassesAxiom axiom : axioms) {
					final Set<OWLClassExpression> expressions = axiom
					        .getClassExpressions();
					for (final OWLClassExpression expre : expressions) {

						final Set<OWLClassExpression> testEx = expre
						        .asConjunctSet();
						for (final OWLClassExpression test : testEx) {
							final RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
							test.accept(visitor);

							if (visitor.isRole()) {
								r.add(visitor);
								// System.out.println(visitor.getExpressionString()
								// + " " + visitor.getFillerString());
							} else if (!test.isAnonymous()) {
								// figure out what this is - the parent?
								test.asOWLClass();
							} else if (test instanceof OWLObjectUnionOfImpl) {
								// TODO role group
							} else {
								String debug = "TRUE";
								// Seems to be role group - OWLObjectUnionOfImpl
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Gets the equivalent class roles.
	 *
	 * @param code
	 *            the code
	 * @return the equivalent class roles
	 */
	public Vector<RoleDescriptionVisitor> getEquivalentClassRoles(IRI code) {
		final OWLClass c = this.getOWLClass(code);
		return this.getEquivalentClassRoles(c);
	}

	/**
	 * Gets the checks if is anonymous.
	 *
	 * @param code
	 *            the code
	 * @return the checks if is anonymous
	 */
	public boolean getIsAnonymous(IRI code) {
		final OWLClass cls = this.getOWLClass(code);
		return this.isAnonymous(cls);
	}

	/**
	 * Gets the checks if is defined.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the checks if is defined
	 */
	public boolean getIsDefined(IRI conceptCode) {
		OWLClass cls = this.getOWLClass(conceptCode);
		Collection<OWLClassExpression> classes = EntitySearcher.getEquivalentClasses(cls, this.ontology);
		if(classes.size()>0){
			return true;
		}
		return EntitySearcher.isDefined(cls,
		        this.ontology);
	}

	/**
	 * Gets the checks if is transitive.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the checks if is transitive
	 */
	public boolean getIsTransitive(String roleCode) {
		return EntitySearcher.isTransitive(this.roleMap.get(roleCode),
		        this.ontology);
	}

	/**
	 * Gets the namespace.
	 *
	 * @return the namespace
	 */
	public String getNamespace() {
		return this.defaultNamespace;
	}

	/**
	 * Gets the namespace prefixes.
	 *
	 * @return the namespace prefixes
	 */
	public void getNamespacePrefixes() {

		// String defaultPrefix =
	}

	/**
	 * Gets the OWL class.
	 *
	 * @param conceptIRI
	 *            the concept IRI
	 * @return the OWL class
	 */
	private OWLClass getOWLClass(final IRI conceptIRI) {
		final OWLClass cls = this.manager.getOWLDataFactory().getOWLClass(
		        conceptIRI);
		return cls;
	}

	/**
	 * Gets the parent codes for concept.
	 *
	 * @param concept
	 *            the concept
	 * @return the parent codes for concept
	 */
	public Vector<IRI> getParentCodesForConcept(OWLClass concept) {
		final Vector<IRI> parentCodes = new Vector<IRI>();
		final Vector<OWLClass> descendants = this
		        .getSuperClasses(concept, true);
		if (descendants != null) {
			for (final OWLClass cls : descendants) {
				IRI clsIRI = cls.getIRI();
				// String code = clsIRI.getFragment();
				parentCodes.add(clsIRI);
			}
		}

		// TODO finish pulling parents from eq
		if ((parentCodes == null) || (parentCodes.size() < 1)) {
			Vector<OWLClass> parents = this.getEquivalentClassParents(concept);

			for (OWLClass parent : parents) {
				parentCodes.add(parent.getIRI());
			}
		}

		return parentCodes;
	}


	// public Vector<String> getParentCodesForConcept(String conceptCode) {
	//
	// final OWLClass concept = getOWLClass(conceptCode);
	// return getParentCodesForConcept(concept);
	// }

	public Vector<IRI> getParentCodesForConcept(IRI conceptIRI) {
		final OWLClass concept = this.getOWLClass(conceptIRI);
		return this.getParentCodesForConcept(concept);
	}

	/**
	 * Gets the parent concepts for concept.
	 *
	 * @param concept
	 *            the concept
	 * @return the parent concepts for concept
	 */
	public Vector<OWLClass> getParentConceptsForConcept(OWLClass concept) {
		Vector<OWLClass> parentCodes = new Vector<OWLClass>();
		final Vector<OWLClass> descendants = this
		        .getSuperClasses(concept, true);
		if (descendants != null) {
			for (final OWLClass cls : descendants) {
				parentCodes.add(cls);
			}
		}

		if ((parentCodes == null) || (parentCodes.size() < 1)) {
			parentCodes = this.getParentsFromEquivalentClasses(concept);
		}

		return parentCodes;
	}

	/**
	 * Gets the parent concepts for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the parent concepts for concept
	 */
	public Vector<OWLClass> getParentConceptsForConcept(IRI conceptCode) {

		final OWLClass concept = this.getOWLClass(conceptCode);
		return this.getParentConceptsForConcept(concept);
	}

	/**
	 * Gets the parent IRI for concept.
	 *
	 * @param concept
	 *            the concept
	 * @return the parent IRI for concept
	 */
	public Vector<URI> getParentIRIForConcept(OWLClass concept) {
		final Vector<URI> parentCodes = new Vector<URI>();
		final Vector<OWLClass> descendants = this
		        .getSuperClasses(concept, true);
		if (descendants != null) {
			for (final OWLClass cls : descendants) {
				IRI clsIRI = cls.getIRI();

				parentCodes.add(clsIRI.toURI());
			}
		}

		// TODO finish pulling parents from eq
		if ((parentCodes == null) || (parentCodes.size() < 1)) {
			this.getEquivalentClassParents(concept);
		}

		return parentCodes;
	}

	/**
	 * Gets the parents for annotation property.
	 *
	 * @param propCode
	 *            the prop code
	 * @return the parents for annotation property
	 */
	public Vector<IRI> getParentsForAnnotationProperty(IRI propCode) {
		final OWLAnnotationProperty prop = this.annoPropertyMap.get(propCode);

		final Vector<IRI> parentCodes = new Vector<IRI>();
		final Collection<OWLAnnotationProperty> parentExpressionSet = EntitySearcher
		        .getSuperProperties(prop, this.ontology);

		for (final OWLAnnotationProperty parent : parentExpressionSet) {
			parentCodes.add(parent.getIRI());
		}
		return parentCodes;

	}

	/**
	 * Gets the parents for concept.
	 *
	 * @param cls
	 *            the cls
	 * @return the parents for concept
	 */
	@Deprecated
	public Vector<IRI> getParentsForConcept(OWLClass cls) {
		return this.getParentCodesForConcept(cls);
	}

	/**
	 * Gets the parents for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the parents for concept
	 */
	@Deprecated
	public Vector<IRI> getParentsForConcept(IRI conceptCode) {
		return this.getParentCodesForConcept(conceptCode);

	}

	/**
	 * Gets the parents for data property.
	 *
	 * @param propCode
	 *            the prop code
	 * @return the parents for data property
	 */
	public Vector<IRI> getParentsForDataProperty(IRI propCode) {
		final OWLDataProperty prop = this.propertyMap.get(propCode);

		final Vector<IRI> parentCodes = new Vector<IRI>();
		if (prop != null) {
			final Collection<OWLDataPropertyExpression> parentExpressionSet = EntitySearcher
			        .getSuperProperties(prop, this.ontology);
			for (final OWLDataPropertyExpression parentExpression : parentExpressionSet) {
				final Set<OWLDataProperty> parentPropertySet = parentExpression
				        .getDataPropertiesInSignature();
				for (final OWLDataProperty parent : parentPropertySet) {
					parentCodes.add(parent.getIRI());
				}
			}
		}
		return parentCodes;

	}

	/**
	 * Gets the parents for role.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the parents for role
	 */
	public Vector<IRI> getParentsForRole(IRI roleCode) {
		final OWLObjectProperty prop = this.roleMap.get(roleCode);
		final Vector<IRI> parentCodes = new Vector<IRI>();
		final Collection<OWLObjectPropertyExpression> parentExpressionSet = EntitySearcher
		        .getSuperProperties(prop, this.ontology);
		for (final OWLObjectPropertyExpression parentExpression : parentExpressionSet) {
			final Set<OWLObjectProperty> parentPropertySet = parentExpression
			        .getObjectPropertiesInSignature();
			for (final OWLObjectProperty parent : parentPropertySet) {
				parentCodes.add(parent.getIRI());
			}
		}
		return parentCodes;
	}

	/**
	 * Gets the parents from equivalent classes.
	 *
	 * @param c
	 *            the c
	 * @return the parents from equivalent classes
	 */
	public Vector<OWLClass> getParentsFromEquivalentClasses(OWLClass c) {
		final Vector<OWLClass> parents = new Vector<OWLClass>();
		try {
			final Set<OWLEquivalentClassesAxiom> axioms = this.ontology
			        .getEquivalentClassesAxioms(c);
			if (axioms.size() > 0) {
				for (final OWLEquivalentClassesAxiom axiom : axioms) {
					final Set<OWLClassExpression> expressions = axiom
					        .getClassExpressions();
					for (final OWLClassExpression expre : expressions) {

						final Set<OWLClassExpression> testEx = expre
						        .asConjunctSet();
						for (final OWLClassExpression test : testEx) {
							final RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
							test.accept(visitor);

							if (visitor.isRole()) {

								// System.out.println(visitor.getExpressionString()
								// + " " + visitor.getFillerString());
							} else if (!test.isAnonymous()
							        && !c.getIRI().equals(
							                test.asOWLClass().getIRI())) {
								// figure out what this is

								parents.add(test.asOWLClass());

							}
						}
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return parents;
	}

	/**
	 * Gets the parent URI for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the parent URI for concept
	 */
	public Vector<URI> getParentURIForConcept(IRI conceptCode) {

		final OWLClass concept = this.getOWLClass(conceptCode);
		return this.getParentIRIForConcept(concept);
	}

	/**
	 * Gets the path to root.
	 *
	 * @param code
	 *            the code
	 * @return the path to root
	 */
	public NodeSet<OWLClass> getPathToRoot(IRI code) {

		NodeSet<OWLClass> supers = this.reasoner.getSuperClasses(
		        this.getOWLClass(code), false);

		return supers;

	}

	/**
	 * Gets the property name.
	 *
	 * @param propertyCode
	 *            the property code
	 * @return the property name
	 */
	@Deprecated
	public String getPropertyName(IRI propertyCode) {
		if (this.getDataPropertyMap().containsKey(propertyCode)) return this
		        .getDataPropertyName(propertyCode);
		else if (this.getAnnotationPropertyMap().containsKey(propertyCode)) return this
		        .getAnnotationPropertyName(propertyCode);
		return null;
	}

	/**
	 * Gets the property name by code.
	 *
	 * @param propCode
	 *            the property code
	 * @return the property name by code
	 */
	public String getPropertyNameByCode(IRI propCode) {
		if (this.getDataPropertyMap().containsKey(propCode)) return this
		        .getDataPropertyName(propCode);
		else if (this.getAnnotationPropertyMap().containsKey(propCode)) return this
		        .getAnnotationPropertyName(propCode);
		return null;
	}

	/**
	 * Gets the property values.
	 *
	 * @param c
	 *            the c
	 * @param propertyCode
	 *            the property code
	 * @return the property values
	 */
	private Vector<String> getPropertyValues(final OWLClass c,
	        final IRI propertyCode) {

		final Vector<String> propValues = new Vector<String>();
		try {
			// final IRI uri = createIRI(propertyCode);
			// List<java.net.URI> annoList = new Vector<java.net.URI>();
			final List<OWLAnnotationProperty> propList = new Vector<OWLAnnotationProperty>();
			final Vector<OWLLiteral> propVals = new Vector<OWLLiteral>();
			final OWLAnnotationProperty prop = this.manager.getOWLDataFactory()
			        .getOWLAnnotationProperty(propertyCode);

			for (final OWLAnnotation anno : EntitySearcher.getAnnotations(c,
			        this.ontology, prop)) {
				// annoList.add(anno.getProperty().getURI());
				propList.add(anno.getProperty());
				OWLAnnotationValue val = anno.getValue();
//				if(anno.getValue() instanceof OWLLiteralImpl){
//					propVals.add((OWLLiteralImpl) anno.getValue());
//				} else if (anno.getValue() instanceof OWLLiteralImplPlain){
//					OWLAnnotationValue Temp = anno.getValue();
//					OWLLiteral tempLit = Temp.asLiteral().get();
//					propVals.add((OWLLiteralImpl) tempLit);
//				}
				propVals.add((OWLLiteral) anno.getValue());
			}

			for (final OWLLiteral val : propVals) {
				propValues.add(val.getLiteral());

			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return propValues;
	}

	/**
	 * Gets the property values.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @param propertyCode
	 *            the property code
	 * @return the property values
	 */
	public Vector<String> getPropertyValues(IRI conceptCode, String propertyCode) {
		IRI propertyIRI = this.createIRI(this.getNamespace(), propertyCode);

		return this.getPropertyValues(conceptCode, propertyIRI);
	}

	public Vector<String> getPropertyValues(IRI conceptCode, IRI propertyCode) {
		final OWLClass c = this.getOWLClass(conceptCode);
		return this.getPropertyValues(c, propertyCode);
	}

	/**
	 *@deprecated  Cannot work. Can match multiple properties
	 *
	 * Gets the qualifiers.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @param property
	 *            the property
	 * @return the qualifiers
	 */
	public Vector<QualifierProxy> getQualifiers(IRI conceptCode,
	        PropertyProxy property) {
		OWLClass c = this.getOWLClass(conceptCode);
		// Set<OWLAnnotationAssertionAxiom> axioms = c
		// .getAnnotationAssertionAxioms(ontology);

		Vector<QualifierProxy> quals = new Vector<QualifierProxy>();
		// return quals;

		// TODO THis is a very bad method and needs to be fixed

		// Set<OWLAnnotationAssertionAxiom> oaaa =
		// ontology.getAnnotationAssertionAxioms(cls.getIRI());
		// for(OWLAnnotationAssertionAxiom oaaax: oaaa){
		// Set<OWLAnnotation> debug1=oaaax.getAnnotations();
		// for (OWLAnnotation annoQual: debug1){
		// OWLAnnotationProperty aqp = annoQual.getProperty();
		// OWLAnnotationValue aqv = annoQual.getValue();
		// String debug = "stop here";
		// }
		// AxiomType debug2 = oaaax.getAxiomType();
		// OWLAnnotationProperty anno = oaaax.getProperty();
		// OWLAnnotationSubject debug4 = oaaax.getSubject();
		// OWLAnnotationValue ov = oaaax.getValue();
		//
		//
		// String debug = "stop here";
		//

		final Vector<String> propValues = new Vector<String>();
		try {
			final IRI uri = this.createIRI(property.getCode(),
			        property.getNamespace());
			// List<java.net.URI> annoList = new Vector<java.net.URI>();
			final List<OWLAnnotationProperty> propList = new Vector<OWLAnnotationProperty>();
			final Vector<OWLLiteralImpl> propVals = new Vector<OWLLiteralImpl>();
			final OWLAnnotationProperty prop = this.manager.getOWLDataFactory()
			        .getOWLAnnotationProperty(uri);
			for (final OWLAnnotation anno : EntitySearcher.getAnnotations(c,
			        this.ontology, prop)) {
				// annoList.add(anno.getProperty().getURI());
				propList.add(anno.getProperty());
				propVals.add((OWLLiteralImpl) anno.getValue());
				Collection<OWLAnnotation> qual_annos = EntitySearcher
				        .getAnnotations(prop, this.ontology);

			}

			for (final OWLLiteralImpl val : propVals) {
				propValues.add(val.getLiteral());

			}
		} catch (final Exception e) {
			// TODO: handle exception
		}
		return null;

	}
	
	
	

//	public Vector<QualifierProxy> getQualifiers(OWLClass cls, Property property) {
//		Vector<QualifierProxy> quals = new Vector<QualifierProxy>();
//		try {
//			// Get the axioms from the class and match one to the property.
//			// Then get the annotations on that property
//			final IRI propIRI = this.createIRI(property.getCode(),
//			        property.getNamespace());
//			final OWLAnnotationProperty prop = this.manager.getOWLDataFactory()
//			        .getOWLAnnotationProperty(propIRI);
//			// Get all the properties of that type in the class
//			for (final OWLAnnotation anno : EntitySearcher.getAnnotations(cls,
//			        this.ontology, prop)) {
//				// Check if the value of the property matches the passed in
//				// Property
//				if (anno.getValue().toString().equals(property.getValue())) {
//					// /If they match, then this is the set of qualifiers we
//					// want
//					for (OWLAnnotation qual : EntitySearcher.getAnnotations(
//					        prop, this.ontology)) {
//						Qualifier q = new Qualifier(qual.getProperty().getIRI()
//						        .toString(), qual.getValue().toString());
//					}
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	// /**
	// * Gets the qualifiers.
	// *
	// * @param conceptCode
	// * the concept code
	// * @param propCode
	// * the property
	// * @param qualifier
	// * the qualifier
	// * @return the qualifiers
	// */
	// public Vector<String> getQualifiers(IRI conceptCode, IRI propCode,
	// IRI qualifier) {
	// final OWLClass c = this.getOWLClass(conceptCode);
	// final Vector<String> v = new Vector<String>();
	// for (final OWLAnnotation anno : c.getAnnotations(this.ontology)) {
	// final String annotationValue = anno.toString();
	// if (annotationValue.contains("Annotation(" + propCode)
	// || annotationValue.contains("Annotation(<"
	// + this.ontologyNamespace + "#" + propCode)) {
	// // get property value, return the new value
	// final int pos1 = annotationValue.indexOf("<" + qualifier + ">");
	// final int pos2 = annotationValue
	// .indexOf("</" + qualifier + ">");
	//
	// if ((pos1 != -1) && (pos2 != -1)) {
	// try {
	// final String value = annotationValue
	// .substring(pos1 + qualifier.length() + 2, pos2)
	// .replace("&amp;", "&").replace("&lt;", "<");
	// if (!v.contains(value)) {
	// v.add(value);
	// }
	// } catch (final Exception e) {
	// System.out
	// .println("Could not parse qualifier value from axiom for concept "
	// + c.toString());
	// System.out.println("pos1 = " + pos1
	// + qualifier.length() + 2);
	// System.out.println("pos2 = " + (pos2));
	// System.out.println(annotationValue);
	// }
	// }
	// }
	// }
	// return v;
	// }

	public Vector<String> getQualifierStrings(IRI conceptCode,
	        IRI propertyCode, IRI qualifierCode) {
		final OWLClass c = this.getOWLClass(conceptCode);

		Vector<String> v = new Vector<String>();
		for (OWLAnnotationAssertionAxiom annAx : EntitySearcher
		        .getAnnotationAssertionAxioms(c, this.ontology)) {
			Set<OWLAnnotation> rawQuals = annAx.getAnnotations();
			if (annAx.getProperty().getIRI().equals(propertyCode)) {

				for (OWLAnnotation anno : annAx.getAnnotations()) {
					if (anno.getProperty().getIRI().equals(qualifierCode)) {
						v.add(anno.getValue().toString());
					}
				}
			}
		}

		return v;
	}

	/**
	 * Gets the range for role.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the range for role
	 */
	public Vector<IRI> getRangeForRole(IRI roleCode) {
		final Vector<IRI> range = new Vector<IRI>();
		// RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
		OWLObjectProperty prop = this.roleMap.get(roleCode);
		if (prop == null) {
			prop = this.reverseRoleMap.get(roleCode);
		}
		final Collection<OWLClassExpression> rangeSet = this.getRanges(prop);
		for (final OWLClassExpression desc : rangeSet) {
			// desc.accept(visitor);
			range.add(desc.asOWLClass().getIRI());
			// range.add(desc.toString());
		}
		return range;
	}

	/**
	 * Gets the ranges.
	 *
	 * @param role
	 *            the role
	 * @return the ranges
	 */
	private Collection<OWLClassExpression> getRanges(OWLObjectProperty role) {
		Collection<OWLClassExpression> ranges = EntitySearcher.getRanges(role,
		        this.ontology);
		if (ranges.size() > 0) return ranges;
		final Collection<OWLObjectPropertyExpression> parentExpressionSet = EntitySearcher
		        .getSuperProperties(role, this.ontology);
		if (parentExpressionSet.size() > 0) {
			final OWLObjectPropertyExpression parentExpression = parentExpressionSet
			        .iterator().next();
			final Set<OWLObjectProperty> parentPropertySet = parentExpression
			        .getObjectPropertiesInSignature();
			final OWLObjectProperty parent = parentPropertySet.iterator()
			        .next();
			ranges = this.getRanges(parent);
		}
		return ranges;
	}

	/**
	 * Gets the RDF sid.
	 *
	 * @param cls
	 *            the cls
	 * @return the RDF sid
	 */
	public String getRDFSid(OWLEntity cls) {

		final String out = cls.getIRI().getFragment();
		return out;
	}

	/**
	 * Gets the RDFS label.
	 *
	 * @param cls
	 *            the cls
	 * @return the RDFS label
	 */
	public String getRDFSLabel(OWLEntity cls) {

		// String out = "";
		// final IRI labelURI = OWLRDFVocabulary.RDFS_LABEL.getIRI();
		// final OWLAnnotationProperty prop = this.manager.getOWLDataFactory()
		// .getOWLAnnotationProperty(labelURI);
		// // final Set<OWLAnnotation> debugSet = cls.getAnnotations(ontology);
		// final Set<OWLAnnotation> annotations = cls.getAnnotations(
		// this.ontology, prop);
		// for (final OWLAnnotation annotation : annotations) {
		// final OWLLiteral value = (OWLLiteral) annotation.getValue();
		// out = value.getLiteral();
		// }
		// if (!(out.length() > 0)) {
		// out = cls.getIRI().getFragment();
		// }
		// return out;

		for (OWLAnnotation a : EntitySearcher.getAnnotations(cls,
		        this.ontology, this.manager.getOWLDataFactory().getRDFSLabel())) {
			OWLAnnotationValue val = a.getValue();
			if (val instanceof OWLLiteral) return ((OWLLiteral) val).getLiteral().toString();
			else return val.toString();
		}

		return null;
	}

	// /**
	// * Gets the rdf type.
	// *
	// * @param cls
	// * the cls
	// * @return the rdf type
	// */
	// public int getRdfType(OWLEntity cls) {
	//
	// final int type = getObjectType(cls);
	// return type;
	// }

	/**
	 * Gets the referencing class entity.
	 *
	 * @param ax
	 *            the ax
	 * @param hasLiterals
	 *            the has literals
	 * @return the referencing class entity
	 */
	public OWLEntity getReferencingClassEntity(OWLAxiom ax, boolean hasLiterals) {
		OWLEntity ent = null;
		// assume only one class referenced for each axiom
		// TODO change this to not use strings
		for (final OWLEntity e : ax.getClassesInSignature()) {
			if (hasLiterals && !e.toString().equals("XMLLiteral")) {
				ent = e;
				break;
			}
			if (!hasLiterals && !e.toString().equals("string")) {
				ent = e;
				break;
			}
		}
		return ent;
	}

	/**
	 * Gets the role has parent.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the role has parent
	 */
	public boolean getRoleHasParent(IRI roleCode) {
		boolean hasParent = false;
		final OWLObjectProperty prop = this.roleMap.get(roleCode);
		if (EntitySearcher.getSuperProperties(prop, this.ontology).size() > 0) {
			hasParent = true;
		}
		return hasParent;
	}

	/**
	 * Gets the role map.
	 *
	 * @return the role map
	 */
	private HashMap<IRI, OWLObjectProperty> getRoleMap() {

		if ((this.roleMap == null) || (this.roleMap.size() == 0)) {
			this.loadRoleMap();
		}
		return this.roleMap;
	}

	/**
	 * Gets the role name by code.
	 *
	 * @param code
	 *            the code
	 * @return the role name by code
	 */
	public String getRoleNameByCode(IRI code) {
		final OWLObjectProperty oProp = this.roleMap.get(code);
		final String name = this.getRDFSLabel(oProp);
		return name;
	}

	/**
	 * Gets the roles for source.
	 *
	 * @param code
	 *            the code
	 * @return the roles for source
	 */
	public Vector<RoleDescriptionVisitor> getRolesForSource(IRI code) {
		Vector<RoleDescriptionVisitor> r = new Vector<RoleDescriptionVisitor>();
		try {
			final OWLClass c = this.getOWLClass(code);

			boolean hasEquivalent = false;
			// Set<OWLAxiom> test6 = ontology.getReferencingAxioms(c);
			final Set<OWLClassAxiom> axioms = this.ontology.getAxioms(c);

			for (final OWLClassAxiom axiom : axioms) {
				if (axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
					// Set<OWLObjectProperty> obs =
					// axiom.getObjectPropertiesInSignature();
					// OWLClassExpression asc = ((OWLSubClassOfAxiom)
					// axiom).getSubClass();
					final OWLClassExpression desc = ((OWLSubClassOfAxiom) axiom)
					        .getSuperClass();
					final RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
					desc.accept(visitor);

					if (visitor.isRole()) {
						r.add(visitor);
						// System.out.println(visitor.getExpressionString() +
						// " " + visitor.getFillerString());
					}
					//
				} else if (axiom.getAxiomType().equals(
				        AxiomType.EQUIVALENT_CLASSES)) {
					// Set<OWLObjectProperty> obs =
					// axiom.getObjectPropertiesInSignature();
					// Set<OWLClassExpression> exp =
					// axiom.getNestedClassExpressions();
					hasEquivalent = true;
					// String debug = "pause";
				}
			}
			if (hasEquivalent) {
				final Vector<RoleDescriptionVisitor> merged = new Vector<RoleDescriptionVisitor>();
				merged.addAll(r);
				merged.addAll(this.getEquivalentClassRoles(code));
				r = merged;
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Gets the roles for target.
	 *
	 * @param code
	 *            the code
	 * @return the roles for target
	 */
	public Vector<RoleDescriptionVisitor> getRolesForTarget(IRI code) {

		final Vector<RoleDescriptionVisitor> r = new Vector<RoleDescriptionVisitor>();
		// Vector<String> sources = new Vector<String>();
		try {
			final OWLClass c = this.getOWLClass(code);
			final Set<OWLAxiom> axioms = this.ontology.getReferencingAxioms(c);

			for (final OWLAxiom axiom : axioms) {
				if (axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {

					// Set<OWLObjectProperty> obs =
					// axiom.getObjectPropertiesInSignature();
					final OWLClassExpression asc = ((OWLSubClassOfAxiom) axiom)
					        .getSubClass();
					final RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
					asc.accept(visitor);
					final OWLClassExpression desc = ((OWLSubClassOfAxiom) axiom)
					        .getSuperClass();
					// RoleDescriptionVisitor visitor = new
					// RoleDescriptionVisitor();
					desc.accept(visitor);

					if (visitor.isRole()) {
						// check if code matches target.
						// System.out.println(visitor.getFillerCode());
						if (visitor.getFillerCode().equals(code)) {

							r.add(visitor);
							// System.out.println(visitor.getSourceString() +
							// " " + visitor.getExpressionString() + " " +
							// visitor.getFillerString());
						}
					}
					// RestrictionVisitor restrictionVisitor = new
					// RestrictionVisitor(
					// Collections.singleton(ontology));
					// desc.accept(restrictionVisitor);
					// System.out.println("Restricted properties for " + code +
					// ": "
					// + restrictionVisitor.getRestrictedProperties().size());
					//
					// for (OWLObjectPropertyExpression prop :
					// restrictionVisitor
					// .getRestrictedProperties()) {
					// System.out.println("    " + prop);
					//
					// }
				} else if (axiom.getAxiomType().equals(
				        AxiomType.EQUIVALENT_CLASSES)) {
					String debug = "true";
					OWLEquivalentClassesAxiom oec = (OWLEquivalentClassesAxiom) axiom;
					oec.getClassesInSignature();

					Set<OWLClassExpression> expressions = oec
					        .getClassExpressions();
					for (OWLClassExpression expression : expressions) {
						expression.toString();

					}
				} else if (axiom.getAxiomType().equals(AxiomType.DECLARATION)) {
					// Debug - just checking. We don't care about this
				} else {
					String debug = "true";
				}
			}

			// buildRoleSetForConcept(c);
			// Set<OWLSubClassOfAxiom> axioms = ontology
			// .getSubClassAxiomsForSuperClass(c);
			//
			// if (axioms.size() > 0) {
			// for (OWLSubClassOfAxiom axiom : axioms) {
			// OWLClassExpression desc = axiom.getSuperClass();
			// OWLClassExpression source = axiom.getSubClass();
			//
			// sources.add(getConceptCode(source.asOWLClass()));
			// RoleDescriptionVisitor visitor = new RoleDescriptionVisitor();
			// source.accept(visitor);
			// r.add(visitor);
			// }
			// }
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Gets the root class for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the root class for concept
	 */
	private OWLClass getRootClassForConcept(IRI conceptCode) {
		// OWLClass cls = getOWLClass(conceptCode);
		final OWLClass thisClass = this.getOWLClass(conceptCode);
		final Vector<IRI> ancestorCodes = this
		        .getAllAncestorsForConcept(conceptCode);

		final Iterator<IRI> iter = ancestorCodes.iterator();
		while (iter.hasNext()) {
			final IRI code = iter.next();
			final OWLClass cls = this.getOWLClass(code);

			if (this.isRootClass(cls) && !cls.isOWLThing()
			        && !code.equals("owl:Thing")) return cls;
		}

		return thisClass;

	}

	/**
	 * Gets the root concept code for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the root concept code for concept
	 */
	// private void removeBranch(URI classURI) {
	// try {
	// OWLEntityRemover remover = new OWLEntityRemover(manager,
	// Collections.singleton(ontology));
	// for (OWLClass cls : ontology.getClassesInSignature()) {
	// // System.out.println(cls.getURI().toString());
	// if (cls.getIRI().equals(classURI)) {
	// // System.out.println("Now printing all descendants of " +
	// // cls);
	// Vector<OWLClass> descendants = getSubClasses(cls, false);
	// if (descendants != null) {
	// for (OWLClass dClass : descendants) {
	// // System.out.println("Removing " + odesc );
	// IRI test = dClass.getIRI();
	// removedClasses.add(test);
	// dClass.accept(remover);
	// }
	// } else {
	// // System.out.println("No children to remove from " +
	// // cls + ".");
	// }
	// // System.out.println( "Removing branch class " + cls);
	// cls.accept(remover);
	// }
	// }
	// // System.out.println("Applying branch removal changes...");
	// manager.applyChanges(remover.getChanges());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Gets the root concept code for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return The Code for the rootClass of the tree where this concept belongs
	 */
	public IRI getRootConceptCodeForConcept(IRI conceptCode) {
		final OWLClass cls = this.getRootClassForConcept(conceptCode);
		// final String rootCode = getConceptCode(cls);
		return cls.getIRI();
		// return rootCode;
	}

	/**
	 * Gets the root concept codes.
	 *
	 * @return the root concept codes
	 */
	public Vector<IRI> getRootConceptCodes() {
		final Vector<OWLClass> roots = this.getRootConcepts();
		final Vector<IRI> conceptCodes = new Vector<IRI>();
		for (final OWLClass concept : roots) {
			// final String conceptCode = getConceptCode(concept);
			conceptCodes.add(concept.getIRI());
		}
		return conceptCodes;
	}

	/**
	 * Gets the root concepts.
	 *
	 * @return the root concepts
	 */
	private Vector<OWLClass> getRootConcepts() {
		if (this.rootMap.size() > 0) return this.rootMap;
		this.loadRootConcepts();
		return this.rootMap;
	}

	/**
	 * Gets the sole property value.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @param propCode
	 *            the property
	 * @return the sole property value
	 */
	public String getSolePropertyValue(IRI conceptCode, IRI propCode) {
		final OWLClass c = this.getOWLClass(conceptCode);

		Vector<String> values = this.getPropertyValues(c, propCode);
		if (values != null && values.size() > 0) return values.get(0);
		return null;

		// String v = new String("");
		// for (final OWLAnnotation anno : c.getAnnotations(this.ontology)) {
		// final String annotationValue = anno.toString();
		// if (annotationValue.contains("Annotation(" + propCode)
		// || annotationValue.contains("Annotation(<"
		// + this.ontologyNamespace + "#" + propCode)) {
		// // get property value, return the new value
		// // The annotation can be either Annotation(property value or
		// // Annotation(<namespace>value
		// int beginning = 0;
		// if (annotationValue.contains(this.ontologyNamespace)) {
		// beginning = annotationValue.indexOf(">") + 3;
		// } else {
		// beginning = annotationValue.indexOf("(");
		// beginning = beginning + propCode.length() + 3;
		// }
		//
		// final int end = annotationValue.indexOf("^^");
		// v = annotationValue.substring(beginning, end - 1)
		// .replace("&amp;", "&").replace("&lt;", "<");
		// break;
		// }
		// }
		// return v;
	}

	/**
	 * Gets the sub classes.
	 *
	 * @param cls
	 *            the cls
	 * @param directOnly
	 *            the direct only
	 * @return the sub classes
	 */
	private Vector<OWLClass> getSubClasses(OWLClass cls, boolean directOnly) {
		final Vector<OWLClass> vChildren = new Vector<OWLClass>();
		final Vector<OWLClass> vChildAndEquiv = new Vector<OWLClass>();
		if (this.reasoner != null) {
			for (final OWLClass subCls : this.reasoner.getSubClasses(cls,
			        directOnly).getFlattened()) {
				if (!vChildren.contains(subCls)) {
					vChildren.add(subCls);
				}
			}
		}

//		else {

			final Collection<OWLClassExpression> ods = EntitySearcher
			        .getSubClasses(cls, this.ontology);
			final OWLClassExpression[] children = ods
			        .toArray(new OWLClassExpression[ods.size()]);
			if (children==null || children.length == 0) {
				return vChildAndEquiv;
			}
			for (final OWLClassExpression child : children) {
				if( child.asOWLClass().equals(cls) ) {
					System.out.println(cls.toStringID() + " is a child of itself!!!");
					return vChildAndEquiv;
				}
				vChildAndEquiv.add(child.asOWLClass());
			}
			if (!directOnly) {
				for (int i = 0; i < vChildren.size(); i++) {
//					System.out.println(vChildren.elementAt(i).asOWLClass().toStringID());
					final Vector<OWLClass> w = this.getSubClasses(vChildren
					        .elementAt(i).asOWLClass(), false);
					if (w != null) {
						for (int j = 0; j < w.size(); j++) {
							if ((w.elementAt(j) != null)
							        && !vChildAndEquiv.contains(w.elementAt(j))) {
								vChildAndEquiv.add(w.elementAt(j));
							}
						}
					}
				}
			}
//		}
		if(vChildAndEquiv.size()!= vChildren.size()){
			String debug="Stop";
		}
		return vChildren;
	}

	/**
	 * Gets the super classes.
	 *
	 * @param cls
	 *            the cls
	 * @param directOnly
	 *            the direct only
	 * @return the super classes
	 */
	private Vector<OWLClass> getSuperClasses(OWLClass cls, boolean directOnly) {
		if(cls.isOWLNothing()){
			return new Vector<OWLClass>();
		}
		final Vector<OWLClass> vParents = new Vector<OWLClass>();
		if (this.reasoner != null) {
			for (final OWLClass subCls : this.reasoner.getSuperClasses(cls,
			        directOnly).getFlattened()) {
				if (!vParents.contains(subCls) && !subCls.isOWLThing()) {
					vParents.add(subCls);
				}
			}
		}
		if (vParents.size() < 1) {
			final Collection<OWLClassExpression> ods = EntitySearcher
			        .getSuperClasses(cls, this.ontology);
			final OWLClassExpression[] parents = ods
			        .toArray(new OWLClassExpression[ods.size()]);
			if (parents.length == 0) return vParents;

			for (final OWLClassExpression parent : parents) {
				if (!parent.isAnonymous()) {
					vParents.add(parent.asOWLClass());
				}
			}
			if (!directOnly) {
				for (int i = 0; i < vParents.size(); i++) {
					final Vector<OWLClass> w = this.getSuperClasses(vParents
					        .elementAt(i).asOWLClass(), false);
					if (w != null) {
						for (int j = 0; j < w.size(); j++) {
							if ((w.elementAt(j) != null)
							        && !vParents.contains(w.elementAt(j))) {
								vParents.add(w.elementAt(j).asOWLClass());
							}
						}
					}
				}
			}
		}
		return vParents;
	}

	/**
	 * Gets the tag value.
	 *
	 * @param value
	 *            the value
	 * @param tagName
	 *            the tag name
	 * @return the tag value
	 */
	public String getTagValue(String value, String tagName) {
		String tagValue = null;
		// TODO change this to use an XML parser

		final int pos1 = value.indexOf(tagName + ">");
		final int pos2 = value.indexOf("</", pos1);
		if ((pos1 != -1) && (pos2 != -1)) {
			tagValue = value.substring(pos1 + 1 + tagName.length(), pos2);
		}
		return tagValue;

	}

	/**
	 * Checks for same code.
	 *
	 * @param iri1
	 *            the iri 1
	 * @param iri2
	 *            the iri 2
	 * @return true, if successful
	 */
	private boolean hasSameCode(IRI iri1, IRI iri2) {
		String code1 = iri1.getFragment();
		String code2 = iri2.getFragment();
		String prefix1 = iri1.getNamespace();
		String prefix2 = iri2.getNamespace();

		if (code1.equals(code2)) {
			if (prefix1.equals(prefix2)) return true;
			else {
				if (prefix1.endsWith("#") || prefix1.endsWith("/")) {
					prefix1 = prefix1.substring(0, prefix1.length() - 1);
				}
				if (prefix2.endsWith("#") || prefix2.endsWith("/")) {
					prefix2 = prefix2.substring(0, prefix2.length() - 1);
				}
				if (prefix1.equals(prefix2)) return true;
			}
		}
		return false;
	}

	/**
	 * Instantiate maps.
	 */
	private void instantiateMaps() {
		this.loadAnnotationPropertyMap();
		this.loadAssociationMap();
		this.loadDataPropertyMap();
		this.loadRoleMap();
		this.loadRootConcepts();
//		this.getAllConcepts();
	}

	/**
	 * Checks if is anonymous.
	 *
	 * @param cls
	 *            the cls
	 * @return true, if is anonymous
	 */
	public boolean isAnonymous(OWLClass cls) {
		return cls.isAnonymous();
	}

	/**
	 * Checks if is deprecated.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return true, if is deprecated
	 */
	public boolean isDeprecated(final IRI conceptCode) {
		try {
			OWLClass cls = this.getOWLClass(conceptCode);
			Collection<OWLAnnotation> Annos = EntitySearcher.getAnnotations(
			        cls, this.ontology);
			for (OWLAnnotation anno : Annos) {
				if (anno.isDeprecatedIRIAnnotation()) return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Checks if is empty.
	 *
	 * @param ax
	 *            the ax
	 * @return true, if is empty
	 */
	public boolean isEmpty(OWLAxiom ax) {
		boolean test = false;

		// EntityAnnotationAxiom(OBI_0000639 Annotation(IAO_0000111 ""^^string))
		// EntityAnnotationAxiom(OBI_0000481 Annotation(IAO_0000118 ""@en))
		// EntityAnnotationAxiom(OBI_0400008 Comment( ""@en))
		// TODO Check if there is another way to do this that doesn't require
		// strings
		if (ax.toString().contains(" \"\"^^")
		        || ax.toString().contains(" \"\"@")) {
			// System.out.println(ax);
			test = true;
		}
		return test;
	}

	/**
	 * Checks if is in retired branch.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @param deprecatedBranch
	 *            the deprecated branch
	 * @return true, if is in retired branch
	 */
	public boolean isInRetiredBranch(IRI conceptCode, IRI deprecatedBranch) {
		// currently the only way to do this is to see if it is
		// a subclass of ObsoleteClass.
		// In OWL2 there will be an annotation to indicate
		// deprecated classes

		OWLClass cls = this.getRootClassForConcept(conceptCode);
		if (cls.getIRI().equals(deprecatedBranch)) return true;

		// final String rootConcept =
		// getConceptCode(getRootClassForConcept(conceptCode));
		// final IRI rootIRI = getRootClassForConcept(conceptCode).getIRI();
		// if (rootIRI.equals(deprecatedBranch)) {
		// return true;
		// }

		// currently the only way to do this is to see if it is
		// a subclass of ObsoleteClass.
		// In OWL2 there will be an annotation to indicate
		// deprecated classes
		return false;
	}

	/**
	 * Checks if is role transitive.
	 *
	 * @param code
	 *            the code
	 * @return true, if is role transitive
	 */
	public boolean isRoleTransitive(IRI code) {
		final OWLObjectProperty oProp = this.roleMap.get(code);
		return EntitySearcher.isTransitive(oProp, this.ontology);
	}

	// /**
	// * Checks if is root.
	// *
	// * @param c
	// * the c
	// * @return true, if is root
	// */
	// private boolean isRoot(OWLClass c) {
	// final Vector<OWLClass> parents = getParentConceptsForConcept(c);
	// for (final OWLClass parent : parents) {
	// if (parent.isOWLThing()) {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * Checks if is root class.
	 *
	 * @param c
	 *            the c
	 * @return true, if is root class
	 */
	private boolean isRootClass(OWLClass c) {
		// final Set<OWLOntology> ontologies = this.manager.getOntologies();
		// final SimpleRootClassChecker rootChecker = new
		// SimpleRootClassChecker(
		// ontologies);
		// final boolean isRoot = rootChecker.isRootClass(c);
		if (this.getRootConcepts().contains(c)) return true;
		return false;
	}

	/**
	 * Checks if is root class.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return true, if is root class
	 */
	public boolean isRootClass(IRI conceptCode) {
		final OWLClass c = this.getOWLClass(conceptCode);
		return this.isRootClass(c);
	}


	/**
	 * Load annotation property map.
	 */
	private void loadAnnotationPropertyMap() {
		final Set<OWLAnnotationProperty> annoProps = this.ontology
		        .getAnnotationPropertiesInSignature();
		for (final OWLAnnotationProperty prop : annoProps) {
			if (!this.ontology.containsObjectPropertyInSignature(prop.getIRI())) {
				IRI propCode = prop.getIRI();
				// if (propCode == null) {
				// propCode = get_IRIwoHash_getFragment(prop.getIRI());
				// }
				this.annoPropertyMap.put(propCode, prop);
			}

			// for debugging.
			// String label = getRDFSLabel(prop);
			// Set<OWLAnnotationProperty> subs =
			// prop.getSubProperties(ontology);
			// Set<OWLAnnotationProperty> sups =
			// prop.getSuperProperties(ontology);

		}
	}

	/**
	 * Load association map.
	 */
	private void loadAssociationMap() {

		final OWLDataFactory tmpFactory = this.manager.getOWLDataFactory();

		for (final OWLAnnotationProperty u : this.ontology
		        .getAnnotationPropertiesInSignature()) {
//			if (this.ontology.containsObjectPropertyInSignature(u.getIRI())) {
//				final IRI roleCode = u.getIRI();
//				final OWLObjectProperty entity = tmpFactory
//				        .getOWLObjectProperty(u.getIRI());
//				this.associationMap.put(u.getIRI(), u);
//			}
			//TODO this is magic.
			if(u.getIRI().getFragment().startsWith("A")){
				this.associationMap.put(u.getIRI(), u);
			}
		}
		// Now that we have the annotations mapped, fix the actual declarations
		// in the classes
		this.fixAssociations();
	}

	/**
	 * Load data property map.
	 */
	private void loadDataPropertyMap() {
		final Set<OWLDataProperty> props = this.ontology
		        .getDataPropertiesInSignature();

		for (final OWLDataProperty prop : props) {
			final IRI propCode = prop.getIRI();
			this.getRDFSLabel(prop);
			this.propertyMap.put(propCode, prop);
		}

	}

	/**
	 * Load role map.
	 */
	private void loadRoleMap() {
		if ((this.associationMap == null) || (this.associationMap.size() == 0)) {
			this.loadAssociationMap();
		}
		final Set<OWLObjectProperty> oProps = this.ontology
		        .getObjectPropertiesInSignature(true);

		for (final OWLObjectProperty entityAx : oProps) {
			// final EntityType eType = entityAx.getEntityType();
			final Set<OWLObjectProperty> props = entityAx
			        .getObjectPropertiesInSignature();
			final OWLObjectProperty entity = (OWLObjectProperty) props
			        .toArray()[0];
			final IRI roleCode = entity.getIRI();
			final String roleName = this.getRDFSLabel(entity);
			// String type = getRdfType(entity);

			if (!this.associationMap.containsKey(roleCode)) {
				this.roleMap.put(roleCode, entity);
				this.reverseRoleMap.put(roleName, entity);
			}
		}
	}

	/**
	 * Load root concepts.
	 */
	private void loadRootConcepts() {
		final Set<OWLOntology> ontologies = this.manager.getOntologies();
		// SimpleRootClassChecker rootChecker = new SimpleRootClassChecker(
		// ontologies);
		Integer counter = 0;
		final Set<OWLClass> allClasses = this.getAllConcepts();
		final Integer conceptCount = allClasses.size();
		for (final OWLClass owlClass : allClasses) {
			counter++;
			// if (rootChecker.isRootClass(owlClass)) {
			if (this.myIsRootClass(owlClass)) {
				this.rootMap.add(owlClass);
			}
			if ((counter % 10000) == 0) {
				System.out.println("  Reviewed " + counter
				        + " concepts out of " + conceptCount);
			}
		}
	}

	/**
	 * Make iri from code.
	 *
	 * @param code
	 *            the code
	 * @return the iri
	 */
	// private static class RestrictionVisitor extends
	// OWLClassExpressionVisitorAdapter {
	// private final Set<OWLClass> processedClasses;
	// private final Set<OWLObjectPropertyExpression> restrictedProperties;
	// private final Set<OWLOntology> onts;
	//
	// public RestrictionVisitor(Set<OWLOntology> onts) {
	// restrictedProperties = new HashSet<OWLObjectPropertyExpression>();
	// processedClasses = new HashSet<OWLClass>();
	// this.onts = onts;
	// }
	//
	// public Set<OWLObjectPropertyExpression> getRestrictedProperties() {
	// return restrictedProperties;
	// }
	//
	// @Override
	// public void visit(OWLClass desc) {
	// if (!processedClasses.contains(desc)) {
	// // If we are processing inherited restrictions then we
	// // recursively visit named supers. Note that we need to keep
	// // track of the classes that we have processed so that we don't
	// // get caught out by cycles in the taxonomy
	// processedClasses.add(desc);
	// for (OWLOntology ont : onts) {
	// for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass(desc)) {
	// ax.getSuperClass().accept(this);
	// }
	// }
	// }
	// }
	//
	// @Override
	// public void visit(OWLObjectSomeValuesFrom desc) {
	// // This method gets called when a class expression is an existential
	// // (someValuesFrom) restriction and it asks us to visit it
	// restrictedProperties.add(desc.getProperty());
	// }
	// }

	public IRI makeIriFromCode(String code) {
		if (this.isSimpleCode(code)) {
			final String iriSeed = this.getNamespace() + "#" + code;
			return IRI.create(iriSeed);
		} else
			return IRI.create(code);
	}

	/**
	 * Make uri from code.
	 *
	 * @param code
	 *            the code
	 * @return the uri
	 */
	public URI makeUriFromCode(String code) {
		if (this.isSimpleCode(code)) {
			final String uriSeed = this.getNamespace() + "#" + code;
			return URI.create(uriSeed);
		} else
			return URI.create(code);
	}

	private boolean isSimpleCode(String code) {
		if (code.contains("#") || code.contains("/")) return false;
		return true;
	}

	/**
	 * My is root class.
	 *
	 * @param owlClass
	 *            the owl class
	 * @return true, if successful
	 */
	public boolean myIsRootClass(OWLClass owlClass) {
		boolean isRoot = false;
		final Vector<OWLClass> parents = this
		        .getParentConceptsForConcept(owlClass);
		// if (owlClass.getIRI().getFragment().equals("Activity")) {
		// int i = 0;
		// }
		if ((parents != null) && (parents.size() > 0)) {
			for (final OWLClass parent : parents) {

				if (parent.isOWLThing() && (parents.size() == 1)) {
					isRoot = true;
					System.out.println("Root " + owlClass.getIRI().toString());
				} else if (parent.getIRI().toString().contains("Thing")
				        && (parents.size() == 1)) {
					System.out.println("Debug root "
					        + parent.getIRI().toString());
				} else {
					// System.out.println(parent.getIRI().toString());
				}
			}
		} else {
			System.out.println("class " + owlClass.getIRI().toString()
			        + " has no parent");
			isRoot = true;
		}

		return isRoot;
	}

	/**
	 * Refresh maps.
	 */
	private void refreshMaps() {
		this.reloadAnnotationPropertyMap();
		this.reloadAssociationMap();
		// reloadDataPropertyMap();
		// reloadRoleMap();
		// reloadRootConcepts();
	}

	/**
	 * Refresh root nodes.
	 */
	public void refreshRootNodes() {
		this.rootMap = new Vector<OWLClass>();
		this.loadRootConcepts();
	}

	/**
	 * Reload annotation property map.
	 */
	private void reloadAnnotationPropertyMap() {
		this.annoPropertyMap = new HashMap<IRI, OWLAnnotationProperty>();
		this.loadAnnotationPropertyMap();
	}

	/**
	 * Reload association map.
	 */
	private void reloadAssociationMap() {
		this.associationMap = new HashMap<IRI, OWLAnnotationProperty>();
		this.loadAssociationMap();
	}

	/**
	 * Removes the branch.
	 *
	 * @param conceptCode
	 *            the concept code
	 */
	public void removeBranch(IRI conceptCode) {
		// final IRI uri = createIRI(conceptCode);
		// final URI uri = createURI(conceptCode);
		this.removeBranch(conceptCode.toURI());
	}

	/**
	 * Removes the branch.
	 *
	 * @param inputURI
	 *            the input URI
	 */
	public void removeBranch(URI inputURI) {
		try {
			IRI inputIRI = IRI.create(inputURI);
			final OWLEntityRemover remover = new OWLEntityRemover(
			        Collections.singleton(this.ontology));
			Set<OWLAxiom> axiomsToRemove = new HashSet<OWLAxiom>();
			OWLClass clz = this.getOWLClass(inputIRI);
			final Vector<OWLClass> descendants = this.getSubClasses(clz, false);
			if (descendants != null) {
				for (final OWLClass dClass : descendants) {
					// System.out.println("Removing " + odesc );
					// Set<OWLDeclarationAxiom> axiomsDec = ontology
					// .getDeclarationAxioms(dClass);
					// for (OWLAxiom ax: axiomsDec) {
					// axiomsToRemove.add(ax);
					// }
					// Set<OWLClassAxiom> axiomsGet =
					// ontology.getAxioms(dClass);
					// for (OWLAxiom ax:axiomsGet) {
					// axiomsToRemove.add(ax);
					// }
					Set<OWLAxiom> axiomsRef = this.ontology
					        .getReferencingAxioms(dClass);
					for (OWLAxiom ax : axiomsRef) {
						axiomsToRemove.add(ax);
					}
					// for (OWLAxiom ax : ontology.getAxioms()) {
					// if (ax.getSignature().contains(dClass)) {
					// axiomsToRemove.add(ax);
					// }
					// }
					final IRI test = dClass.getIRI();
					this.removedClasses.add(test);
					dClass.accept(remover);
				}
			}
			clz.accept(remover);
			ChangeApplied changes = this.manager.removeAxioms(this.ontology,
			        axiomsToRemove);
			// this.manager.applyChanges(changes);
			this.manager.applyChanges(remover.getChanges());

			// for (final OWLClass cls : this.ontology.getClassesInSignature())
			// {
			// // System.out.println(cls.getURI().toString());
			// if (hasSameCode(cls.getIRI(), inputIRI)) {
			//
			// final Vector<OWLClass> descendants = getSubClasses(cls,
			// false);
			// if (descendants != null) {
			// for (final OWLClass dClass : descendants) {
			// // System.out.println("Removing " + odesc );
			// final IRI test = dClass.getIRI();
			// this.removedClasses.add(test);
			// dClass.accept(remover);
			// }
			// } else {
			// // System.out.println("No children to remove from " +
			// // cls + ".");
			// }
			// // System.out.println( "Removing branch class " + cls);
			// cls.accept(remover);
			// }
			// }
			// System.out.println("Applying branch removal changes...");

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the data.
	 *
	 * @param orig
	 *            the orig
	 * @param tagName
	 *            the tag name
	 * @return the string
	 */
	private String removeData(String orig, String tagName) {
		// TODO see if I can rewrite this to rely less on characters checks
		// http://www.java-tips.org/java-se-tips/org.xml.sax/accessing-character-data-cdata-of-xml-element.html
		String newValue, s1, s2 = new String();
		final int pos1 = orig.indexOf("<" + tagName + ">");
		final int pos2 = orig.indexOf("</" + tagName + ">");
		if ((pos1 != -1) && (pos2 != -1)) {
			s1 = orig.substring(0, pos1);
			s2 = orig.substring(pos2 + 3 + tagName.length(), orig.length()); // 3
			// is
			// </
			// >
			newValue = s1 + s2;
		} else {
			newValue = orig;
		}
		return newValue;
	}

	/**
	 * Removes the empty.
	 */
	public void removeEmpty() {
		try {
			final Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
			for (final OWLAxiom ax : this.ontology.getAxioms()) {
				if (this.isEmpty(ax)) {
					changes.add(new RemoveAxiom(this.ontology, ax));
				}
			}
			final List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
			        changes);
			this.manager.applyChanges(list);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the individuals.
	 */
	public void removeIndividuals() {
		try {
			final OWLEntityRemover remover = new OWLEntityRemover(
			        Collections.singleton(this.ontology));
			for (final OWLNamedIndividual oi : this.ontology
			        .getIndividualsInSignature()) {
				// oi.accept((OWLIndividualVisitor) remover);
				oi.accept(remover);

			}

			this.manager.applyChanges(remover.getChanges());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the internal tag.
	 *
	 * @param tag
	 *            the tag
	 * @param hasLiterals
	 *            the has literals
	 */
	public void removeInternalTag(String tag, boolean hasLiterals) {
		if (tag != null) {
			final Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
			// TODO it never checks the property id, just eliminates the tag.
			// Fix this.
			for (final OWLAxiom ax : this.ontology.getAxioms()) {
				if (ax.isOfType(AxiomType.ANNOTATION_ASSERTION)
				        && ax.toString().contains(tag)) {
					final OWLAnnotationAssertionAxiomImpl axImpl = (OWLAnnotationAssertionAxiomImpl) ax;
					final OWLAnnotationSubject subject = axImpl.getSubject();
					final OWLAnnotationPropertyImpl prop = (OWLAnnotationPropertyImpl) axImpl
					        .getProperty();
					final OWLLiteralImpl value = (OWLLiteralImpl) axImpl
					        .getValue();

					final String origValue = value.getLiteral();
					final String newValue = this.removeData(origValue, tag);

					// delete old axiom
					changes.add(new RemoveAxiom(this.ontology, ax));

					// create new axiom
					final OWLDataFactory tmpFactory = this.manager
					        .getOWLDataFactory();
					OWLDatatype odt;

					if (hasLiterals) {
						odt = tmpFactory.getOWLDatatype(IRI
						        .create(OwlApiLayer.typeConstantLiteral));
					} else {
						odt = tmpFactory.getOWLDatatype(IRI
						        .create(OwlApiLayer.typeConstantString));
					}

					// OWLTypedLiteral otl = tmpFactory.getOWLTypedLiteral(
					// newValue, odt);
					final OWLLiteral otl = tmpFactory.getOWLLiteral(newValue,
					        odt);

					final OWLAnnotation newAnno = tmpFactory.getOWLAnnotation(
					        prop, otl);

					final OWLAxiom ax1 = tmpFactory
					        .getOWLAnnotationAssertionAxiom(subject, newAnno);
					changes.add(new AddAxiom(this.ontology, ax1));

				}
			}
			if (!changes.isEmpty()) {
				final List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
				        changes);
				try {
					// System.out.println("Applying changed complex properties...");
					this.manager.applyChanges(list);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.err
			        .println("Invalid input.  Property and tag must have values");
		}
	}

	/**
	 * Removes the internal tag.
	 *
	 * @param propertyName
	 *            the property name
	 * @param tag
	 *            the tag
	 * @param hasLiterals
	 *            the has literals
	 */
	public void removeInternalTag(String propertyName, String tag,
	        boolean hasLiterals) {
		if ((propertyName != null) && (tag != null)) {
			final Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
			// TODO it never checks the property id, just eliminates the tag.
			// Fix this.
			for (final OWLAxiom ax : this.ontology.getAxioms()) {
				if (ax.isOfType(AxiomType.ANNOTATION_ASSERTION)
				        && ax.toString().contains(tag)) {
					final OWLAnnotationAssertionAxiomImpl axImpl = (OWLAnnotationAssertionAxiomImpl) ax;
					final OWLAnnotationSubject subject = axImpl.getSubject();
					OWLAnnotationProperty prop = axImpl.getProperty();
					OWLAnnotationValue anValue = axImpl.getValue();
					String sAnValue = anValue.toString();
					// final OWLAnnotationPropertyImpl prop =
					// (OWLAnnotationPropertyImpl) axImpl
					// .getProperty();
//					final OWLLiteralImplNoCompression value = (OWLLiteralImplNoCompression) axImpl
//					        .getValue();
					final OWLLiteralImplPlain value = (OWLLiteralImplPlain) axImpl
					        .getValue();


					final String origValue = value.getLiteral();
					final String newValue = this.removeData(origValue, tag);

					// delete old axiom
					changes.add(new RemoveAxiom(this.ontology, ax));

					// create new axiom
					final OWLDataFactory tmpFactory = this.manager
					        .getOWLDataFactory();
					OWLDatatype odt;

					if (hasLiterals) {
						odt = tmpFactory.getOWLDatatype(IRI
						        .create(OwlApiLayer.typeConstantLiteral));
					} else {
						odt = tmpFactory.getOWLDatatype(IRI
						        .create(OwlApiLayer.typeConstantString));
					}

					// OWLTypedLiteral otl = tmpFactory.getOWLTypedLiteral(
					// newValue, odt);
					final OWLLiteral otl = tmpFactory.getOWLLiteral(newValue,
					        odt);
					// OWLLiteral otl = tmpFactory.getOWLLiteral(newValue, odt);

					final OWLAnnotation newAnno = tmpFactory.getOWLAnnotation(
					        prop, otl);

					final OWLAxiom ax1 = tmpFactory
					        .getOWLAnnotationAssertionAxiom(subject, newAnno);
					changes.add(new AddAxiom(this.ontology, ax1));

				}
			}
			if (!changes.isEmpty()) {
				final List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(
				        changes);
				try {
					// System.out.println("Applying changed complex properties...");
					this.manager.applyChanges(list);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.err
			        .println("Invalid input.  Property and tag must have values");
		}
	}

	/**
	 * Removes all instances of the given qualifer on the given property Used to
	 * remove things like Definition Reviewer Name
	 * 
	 * @param property
	 * @param qualifier
	 */
	public void removeQualifier(IRI property, IRI qualifier) {
		for (OWLClass cls : this.ontology.getClassesInSignature()) {
			this.removeQualifier(cls, property, qualifier);
		}
		for (OWLDataProperty dp : this.ontology.getDataPropertiesInSignature()) {
			this.removeQualifier(dp, property, qualifier);
		}
		for (OWLObjectProperty op : this.ontology
				.getObjectPropertiesInSignature()) {
			this.removeQualifier(op, property, qualifier);
		}
		for (OWLAnnotationProperty ap : this.ontology
				.getAnnotationPropertiesInSignature()) {
			this.removeQualifier(ap, property, qualifier);
		}
		for (OWLNamedIndividual ind : this.ontology.getIndividualsInSignature()) {
			this.removeQualifier(ind, property, qualifier);
		}
		for (OWLDatatype dt : this.ontology.getDatatypesInSignature()) {
			this.removeQualifier(dt, property, qualifier);
		}
	}

	private void removeQualifier(OWLEntity cls, IRI property, IRI qualifier) {

		// List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		OWLDataFactory factory = this.manager.getOWLDataFactory();

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAnnotationAssertionAxiom annAx : EntitySearcher
				.getAnnotationAssertionAxioms(cls.getIRI(), this.ontology)) {
			boolean match = false;
			Set<OWLAnnotation> existingAnnotations = annAx.getAnnotations();

			if (annAx.getProperty().getIRI().equals(property)) {

				for (OWLAnnotation anno : annAx.getAnnotations()) {
					if (anno.getProperty().getIRI().equals(qualifier)) {
						existingAnnotations.remove(anno);
						match = true;
					}
				}
			}

			if (match) {
				OWLAnnotation newAnnotation = factory.getOWLAnnotation(
						factory.getOWLAnnotationProperty(annAx.getProperty()
								.getIRI()), factory.getOWLLiteral(annAx
										.getValue().toString()));
				Set<OWLAnnotation> newAnnotations = new HashSet<OWLAnnotation>();
				for (OWLAnnotation newAnno : existingAnnotations) {
					newAnnotations.add(newAnno);
				}
				OWLAxiom annotatedAxiom = factory
						.getOWLAnnotationAssertionAxiom(cls.getIRI(),
								newAnnotation, newAnnotations);
				changes.add(new RemoveAxiom(this.ontology, annAx));
				changes.add(new AddAxiom(this.ontology, annotatedAxiom));

			}
		}
		if (changes.size() > 0) {
			this.manager.applyChanges(changes);
		}

	}

	/**
	 * From owltools
	 * https://github.com/owlcollab/owltools/blob/master/OWLTools-Core
	 * /src/main/java/owltools/graph/AxiomAnnotationTools.java
	 *
	 * Retrieve the literal values for the axiom annotations with the given
	 * annotation property IRI.
	 *
	 * @param iri
	 * @param axiom
	 * @return literal values or null
	 */
	public static List<String> getAxiomAnnotationValues(IRI iri, OWLAxiom axiom) {
		List<String> result = null;
		for (OWLAnnotation annotation : axiom.getAnnotations()) {
			OWLAnnotationProperty property = annotation.getProperty();
			if (property.getIRI().equals(iri)) {
				OWLAnnotationValue value = annotation.getValue();
				if (value instanceof OWLLiteral) {
					String literal = ((OWLLiteral) value).getLiteral();
					if (result == null) {
						result = Collections.singletonList(literal);
					} else if (result.size() == 1) {
						result = new ArrayList<String>(result);
						result.add(literal);
					} else {
						result.add(literal);
					}
				}
			}
		}
		return result;
	}

	/**
	 * From owltools
	 * https://github.com/owlcollab/owltools/blob/master/OWLTools-Core
	 * /src/main/java/owltools/graph/AxiomAnnotationTools.java
	 *
	 * Update the given axiom to a new set of axiom annotation.<br>
	 * <b>Side effect</b>: This removes the old axiom and adds the new axiom to
	 * the given ontology. The method also returns the new axiom to enable
	 * chaining.
	 *
	 * @param axiom
	 * @param annotations
	 * @param ontology
	 * @return newAxiom
	 */
	public static OWLAxiom changeAxiomAnnotations(OWLAxiom axiom,
	        Set<OWLAnnotation> annotations, OWLOntology ontology) {
		final OWLOntologyManager manager = ontology.getOWLOntologyManager();
		final OWLDataFactory factory = manager.getOWLDataFactory();
		final OWLAxiom newAxiom = changeAxiomAnnotations(axiom, annotations,
		        factory);
		manager.removeAxiom(ontology, axiom);
		manager.addAxiom(ontology, newAxiom);
		return newAxiom;
	}

	/**
	 * From owltools
	 * https://github.com/owlcollab/owltools/blob/master/OWLTools-Core
	 * /src/main/java/owltools/graph/AxiomAnnotationTools.java
	 *
	 * Update the given axiom to the new set of axiom annotation. Recreates the
	 * axiom with the new annotations using the given factory.
	 *
	 * @param axiom
	 * @param annotations
	 * @param factory
	 * @return newAxiom
	 */
	public static OWLAxiom changeAxiomAnnotations(OWLAxiom axiom,
	        Set<OWLAnnotation> annotations, OWLDataFactory factory) {
		final AxiomAnnotationsChanger changer = new AxiomAnnotationsChanger(
		        annotations, factory);
		final OWLAxiom newAxiom = axiom.accept(changer);
		return newAxiom;
	}

	/**
	 * Removes the namespace from identifier.
	 *
	 * @param rawID
	 *            the raw ID
	 * @return the string
	 */
	public String removeNamespaceFromIdentifier(String rawID) {
		String cleanID = rawID;
		if (rawID.contains(this.defaultNamespace)) {
			final int beginning = rawID.indexOf("#") + 1;
			final int end = rawID.length() - 1;
			if ((beginning > 0) && (end > 0)) {
				cleanID = rawID.substring(beginning, end);
			}
		}
		return cleanID;
	}

	/*
	 * This method will remove a given property from the OWL vocabulary. Axioms
	 * to the property are first removed, then the Object/Data property is
	 * removed. Note, axioms within Object or Data properties are not changed.
	 */
	/**
	 * Removes the property.
	 *
	 * @param property
	 *            the property
	 */
	public void removeProperty(IRI property) {
		try {
			final OWLEntityRemover remover = new OWLEntityRemover(
			        Collections.singleton(this.ontology));
			// Set<OWLOntologyChange> changes = new
			// HashSet<OWLOntologyChange>();

			for (final OWLDataProperty odp : this.ontology
			        .getDataPropertiesInSignature()) {
				if (odp.getIRI().equals(property)) {
					odp.accept(remover);
				}
			}
			this.manager.applyChanges(remover.getChanges());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method will remove a given property from the OWL vocabulary. Axioms
	 * to the property are first removed, then the Object/Data property is
	 * removed. Note, axioms within Object or Data properties are not changed.
	 */
	/**
	 * Removes the property.
	 *
	 * @param property
	 *            the property //
	 */
	// public void removeProperty(String property) {
	// final IRI propertyURI = createIRI(this.getNamespace(),property);
	// removeProperty(propertyURI);
	// }

	/**
	 * Save ontology.
	 *
	 * @param savePath
	 *            the save path
	 */
	public void saveOntology(String savePath) {
		try {
			final File output = File.createTempFile(savePath, "owl");
			final IRI documentIRI = IRI.create(output);
			this.manager.saveOntology(this.ontology, documentIRI);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save ontology.
	 *
	 * @param savePath
	 *            the save path
	 * @param encodingFormat
	 *            the encoding format
	 */
	// public void saveOntology(String savePath, String encodingFormat) {
	// try {
	// // final RDFXMLOntologyStorer storer = new RDFXMLOntologyStorer();
	// // final File newFile = new File(savePath);
	// // final FileOutputStream out = new FileOutputStream(newFile);
	// // final WriterDocumentTarget target = new WriterDocumentTarget(
	// // new BufferedWriter(new OutputStreamWriter(out,
	// // encodingFormat)));
	// // final OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
	// // storer.storeOntology(this.manager, this.ontology, target,
	// // format);
	//
	// manager.saveOntology(this.ontology, IRI.create(savePath));
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Save ontology.
	 *
	 * @param saveURI
	 *            the save URI
	 * @param encodingFormat
	 *            the encoding format
	 */
	public void saveOntology(URI saveURI, String encodingFormat) {
		try {
			final RDFXMLStorer storer = new RDFXMLStorer();
			final File newFile = new File(saveURI);
			final FileOutputStream out = new FileOutputStream(newFile);
			final WriterDocumentTarget target = new WriterDocumentTarget(
			        new BufferedWriter(new OutputStreamWriter(out,
			                encodingFormat)));
			final OWLDocumentFormat format = new OWLXMLDocumentFormat();
			storer.storeOntology(this.ontology, target, format);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the namespace.
	 *
	 * @param namespace
	 *            the new namespace
	 */
	public void setNamespace(String namespace) {
		this.defaultNamespace = namespace;
	}

	/**
	 * Should walk ontology.
	 *
	 * @throws OWLOntologyCreationException
	 *             the OWL ontology creation exception
	 */

	public void shouldWalkOntology() throws OWLOntologyCreationException {
		// This example shows how to use an ontology walker to walk the asserted
		// structure of an ontology. Suppose we want to find the axioms that use
		// a some values from (existential restriction) we can use the walker to
		// do this. We'll use the pizza ontology as an example. Load the
		// ontology from the web:
		// IRI documentIRI = IRI.create(PIZZA_IRI);
		// OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		// OWLOntology ont = man.loadOntologyFromOntologyDocument(documentIRI);
		// Create the walker. Pass in the pizza ontology - we need to put it
		// into a set though, so we just create a singleton set in this case.
		final OWLOntologyWalker walker = new OWLOntologyWalker(
		        Collections.singleton(this.ontology));
		// Now ask our walker to walk over the ontology. We specify a visitor
		// who gets visited by the various objects as the walker encounters
		// them. We need to create out visitor. This can be any ordinary
		// visitor, but we will extend the OWLOntologyWalkerVisitor because it
		// provides a convenience method to get the current axiom being visited
		// as we go. Create an instance and override the
		// visit(OWLObjectSomeValuesFrom) method, because we are interested in
		// some values from restrictions.
		final OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(
		        walker) {
			@Override
			public void visit(OWLObjectSomeValuesFrom desc) {
				// Print out the restriction
				System.out.println(desc);
				// Print out the axiom where the restriction is used
				System.out.println("         " + this.getCurrentAxiom());
				System.out.println();
				// We don't need to return anything here.

			}
		};
		// Now ask the walker to walk over the ontology structure using our
		// visitor instance.
		walker.walkStructure(visitor);
	}


	public void startReasoner() {
		 System.out.println("Starting reasoner.");
		// this.config.reasonerProgressMonitor = new ConsoleProgressMonitor();
//	 this. = factory.createReasoner(this.ontology, this.config);
		this.reasoner = reasonerFactory.createReasoner(this.ontology);
		reasoner.getUnsatisfiableClasses();
//		reasoner.classify();

		System.out.println("Finished computing class hierarchy.");
	}

//	/**
//	 * Start told reasoner.
//	 */
//	public void startToldReasoner() {
//		System.out.println("Starting TOLD reasoner.");
//		// this.config.reasonerProgressMonitor = new ConsoleProgressMonitor();
//		// this.reasoner = factory.createReasoner(this.ontology, this.config);
//		// this.reasoner = new StructuralReasoner(this.ontology, this.config,
//		// BufferingMode.BUFFERING);
//		this.reasoner = this.reasonerFactory.createReasoner(this.ontology);
//		this.reasoner.getUnsatisfiableClasses();
//		System.out.println("Finished computing class hierarchy.");
//	}

	/**
	 * Stop reasoner.
	 */
	public void stopReasoner() {
		this.reasoner.dispose();
	}

	/**
	 * Tr literal.
	 *
	 * @param value
	 *            the value
	 * @return the OWL annotation value
	 */
	protected OWLAnnotationValue trLiteral(Object value) {
		OWLDataFactory fac = this.manager.getOWLDataFactory();
		if (value instanceof Boolean) return fac.getOWLLiteral((Boolean) value);
		else if (!(value instanceof String)) {
			// TODO
			// e.g. boolean
			value = value.toString();
		}
		// System.out.println("v="+value);
		return fac.getOWLLiteral((String) value); // TODO
	}

	/**
	 * Tr tag to annotation prop.
	 *
	 * @param tag
	 *            the tag
	 * @return the OWL annotation property
	 */
	@Deprecated
	private OWLAnnotationProperty trTagToAnnotationProp(String tag) {

		return this.trTagToAnnotationProp(this.getNamespace(), tag);
	}

	/**
	 * Tr tag to annotation prop.
	 *
	 * @param propNamespace
	 *            the prop namespace
	 * @param tag
	 *            the tag
	 * @return the OWL annotation property
	 */
	private OWLAnnotationProperty trTagToAnnotationProp(String propNamespace,
	        String tag) {
		IRI iri = this.createIRI(tag, propNamespace);
		return this.trTagToAnnotationProp(iri);

	}

	private OWLAnnotationProperty trTagToAnnotationProp(IRI propCode) {

		OWLDataFactory fac = this.manager.getOWLDataFactory();
		OWLAnnotationProperty ap = fac.getOWLAnnotationProperty(propCode);

		return ap;
	}

	/**
	 * Remove a single property from a concept.  If multiple candidate properties are found,
	 *     the effort aborts and the return is false.
	 *     If the removal succeeds then it returns true
	 * @param conceptCode
	 * @param propertyCode
	 * @return
	 */
	protected boolean removePropertyFromConcept(IRI conceptCode, String propertyCode) {
		final OWLClass c = this.getOWLClass(conceptCode);
		final OWLAnnotationProperty prop = this.manager.getOWLDataFactory()
		        .getOWLAnnotationProperty(IRI.create(propertyCode));
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		Collection<OWLAnnotationAssertionAxiom> assertions = EntitySearcher.getAnnotationAssertionAxioms(c, this.ontology);
		if (assertions.size()==1){
			for (OWLAnnotationAssertionAxiom annAx : EntitySearcher
					.getAnnotationAssertionAxioms(conceptCode, this.ontology)) {
				changes.add(new RemoveAxiom(this.ontology, annAx));
			}
			this.manager.applyChanges(changes);
			return true;
		}
		return false;
    }
	
	private void loadAssociationGraph() throws OWLOntologyCreationException{
		// Create an RDFTranslator - we override the addTriple  
//method because we want to
		// handle the triples in a streaming manner
		gov.nih.nci.evs.owl.data.AssociationTranslator translator = new gov.nih.nci.evs.owl.data.AssociationTranslator(this.manager, this.ontology, false, null) {
//		    public void addTriple(OWLAxiom ax, RDFResource subject, RDFResourceIRI pred, RDFNode object) {
//		        System.out.println(subject + " -> " + pred + " ->  " + object);
//		    }
//		    
//		    protected void addSingleTriple(OWLAxiom ax, RDFResource subject, RDFResource pred, IRI object){
//		    	System.out.println(subject + " -> " + pred + " ->  " + object);
//		    }
		};

		// Generate triples for axioms in the ontology.
		for(OWLAxiom ax : this.ontology.getAxioms()) {
			Set<OWLAnnotationProperty> props = ax.getAnnotationPropertiesInSignature();
            if(props.size()>0){
            	for(OWLAnnotationProperty prop:props){
            		if(prop.getIRI().getFragment().startsWith("A")){
            			ax.accept(translator);
            		}
            	}
            }		    
		}
		
		associationGraph = translator.getAssocGraph();
		if(associationGraph == null)
		{
			associationGraph = (gov.nih.nci.evs.owl.data.AssociationGraph) new RDFGraph();
		}
	}
	
	public HashMap<URI, String> getAllAssociations() {
		final HashMap<IRI, String> nameTemp = this.getAllAssociationNames();
		HashMap<URI, String> names = new HashMap<URI, String>();
		for (IRI iri : nameTemp.keySet()) {
			names.put(iri.toURI(), nameTemp.get(iri));
		}

		return names;
	}

	public Collection<RDFTriple> getAssociationsForTarget(ConceptProxy target) {
	    
	    try {
	        if(associationGraph == null){
	        	loadAssociationGraph();
	        	return getAssociationsForTarget(target);
	        } else
	        {
	        	return associationGraph.getTriplesForObject(IRI.create(target.getURI()));

	        	
	        }
        } catch (OWLOntologyCreationException e) {
	        
	        e.printStackTrace();
	        return null;
        }
    }

	public int getAxiomCount() {
		return metrics.getAxiomCount();
	}

	public int getGciCount(){
		return metrics.getGciCount();
	}

	public int getHiddenGciCount(){
		return metrics.getHiddenGciCount();
	}
	
	public int getMultiParentCount(){
		return metrics.getMultipleInheritanceCount();
	}

	public String getDLExpressivity() {
		return metrics.getDLexpressivity();
	}

	public int getReferencedIndividualsCount() {return metrics.getReferencedIndividualsCount();}
}
