package gov.nih.nci.evs.owl;

/*
 * Robert Wynne, MSC
 * Tracy Safran, Leidos
 *
 * Center for Bioinformatics and Information Technology (CBIIT)
 * Enterprise Vocabulary Services (EVS)
 *
 * Modified: 8/18/16 for Cancer Moonshot Phase 1
 *                In Support of OCPL Clinical Trials Search API
 *                
 *           2/5/18 to generate CURIEs from oboInOWL:hasDbXref
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWL2Datatype;


/**
 * The Class OWLScrubber.
 */
public class OWLScrubber {

	/**
	 * The main method. Creates OWLScrubber, configures it, then runs it
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		try {
			OWLScrubber scrubber = new OWLScrubber();
			scrubber.configure(args);
			scrubber.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OWLReasoner reasoner;

	/** The ontology namespace. */
	private String ontologyNamespace;

	/** The physical uri where the file to be processed is found. */
	private URI physicalURI;

	/** The save uri where the output file should be written. */
	private URI saveURI;

	/** The type constant string. */
	private final String typeConstantString = "http://www.w3.org/2001/XMLSchema#string";

	/** The type constant literal. */
	private final String typeConstantLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

	/** The manager. */
	private OWLOntologyManager manager;

	/** The ontology. */
	private OWLOntology ontology;

	/** The suppress individuals. Should OWL#Individual be suppressed */
	private boolean suppressIndividuals = true;

	/** The has literals. Does the ontology possess instances of XML:Literal */
	private boolean hasLiterals = false;

	/** The for meme. Is the output intended for import into MEME */
	private final boolean forMeme = false;

	/** The for ftp. Is the output intended for publishing to the FTP */
	private final boolean forFtp = false;

	/**
	 * The pretty print. Should the output skip processing and just be a
	 * prettier version of the input
	 */
	private boolean prettyPrint = false;

	/** The scrub empty. Should empty qualifiers be scrubbed from the output */
	private boolean scrubEmpty = false;

	/**
	 * The prefix. If hasLiterals=true, then a prefix for the literal's
	 * definition must be supplied
	 */
	private String prefix = new String("");

	private PrintWriter pw;

	private String configFile = "./config/owlscrubber.properties";

	Vector<String> complexPropsToSimplify;

	/** The for ftp. Is the output intended for publishing to the FTP */
	private boolean generateFlatFile = false;

	/** The uri where the Flat File should be printied */
	private URI flatFileURI;

	/** The removed classes. */
	Vector<IRI> removedClasses = new Vector<IRI>();

	/** The branches to delete. */
	Vector<String> branchesToDelete;

	/** The properties to delete. */
	Vector<String> propertiesToDelete;

	/** The complex data to delete. */
	Vector<String> complexDataToDelete;

	/** The properties to be substituted **/
	Vector<String> propertySubs;

	/**
	 * Configure the PrintWriter
	 *
	 * @param fileLoc
	 */
	private boolean config_pw(URI fileLoc) {
		try {
			File file = new File(fileLoc);
			this.pw = new PrintWriter(file);
			return true;
		} catch (Exception e) {
			System.out.println("Error in PrintWriter");
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#configure(java.lang.String[])
	 */

	public void configure(String[] args) throws Exception {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String option = args[i];
				if (option.equalsIgnoreCase("--help")) {
					this.printHelp();
				} else if (option.equalsIgnoreCase("-E") || option.equalsIgnoreCase("--Empty")) {
					this.scrubEmpty = true;
				} else if (option.equalsIgnoreCase("-I") || option.equalsIgnoreCase("--Individuals")) {
					this.suppressIndividuals = false;
				} else if (option.equalsIgnoreCase("-L") || option.equalsIgnoreCase("--Literals")) {
					this.hasLiterals = true;
					this.prefix = args[++i] + ":";
				} else if (option.equalsIgnoreCase("-C") || option.equalsIgnoreCase("--Config")) {
					this.configFile = args[++i];
				} else if (option.equalsIgnoreCase("-F") || option.equalsIgnoreCase("-Flat")) {
					this.generateFlatFile = true;
					this.flatFileURI = new URI(args[++i]);
				} else if (option.equalsIgnoreCase("-P") || option.equalsIgnoreCase("--Pretty")) {
					this.prettyPrint = true;
				} else if (option.equalsIgnoreCase("-N") || option.equalsIgnoreCase("--iNput")) {
					this.physicalURI = new URI(args[++i]);
				} else if (option.equalsIgnoreCase("-O") || option.equalsIgnoreCase("--Output")) {
					this.saveURI = new URI(args[++i]);
				} else {
					this.printHelp();
				}

			}
		} else {
			this.printHelp(); // This will exit the program
		}

		String branchDeleteFile = "";
		String propsDeleteFile = "";
		String complexDeleteFile = "";
		String complexSimplifyFile = "";
		String propertySubFile = "";
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(this.configFile));
			this.ontologyNamespace = props.getProperty("namespace");
			if (this.saveURI == null) {
				this.saveURI = new URI(props.getProperty("saveURI"));
			}
			if (this.physicalURI == null) {
				this.physicalURI = new URI(props.getProperty("inputURI"));
			}
			branchDeleteFile = props.getProperty("branch_delete");
			propsDeleteFile = props.getProperty("props_delete");
			complexDeleteFile = props.getProperty("complex_delete");
			complexSimplifyFile = props.getProperty("complex_simplify");
			propertySubFile = props.getProperty("property_substitution");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to find owlscrubber.properties file in this directory.  Aborting.");
			System.exit(1);
		}
		try {
			this.manager = OWLManager.createOWLOntologyManager();
			this.ontology = this.manager.loadOntologyFromOntologyDocument(IRI.create(this.physicalURI));
		} catch (OWLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.branchesToDelete = this.readConfigFile(branchDeleteFile);
		this.propertiesToDelete = this.readConfigFile(propsDeleteFile);
		this.complexDataToDelete = this.readConfigFile(complexDeleteFile);
		this.complexPropsToSimplify = this.readConfigFile(complexSimplifyFile);
		this.propertySubs = this.readConfigFile(propertySubFile);
		startToldReasoner();
	}

	private IRI createIRI(String className) {
		return IRI.create(this.createURI(className));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#createURI(java.lang.String)
	 */

	private URI createURI(String className) {
		return URI.create(className);
	}

	/**
	 * Gets the OWL class.
	 *
	 * @param conceptIRI
	 *            the concept IRI
	 * @return the OWL class
	 */
	private OWLClass getOWLClass(final IRI conceptIRI) {
		final OWLClass cls = this.manager.getOWLDataFactory().getOWLClass(conceptIRI);
		return cls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#fixReferences()
	 */
	private void fixReferences() {
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		if (this.removedClasses != null) {
			for (final IRI conceptCode : this.removedClasses) {
				final OWLClass concept = this.getOWLClass(conceptCode);
				Collection<OWLAnnotationAssertionAxiom> esAxioms = EntitySearcher.getAnnotationAssertionAxioms(concept,
						this.ontology);
				for (final OWLAnnotationAxiom axiom : esAxioms) {
					changes.add(new RemoveAxiom(this.ontology, axiom));
				}
			}
		}
		List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(changes);
		this.manager.applyChanges(list);
	}

	public void startToldReasoner() {
		System.out.println("Starting TOLD reasoner.");
		// this.config.reasonerProgressMonitor = new ConsoleProgressMonitor();
		// this.reasoner = factory.createReasoner(this.ontology, this.config);
		// this.reasoner = new StructuralReasoner(this.ontology, this.config,
		// BufferingMode.BUFFERING);
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		reasoner = reasonerFactory.createReasoner(this.ontology);
		reasoner.getUnsatisfiableClasses();
		System.out.println("Finished computing class hierarchy.");
	}

	private Vector<OWLClass> getSuperClasses(OWLClass cls, boolean directOnly) {
		if (cls.isOWLNothing()) {
			return new Vector<OWLClass>();
		}

		final Vector<OWLClass> vParents = new Vector<OWLClass>();
		if (this.reasoner != null) {
			for (final OWLClass subCls : this.reasoner.getSuperClasses(cls, directOnly).getFlattened()) {
				if (!vParents.contains(subCls) && !subCls.isOWLThing()) {
					vParents.add(subCls);
				}
			}
		}
		if (vParents.size() < 1) {
			final Collection<OWLClassExpression> ods = EntitySearcher.getSuperClasses(cls, this.ontology);
			final OWLClassExpression[] parents = ods.toArray(new OWLClassExpression[ods.size()]);
			if (parents.length == 0)
				return vParents;

			for (final OWLClassExpression parent : parents) {
				if (!parent.isAnonymous()) {
					vParents.add(parent.asOWLClass());
				}
			}
			if (!directOnly) {
				for (int i = 0; i < vParents.size(); i++) {
					final Vector<OWLClass> w = this.getSuperClasses(vParents.elementAt(i).asOWLClass(), false);
					if (w != null) {
						for (int j = 0; j < w.size(); j++) {
							if ((w.elementAt(j) != null) && !vParents.contains(w.elementAt(j))) {
								vParents.add(w.elementAt(j).asOWLClass());
							}
						}
					}
				}
			}
		}
		return vParents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#generateFlat()
	 */
	@SuppressWarnings("unused")
	private void generateFlat() {
		TreeMap<String, Vector<String>> idAndDatas = new TreeMap<String, Vector<String>>();
		TreeMap<String, Vector<String>> idAndDatasRetired = new TreeMap<String, Vector<String>>();
		// OWLClassReasoner reasoner = new ToldClassHierarchyReasoner(manager);
		try {
			// reasoner.loadOntologies(Collections.singleton(ontology));
			// reasoner.classify();
			for (OWLClass c : this.ontology.getClassesInSignature()) {
				// Set<Set<OWLClass>> parents = reasoner.getSuperClasses(c);
				if (c.getIRI().getFragment().equals("C3163")) {
					String debug = "Stop here";
				}

				Vector<OWLClass> parents = getSuperClasses(c, true);

				// Collection<OWLClassExpression> parents = EntitySearcher
				// .getSuperClasses(c, this.ontology);
				// Collection<OWLClassExpression> parents2 = EntitySearcher
				// .getEquivalentClasses(c, this.ontology);
				// for(OWLClassExpression oce: parents2){
				// Set<OWLClassExpression> classes =
				// oce.getNestedClassExpressions();
				// for(OWLClassExpression cls : classes){
				// if (cls instanceof OWLClassImpl){
				// parents.add(cls);
				// }
				// }
				// }

				// Set<OWLDescription> parents = c.getSuperClasses(ontology);
				// Set<OWLDescription> parents2 =
				// c.getEquivalentClasses(ontology);
				// parents.addAll(parents2);

				String code = this.getSolePropertyValue(c, PROPERTY_ALIAS.CODE.iri());
				String pn = this.getSolePropertyValue(c, PROPERTY_ALIAS.PREFERRED_NAME.iri());
				Vector<String> definitions = this.getPropertyValues(c, PROPERTY_ALIAS.DEFINITION.iri());
				Vector<String> terms = this.getPropertyValues(c, PROPERTY_ALIAS.FULL_SYN.iri());
				Vector<String> dns = this.getPropertyValues(c, PROPERTY_ALIAS.DISPLAY_NAME.iri());
				Vector<String> stys = this.getPropertyValues(c, PROPERTY_ALIAS.SEMANTIC_TYPE.iri());
				Vector<String> statuses = this.getPropertyValues(c, PROPERTY_ALIAS.CONCEPT_STATUS.iri());

				if (pn.equals("")) {
					pn = c.asOWLClass().getIRI().getFragment();
				}

				// 0- code
				// 1- parents
				// 2- terms
				// 3- definition
				// 4- display_name
				// 5- concept_status
				// 6- semantic_type
				Vector<String> datas = new Vector<String>();
				Vector<String> parentV = new Vector<String>();
				datas.add(code);

				String parentsString = new String("");
				String termsString = new String(pn);
				String defString = new String("");
				String dnString = new String("");
				String styString = new String("");
				String statusString = new String("");

				// for (Set<OWLClass> pSet : parents) {
				// for (OWLClass p : pSet) {
				for (OWLClassExpression p : parents) {
					if (!p.isAnonymous()) {
						String parCode = this.getSolePropertyValue(p.asOWLClass(), PROPERTY_ALIAS.CODE.iri());
						String par = p.asOWLClass().getIRI().getFragment();
						if (par.equals("Thing")) {
							par = "root_node";
						}
						if (parCode != null && parCode.length() > 0) {
							parentV.add(parCode);
						} else {
							parentV.add(par);
						}
					}
				}
				// }
				Collections.sort(parentV);
				for (int i = 0; i < parentV.size(); i++) {
					parentsString = parentV.elementAt(i) + "|" + parentsString;
				}
				if (parentsString.contains("|")) {
					parentsString = parentsString.substring(0, parentsString.length() - 1);
				}

				datas.add(parentsString);

				while (terms.contains(pn)) {
					terms.remove(pn);
				}
				Collections.sort(terms);
				if (terms.size() > 0) {
					for (int i = 0; i < terms.size(); i++) {
						if (i == 0) {
							termsString = termsString + "|";
						}
						termsString = termsString + terms.elementAt(i);
						if (i != terms.size() - 1) {
							termsString = termsString + "|";
						}
					}
					datas.add(termsString);
				} else {
					datas.add(termsString);
				}

				if (definitions.size() > 0) {
					datas.add(definitions.elementAt(0));
				} else {
					datas.add(defString);
				}

				if (dns.size() >= 1) {
					for (int i = 0; i < dns.size(); i++) {
						if (i != dns.size() - 1) {
							dnString = dnString + dns.elementAt(i) + "|";
						} else {
							dnString = dnString + dns.elementAt(i);
						}
					}
					datas.add(dnString);
				} else {
					datas.add(dnString);
				}

				if (statuses.size() >= 1) {
					for (int i = 0; i < statuses.size(); i++) {
						if (i != statuses.size() - 1) {
							statusString = statusString + statuses.elementAt(i) + "|";
						} else {
							statusString = statusString + statuses.elementAt(i);
						}
					}
					datas.add(statusString);
				} else {
					datas.add(statusString);
				}

				if (stys.size() >= 1) {
					for (int i = 0; i < stys.size(); i++) {
						if (i != stys.size() - 1) {
							styString = styString + stys.elementAt(i) + "|";
						} else {
							styString = styString + stys.elementAt(i);
						}
					}
					datas.add(styString);
				} else {
					datas.add(styString);
				}

				// pw.println(datas);
				if (this.isRetired(c)) {
					idAndDatasRetired.put(c.toString(), datas);
				} else {
					idAndDatas.put(c.toString(), datas);
				}
			}
			for (String key : idAndDatas.keySet()) {
				Vector<String> data = idAndDatas.get(key);
				this.pw.println(data.elementAt(0) + "\t" + key + "\t" + data.elementAt(1) + "\t" + data.elementAt(2)
						+ "\t" + data.elementAt(3) + "\t" + data.elementAt(4) + "\t" + data.elementAt(5) + "\t"
						+ data.elementAt(6));
				this.pw.flush();
			}
			for (String key : idAndDatasRetired.keySet()) {
				Vector<String> data = idAndDatasRetired.get(key);
				this.pw.println(data.elementAt(0) + "\t" + key + "\t" + data.elementAt(1) + "\t" + data.elementAt(2)
						+ "\t" + data.elementAt(3) + "\t" + data.elementAt(4) + "\t" + data.elementAt(5) + "\t"
						+ data.elementAt(6));
				this.pw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#getDescendants(org.semanticweb.
	 * owlapi .model.OWLClass)
	 */

	private Collection<OWLClassExpression> getDescendants(OWLClass cls) {
		Collection<OWLClassExpression> oce = EntitySearcher.getSubClasses(cls, this.ontology);
		if (oce.isEmpty())
			return null;

		Collection<OWLClassExpression> oce2 = EntitySearcher.getSubClasses(cls, this.ontology);
		for (OWLClassExpression cex : oce) {
			Collection<OWLClassExpression> w = this.getDescendants(cex.asOWLClass());
			if (w != null) {
				for (OWLClassExpression cex2 : w) {
					if ((cex2 != null) && (!oce.contains(cex2))) {
						oce2.add(cex2);
					}
				}
			}
		}
		return oce2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nih.nci.owl.OwlScrubberInterface#getPropertyValues(org.semanticweb
	 * .owlapi.model.OWLClass, java.lang.String)
	 */

	private Vector<String> getPropertyValues(OWLClass c, IRI property) {
		Vector<String> v = new Vector<String>();
		EntitySearcher.getAnnotationAssertionAxioms(c, this.ontology);
		for (OWLAnnotationAssertionAxiom anno : EntitySearcher.getAnnotationAssertionAxioms(c, this.ontology)) {

			if (anno.getProperty().getIRI().equals(property)) {
				OWLLiteral annotationLiteral = anno.getValue().asLiteral().orNull();
				if (annotationLiteral != null) {
					v.add(annotationLiteral.getLiteral().toString());
				}
			}
			// }
		}
		Collections.sort(v);
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nih.nci.owl.OwlScrubberInterface#getQualifiers(org.semanticweb.owlapi
	 * .model.OWLClass, java.lang.String, java.lang.String)
	 */
	//
	// private Vector<String> getQualifiers(OWLClass c, IRI property, IRI
	// qualifier) {
	//
	// Vector<String> v = new Vector<String>();
	// for (OWLAnnotationAssertionAxiom annAx : EntitySearcher
	// .getAnnotationAssertionAxioms(c, this.ontology)) {
	// Set<OWLAnnotation> rawQuals = annAx.getAnnotations();
	// if (annAx.getProperty().getIRI().equals(property)) {
	//
	// for (OWLAnnotation anno : annAx.getAnnotations()) {
	// if (anno.getProperty().getIRI().equals(qualifier)) {
	// v.add(anno.getValue().toString());
	// }
	// }
	// }
	// }
	//
	// return v;
	// }
	//
	// /*
	// * (non-Javadoc)
	// * @see
	// gov.nih.nci.owl.OwlScrubberInterface#getReferencingClassEntity(org.
	// * semanticweb.owlapi.model.OWLAxiom)
	// */
	//
	// private OWLEntity getReferencingClassEntity(OWLAxiom ax) {
	// OWLEntity ent = null;
	// // assume only one class referenced for each axiom
	// ax.getClassesInSignature();
	// // TODO fix this
	// boolean debug = true;
	// // for (OWLEntity e : ax.getReferencedEntities()) {
	// // if (hasLiterals && !e.toString().equals("XMLLiteral")) {
	// // ent = e;
	// // break;
	// // }
	// // if (!hasLiterals && !e.toString().equals("string")) {
	// // ent = e;
	// // break;
	// // }
	// // }
	// return ent;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nih.nci.owl.OwlScrubberInterface#getSolePropertyValue(org.semanticweb
	 * .owlapi.model.OWLClass, java.lang.String)
	 */

	private String getSolePropertyValue(OWLClass c, IRI property) {
		String annotationValue = new String("");
		for (OWLAnnotationAssertionAxiom anno : EntitySearcher.getAnnotationAssertionAxioms(c, this.ontology)) {
			OWLLiteral annotationLiteral = anno.getValue().asLiteral().orNull();
			if (annotationLiteral != null) {
				annotationValue = annotationLiteral.getLiteral().toString();
			}
			if (anno.getProperty().getIRI().equals(property))
				return annotationValue;
		}
		return annotationValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nih.nci.owl.OwlScrubberInterface#isEmpty(org.semanticweb.owlapi.model
	 * .OWLAxiom)
	 */

	private boolean isEmpty(OWLAxiom ax) {
		boolean test = false;
		// EntityAnnotationAxiom(OBI_0000639 Annotation(IAO_0000111 ""^^string))
		// TODO this is false flagging
		if (ax.toString().contains(" \"\"^^") || ax.toString().contains(" \"\"@")) {
			// System.out.println(ax);
			// test = true;
		}
		return test;
	}

	private boolean isRetired(OWLClass c) {

		for (OWLAnnotationAssertionAxiom anno : EntitySearcher.getAnnotationAssertionAxioms(c, this.ontology)) {
			if (anno.getProperty().getIRI().equals(PROPERTY_ALIAS.CONCEPT_STATUS)
					&& anno.getValue().toString().contains("Retired_Concept")) {
				// String annotationValue = anno.toString();
				// if (annotationValue
				// .contains("Annotation(Concept_Status \"Retired_Concept")) {
				return true;

			} else if (anno.isDeprecatedIRIAssertion()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#printHelp()
	 */

	public void printHelp() {
		System.out.println("");
		// System.out.println("Usage: OWLScrubber [OPTIONS] ... [OWL] [OUTPUT
		// FILE]");
		System.out.println("Usage: OWLScrubber [OPTIONS] ");
		System.out.println(" ");
		System.out.println("  -C [configFile]\tTells where to find owlscrubber.properties file");
		System.out.println("  -E, --Empty\t\tScrub empty properties");
		System.out.println("  -I, --Individuals\t\tOutput OWL Individuals");
		System.out.println("  -L, --Literals [prefix]\tInput OWL contains XML Literals");
		// System.out
		// .println(" -M, --Meme\t\t\tOutput MEME file for publication");
		System.out.println("   -F, --Flat\t\t\tURL to print flat file (optional)");
		System.out.println("  -P, --Pretty\t\t\tPretty print, scrub nothing");
		// System.out.println(" -S, --Synonyms\t\tConstruct synonyms");
		System.out.println("  -N, --iNput\t\t\tURL of input file");
		System.out.println("  -O, --Output\t\t\tURL of output file");
		System.out.println("");
		System.exit(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nih.nci.owl.OwlScrubberInterface#readConfigFile(java.lang.String)
	 */

	public Vector<String> readConfigFile(String filename) {
		Vector<String> v = new Vector<String>();
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {
					v.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!v.isEmpty())
			return v;
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#removeBranch(java.net.URI)
	 */

	private void removeBranch(URI classURI) {
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(this.ontology));

		OWLClass cls = this.manager.getOWLDataFactory().getOWLClass(IRI.create(classURI));

		Collection<OWLClassExpression> descendants = this.getDescendants(cls);
		if (descendants != null) {
			for (OWLClassExpression odesc : descendants) {
				// System.out.println("Removing " + odesc );
				IRI test = odesc.asOWLClass().getIRI();
				this.removedClasses.add(test);
				odesc.asOWLClass().accept(remover);
			}
		} else {
			// System.out.println("No children to remove from " +
			// cls + ".");
		}

		cls.accept(remover);

		this.manager.applyChanges(remover.getChanges());
	}

	private void removeQualifier() {
		for (OWLClass cls : this.ontology.getClassesInSignature()) {
			this.removeQualifier2(cls);
		}
		for (OWLDataProperty dp : this.ontology.getDataPropertiesInSignature()) {
			this.removeQualifier2(dp);
		}
		for (OWLObjectProperty op : this.ontology.getObjectPropertiesInSignature()) {
			this.removeQualifier2(op);
		}
		for (OWLAnnotationProperty ap : this.ontology.getAnnotationPropertiesInSignature()) {
			this.removeQualifier2(ap);
		}
		for (OWLNamedIndividual ind : this.ontology.getIndividualsInSignature()) {
			this.removeQualifier2(ind);
		}
		for (OWLDatatype dt : this.ontology.getDatatypesInSignature()) {
			this.removeQualifier2(dt);
		}

	}

	private void removeQualifier2(OWLEntity cls) {

		// List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		OWLDataFactory factory = this.manager.getOWLDataFactory();

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls.getIRI(),
				this.ontology)) {
			boolean match = false;
			Set<OWLAnnotation> existingAnnotations = annAx.getAnnotations();
			for (String complex : this.complexDataToDelete) {
				String[] complexArray = complex.split("\t");
				if (annAx.getProperty().getIRI().toString().equals(complexArray[0])) {

					for (OWLAnnotation anno : annAx.getAnnotations()) {
						if (anno.getProperty().getIRI().toString().equals(complexArray[1])) {
							existingAnnotations.remove(anno);
							match = true;
						}
					}
				}

			}
			if (match) {
				OWLAnnotation newAnnotation = factory.getOWLAnnotation(
						factory.getOWLAnnotationProperty(annAx.getProperty().getIRI()), annAx.getValue());
				Set<OWLAnnotation> newAnnotations = new HashSet<OWLAnnotation>();
				for (OWLAnnotation newAnno : existingAnnotations) {
					newAnnotations.add(newAnno);
				}
				OWLAxiom annotatedAxiom = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), newAnnotation,
						newAnnotations);
				changes.add(new RemoveAxiom(this.ontology, annAx));
				changes.add(new AddAxiom(this.ontology, annotatedAxiom));

			}
		}
		if (changes.size() > 0) {
			this.manager.applyChanges(changes);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#removeEmpty()
	 */

	private void removeEmpty() {
		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		for (OWLAxiom ax : this.ontology.getAxioms()) {
			if (this.isEmpty(ax)) {
				changes.add(new RemoveAxiom(this.ontology, ax));
			}
		}
		List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(changes);
		this.manager.applyChanges(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#removeIndividuals()
	 */

	private void removeNamedIndividuals() {
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(this.ontology));
		for (OWLNamedIndividual oi : this.ontology.getIndividualsInSignature()) {
			oi.accept(remover);

		}
		this.manager.applyChanges(remover.getChanges());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nih.nci.owl.OwlScrubberInterface#removeProperty(java.lang.String)
	 */

	private void removeProperty(String property) {
		this.removeAnnotationProperty(this.createIRI(property));

	}

	private void removeAnnotationProperty(IRI property) {
		try {
			final OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(this.ontology));

			OWLDataProperty prop = this.manager.getOWLDataFactory().getOWLDataProperty(property);
			prop.accept(remover);

			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory().getOWLAnnotationProperty(property);
			aProp.accept(remover);

			for(OWLDeclarationAxiom ax : this.ontology.getAxioms(AxiomType.DECLARATION)) {
				if( ax.getEntity().getIRI().equals(property) ) {
					ax.getEntity().accept(remover);
				}
			}
			
			// for(final OWLAnnotationProperty oap:
			// this.ontology.getAnnotationPropertiesInSignature()){
			//
			// if(oap.getIRI().equals(property)){
			// oap.accept(remover);
			// }
			// }
			//
			// for (final OWLDataProperty odp : this.ontology
			// .getDataPropertiesInSignature()) {
			// if (odp.getIRI().equals(property)) {
			// odp.accept(remover);
			// }
			// }
			this.manager.applyChanges(remover.getChanges());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void removePropertyWithAnnotation(IRI property, IRI annotation) {
		// Find all properties with the given annotation
		Set<OWLClass> classes = this.ontology.getClassesInSignature();

		for (OWLClass cls : classes) {
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls.getIRI(),
					this.ontology)) {

				if (annAx.getProperty().getIRI().equals(property)) {

					for (OWLAnnotation anno : annAx.getAnnotations()) {
						if (anno.getProperty().getIRI().equals(annotation)) {
							changes.add(new RemoveAxiom(this.ontology, annAx));
						}
					}
				}

			}
			if (changes.size() > 0) {
				this.manager.applyChanges(changes);
			}

		}
	}

	private void removePropertyWithAnnotationQualifier(IRI property, IRI annotation, String value) {
		// Find all properties with the given annotation and the given value
		Set<OWLClass> classes = this.ontology.getClassesInSignature();

		for (OWLClass cls : classes) {
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls.getIRI(),
					this.ontology)) {

				if (annAx.getProperty().getIRI().equals(property)) {

					Set<OWLAnnotation> debug = annAx.getAnnotations(); //needed?
					for (OWLAnnotation anno : annAx.getAnnotations()) {
						if (anno.getProperty().getIRI().equals(annotation)) {

							if (((OWLLiteral) anno.getValue()).getLiteral().toString().equals(value)) {

								changes.add(new RemoveAxiom(this.ontology, annAx));
								break;
							}
						}
					}
				}

			}
			if (changes.size() > 0) {
				this.manager.applyChanges(changes);
			}

		}
	}

	private void substituteProperty(String[] values) {
		IRI propIRI = IRI.create(values[0]);
		String oldValue = values[1];
		String newValue = values[2];
		for (OWLClass cls : this.ontology.getClassesInSignature()) {
			this.substitutePropertyValue(cls, propIRI, oldValue, newValue);
		}
		for (OWLDataProperty dp : this.ontology.getDataPropertiesInSignature()) {
			this.substitutePropertyValue(dp, propIRI, oldValue, newValue);
		}
		for (OWLObjectProperty op : this.ontology.getObjectPropertiesInSignature()) {
			this.substitutePropertyValue(op, propIRI, oldValue, newValue);
		}
		for (OWLAnnotationProperty ap : this.ontology.getAnnotationPropertiesInSignature()) {
			this.substitutePropertyValue(ap, propIRI, oldValue, newValue);
		}
		for (OWLNamedIndividual ind : this.ontology.getIndividualsInSignature()) {
			this.substitutePropertyValue(ind, propIRI, oldValue, newValue);
		}
		for (OWLDatatype dt : this.ontology.getDatatypesInSignature()) {
			this.substitutePropertyValue(dt, propIRI, oldValue, newValue);
		}

	}

	private void substitutePropertyValue(OWLEntity cls, IRI property, String oldValue, String newValue) {
		OWLDataFactory factory = this.manager.getOWLDataFactory();

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls.getIRI(),
				this.ontology)) {
			boolean match = false;
			Set<OWLAnnotation> existingAnnotations = annAx.getAnnotations();
			// for (String complex : this.complexDataToDelete) {
			// String[] complexArray = complex.split("\t");
			if (annAx.getProperty().getIRI().equals(property)) {
				if (annAx.getValue().toString().equals(oldValue)) {
					match = true;
				}

			}

			// }
			if (match) {
				OWLAnnotation newAnnotation = factory.getOWLAnnotation(
						factory.getOWLAnnotationProperty(annAx.getProperty().getIRI()),
						factory.getOWLLiteral(newValue));
				Set<OWLAnnotation> newAnnotations = new HashSet<OWLAnnotation>();
				// Add the remaining annoations back onto the property
				for (OWLAnnotation newAnno : existingAnnotations) {
					newAnnotations.add(newAnno);
				}
				OWLAxiom annotatedAxiom = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), newAnnotation,
						newAnnotations);
				changes.add(new RemoveAxiom(this.ontology, annAx));
				changes.add(new AddAxiom(this.ontology, annotatedAxiom));

			}
		}
		if (changes.size() > 0) {
			this.manager.applyChanges(changes);
		}

	}

	private void substituteQualifier(String[] values) {
		IRI propIRI = IRI.create(values[0]);
		IRI qualIRI = IRI.create(values[1]);
		String oldValue = values[2];
		String newValue = values[3];
		for (OWLClass cls : this.ontology.getClassesInSignature()) {
			this.substituteQualifierValue(cls, propIRI, qualIRI, oldValue, newValue);
		}
		for (OWLDataProperty dp : this.ontology.getDataPropertiesInSignature()) {
			this.substituteQualifierValue(dp, propIRI, qualIRI, oldValue, newValue);
		}
		for (OWLObjectProperty op : this.ontology.getObjectPropertiesInSignature()) {
			this.substituteQualifierValue(op, propIRI, qualIRI, oldValue, newValue);
		}
		for (OWLAnnotationProperty ap : this.ontology.getAnnotationPropertiesInSignature()) {
			this.substituteQualifierValue(ap, propIRI, qualIRI, oldValue, newValue);
		}
		for (OWLNamedIndividual ind : this.ontology.getIndividualsInSignature()) {
			this.substituteQualifierValue(ind, propIRI, qualIRI, oldValue, newValue);
		}
		for (OWLDatatype dt : this.ontology.getDatatypesInSignature()) {
			this.substituteQualifierValue(dt, propIRI, qualIRI, oldValue, newValue);
		}

	}

	private void substituteQualifierValue(OWLEntity cls, IRI property, IRI qualifier, String oldValue,
			String newValue) {
		OWLDataFactory factory = this.manager.getOWLDataFactory();

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls.getIRI(),
				this.ontology)) {
			boolean match = false;
			Set<OWLAnnotation> existingAnnotations = annAx.getAnnotations();

			if (annAx.getProperty().getIRI().equals(property)) {
				for (OWLAnnotation anno : annAx.getAnnotations()) {
					if (anno.getProperty().getIRI().equals(qualifier)) {
						if (anno.getValue().asLiteral().orNull() != null) {
							if (((OWLLiteral) anno.getValue()).getLiteral().toString().equals(oldValue)) {
								existingAnnotations.remove(anno);
								OWLAnnotation newAnno = factory.getOWLAnnotation(anno.getProperty(),
										factory.getOWLLiteral(newValue));
								existingAnnotations.add(newAnno);
								match = true;
							}
						}
					}
				}
			}
			if (match) {
				OWLAnnotation replacedAnnotation = factory.getOWLAnnotation(annAx.getProperty(), annAx.getValue());
				OWLAxiom annotatedAxiom = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), replacedAnnotation,
						existingAnnotations);
				changes.add(new RemoveAxiom(this.ontology, annAx));
				changes.add(new AddAxiom(this.ontology, annotatedAxiom));
			}
		}
		if (changes.size() > 0) {
			this.manager.applyChanges(changes);
		}
	}
	
	//Method specific to NCI Thesaurus
	//Convert annotations with a code value that also have an owl:Axiom
	//containing an xref-source annotation.  Make them a plain literal annotation of the form
	//<prefix:hasDbXref>source:code</prefix:hasDbXref>
	private void convertXrefs() {
		IRI propIRI = IRI.create("http://www.geneontology.org/formats/oboInOwl#hasDbXref");
		for(OWLClass cls : this.ontology.getClassesInSignature()) {
			convertXrefHelper(cls, propIRI);
		}
		for(OWLDataProperty op : this.ontology.getDataPropertiesInSignature()) {
			convertXrefHelper(op, propIRI);
		}
		for(OWLAnnotationProperty ap : this.ontology.getAnnotationPropertiesInSignature()) {
			convertXrefHelper(ap, propIRI);
		}
		for(OWLNamedIndividual ind : this.ontology.getIndividualsInSignature()) {
			convertXrefHelper(ind, propIRI);
		}
		for(OWLDatatype dt : this.ontology.getDatatypesInSignature()) {
			convertXrefHelper(dt, propIRI);
		}
	}
	
	private void convertXrefHelper(OWLEntity cls, IRI property) {
		IRI qualifier = IRI.create(ontologyNamespace + "#xref-source");
		OWLDataFactory factory = this.manager.getOWLDataFactory();

		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		Vector<OWLAnnotationAssertionAxiom> removeAxioms = new Vector<OWLAnnotationAssertionAxiom>();
		for (OWLAnnotationAssertionAxiom annAx : EntitySearcher.getAnnotationAssertionAxioms(cls.getIRI(), this.ontology)) {
			boolean match = false;
			Vector<String> sources = new Vector<String>();
			OWLAnnotationValue value = null;
			if (annAx.getProperty().getIRI().equals(property)) {
				removeAxioms.add(annAx);
				value = annAx.getValue();
				for (OWLAnnotation anno : annAx.getAnnotations()) {
					if (anno.getProperty().getIRI().equals(qualifier)) {
						OWLLiteral annotationLiteral = anno.getValue().asLiteral().orNull();
						//if(anno.getValue().toString().equals(oldValue)){
						if( annotationLiteral != null ) {
							String source = annotationLiteral.getLiteral().toString();
							sources.add(source);
							match = true;
						}
					}
				}
			}
			if (match) {
				for( String source : sources ) {
					OWLAnnotation newAnnotation = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(annAx.getProperty().getIRI()), 
							factory.getOWLLiteral(source + ":" + value.toString().replaceAll("\"", ""), OWL2Datatype.RDF_PLAIN_LITERAL));
					OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), newAnnotation);
					changes.add(new AddAxiom(this.ontology, axiom));					
				}
				for( OWLAnnotationAssertionAxiom ax : removeAxioms ) {
					changes.add(new RemoveAxiom(this.ontology, ax));		
				}
			}
		}
		if (changes.size() > 0) {
			this.manager.applyChanges(changes);
		}	
		
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.nci.owl.OwlScrubberInterface#run()
	 */

	public void run() {
		try {
			if (!this.prettyPrint) {

				System.out.println("Removing branches...");
				for (String branch : this.branchesToDelete) {
					URI branchToDelete = this.createURI(branch);
					this.removeBranch(branchToDelete);
				}

				System.out.println("Scrubbing complex data...");
				this.removeQualifier();

				System.out.println("Removing properties...");
				for (String property : this.propertiesToDelete) {
					String[] values = property.split("\t");
					if (values.length == 1) {
						this.removeProperty(property);
					} else if (values.length == 2) {
						this.removePropertyWithAnnotation(IRI.create(values[0]), IRI.create(values[1]));
					} else if (values.length == 3) {
						this.removePropertyWithAnnotationQualifier(IRI.create(values[0]), IRI.create(values[1]),
								values[2]);

					} else {
						System.out.println(
								"Invalid input format exists for property (" + property + ") in file prop_del.txt.");
					}
				}

				System.out.println("Substituting properties");
				for (String propertySub : this.propertySubs) {
					String[] values = propertySub.split("\t");
					if (values.length == 3) {
						substituteProperty(values);
					} else if (values.length == 4) {
						substituteQualifier(values);
					} else {
						System.out.println("Invalid input format exists for property (" + propertySub
								+ ") in file prop_value_replace.txt.");
					}
				}

				this.fixReferences();
			}

			if (this.suppressIndividuals) {
				System.out.println("Suppressing Individuals...");
				this.removeNamedIndividuals();
			}
			
			System.out.println("Creating CURIEs");
			this.convertXrefs();			

			if (this.scrubEmpty) {
				System.out.println("Removing empty properties...");
				this.removeEmpty();
			}

			if (this.generateFlatFile) {
				System.out.println("Generating flat file...");
				// config_pw("./FlatFile.txt");
				if (this.config_pw(this.flatFileURI)) {
					this.generateFlat();
				}

			}
			System.out.println("Saving...");
			this.saveOntology();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// /**
	// * Save ontology to the file specified in the properties By default
	// encodes
	// * to utf-8
	// */
	// private void saveOntology() {
	// try {
	// RDFXMLOntologyStorer storer = new RDFXMLOntologyStorer();
	// File newFile = new File(saveURI);
	// FileOutputStream out = new FileOutputStream(newFile);
	// WriterOutputTarget target = new WriterOutputTarget(
	// new BufferedWriter(new OutputStreamWriter(out, "UTF8")));
	// OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
	// if (hasLiterals && prefix.contains("ncicp")) {
	// String prefixToAdd = prefix.replace(":", "");
	// format
	// .addPrefixNamespaceMapping(prefixToAdd,
	// "http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#");
	// }
	// storer.storeOntology(manager, ontology, target, format);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Save ontology.
	 *
	 *
	 *            the save path
	 */
	private void saveOntology() {

		try {

			// final File output = File.createTempFile(saveURI.toString(),
			// "owl");
			// final IRI documentIRI = IRI.create(output);
			final IRI documentIRI = IRI.create(this.saveURI);
			System.out.println("....to " + this.saveURI.toString());
			this.manager.saveOntology(this.ontology, documentIRI);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
