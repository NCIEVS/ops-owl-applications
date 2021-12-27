/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl.entity May 4, 2009
 */
package gov.nih.nci.evs.owl.data;

// import gov.nih.nci.evs.owl.OwlApiInterface;

import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Concept;
import gov.nih.nci.evs.owl.entity.Property;
//import gov.nih.nci.evs.owl.entity.Concept;
//import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.entity.Relationship;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.entity.Role.RoleModifier;
import gov.nih.nci.evs.owl.exceptions.PropertyException;
import gov.nih.nci.evs.owl.exceptions.RoleException;
import gov.nih.nci.evs.owl.meta.PropertyDef;
import gov.nih.nci.evs.owl.meta.RelationDef;
import gov.nih.nci.evs.owl.meta.RoleDef;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.owl.proxy.PropertyProxy;
import gov.nih.nci.evs.owl.proxy.QualifierProxy;
import gov.nih.nci.evs.owl.proxy.RoleProxy;
import gov.nih.nci.evs.owl.visitor.AssociationVisitor;
import gov.nih.nci.evs.owl.visitor.RoleDescriptionVisitor;
import gov.nih.nlm.nls.lvg.Flows.ToMapSymbolToAscii;
import gov.nih.nlm.nls.lvg.Flows.ToMapUnicodeToAscii;
import gov.nih.nlm.nls.lvg.Flows.ToStripMapUnicode;
import gov.nih.nlm.nls.lvg.Lib.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.NodeSet;
//import org.semanticweb.owlapi.search.EntitySearcher;

// TODO: Auto-generated Javadoc
/**
 * The Class OWLKb.
 *
 * @author safrant
 */
public class OWLKb {

	// public static String getCodeFromIRIString(String iriString) {
	// try {
	// Pattern p = Pattern.compile("^<.*>$");
	// Matcher m = p.matcher(iriString);
	// if (m.matches()) {
	// // Trim first and last <>
	// final String tmp = iriString.substring(1,
	// iriString.length() - 1);
	// iriString = tmp;
	// }
	//
	// p = Pattern.compile(".*http.*");
	// m = p.matcher(iriString);
	// if (m.matches()) {
	// // IRI iri = IRI.create(iriString);
	// final URI iri = URI.create(iriString);
	// // return getCodeFromIRI(iri);
	// return OWLKb.getCodeFromURI(iri);
	// } else {
	// return iriString;
	// }
	// } catch (final Exception e) {
	// return iriString;
	// }
	//
	// }
	
	private boolean loadValid = false;
	
	public boolean isLoadValid(){
		return loadValid;
	}

	public static String getCodeFromURI(URI uri) {
		try {
			return uri.getFragment();
		} catch (final Exception e) {
			return uri.toString();
		}
	}

	// public static String getPathFromIRIString(String iriString) {
	// try {
	// Pattern p = Pattern.compile("^<.*>$");
	// Matcher m = p.matcher(iriString);
	// if (m.matches()) {
	// // Trim first and last <>
	// final String tmp = iriString.substring(1,
	// iriString.length() - 1);
	// iriString = tmp;
	// }
	//
	// p = Pattern.compile(".*http.*");
	// m = p.matcher(iriString);
	// if (m.matches()) {
	// // IRI iri = IRI.create(iriString);
	// final URI iri = URI.create(iriString);
	// // return getCodeFromIRI(iri);
	// return OWLKb.getPathFromURI(iri);
	// } else {
	// return iriString;
	// }
	// } catch (final Exception e) {
	// return iriString;
	// }
	//
	// }

	public static String getPathFromIRI(IRI iri) {
		// return iri.getNamespace();
		return iri.getNamespace();
	}

	public static String getCodeFromIRI(IRI iri) {
		return iri.getFragment();
	}

	public static String getPathFromURI(URI uri) {
		try {
			return IRI.create(uri).getNamespace();
			// return iri.getPath();
			// return iri.getSchemeSpecificPart();
		} catch (final Exception e) {
			return uri.toString();
		}
	}

	public static URI createURI(String className, String namespace) {
		if (namespace.endsWith("#") || namespace.endsWith("/")) {
			namespace = namespace.substring(0, namespace.length() - 1);
		}
		return URI.create(namespace + "#" + className);
	}

	/** The api. */
	private OwlApiLayer api = null;

	/** The concepts. */
	private HashMap<URI, ConceptProxy> concepts = new HashMap<URI, ConceptProxy>();

	// public static String getCodeFromIRI(IRI iri) {
	// try {
	// return iri.getFragment();
	// } catch (Exception e) {
	// return iri.toString();
	// }
	// }

	private boolean countsInitialized = false;

	/** The id of the root concept for the deprecated or retired branch *. */
//	private String deprecatedBranchString = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Retired_Concepts";
	private String defaultDeprecatedBranch = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428";
	private URI deprecatedBranch;

	// public void fixReferences() {
	// this.api.fixReferences();
	// }

	/** Format for encoding the OWL files. Default is UTF8 **/
	String encodingFormat = "UTF8";

	/** Whether the ontology uses XMLLiteral or not. False by default **/
	boolean hasLiterals = false;

	private Logger logger = LogManager
	        .getLogger(gov.nih.nci.evs.owl.data.OWLKb.class);

	private int numberFullyDefined;

	/** The relationships. */
	// private HashMap<String, Relationship> relationships;

	private int numberLogicallyDefined;

	private int numberTextDefined;

	private URI ontologyURI = null;



	/**
	 * Instantiates a new oWL kb.
	 *
	 * @param uri_string
	 *            the uri_string
	 * @param namespace
	 *            the namespace
	 * @throws OWLOntologyCreationException
	 * @throws Exception
	 */
	public OWLKb(String uri_string, String namespace) {

		this(uri_string, namespace, false, false);
	}

	/**
	 * Instantiates a new oWL kb.
	 *
	 * @param uri_string
	 *            the uri_string
	 * @param namespace
	 *            the namespace
	 * @throws Exception
	 */
	public OWLKb(String uri_string, String namespace, boolean useReasoner) {
		this(uri_string, namespace, useReasoner,false);
	}

	public OWLKb(String uri_string, String namespace, boolean useReasoner, boolean createNew){
		this(URI.create(uri_string), namespace, useReasoner,createNew);
	}

	/**
	 * Instantiates a new oWL kb. By default this will call a reasoner. If you
	 * don't wish to use a reasoner, then call the constructor with the boolean
	 * useReasoner and pass in false
	 *
	 * @param uri
	 *            the uri
	 * @param namespace
	 *            the namespace
	 */
	public OWLKb(URI uri, String namespace) {
		this(uri, namespace, false);

	}

	public OWLKb(URI uri, String namespace, boolean useReasoner) {
		this(uri,namespace,useReasoner,false);

	}
	
	public OWLKb(URI uri, String namespace, boolean useReasoner, boolean createNew) {
		this.ontologyURI = uri;
		try {
			this.api = new OwlApiLayer(uri, namespace, createNew);
			this.deprecatedBranch=new URI(defaultDeprecatedBranch);

			if (useReasoner) {
				// api.startHermitReasoner();
				this.api.startReasoner();
			}
			loadValid=true;
		} catch (OWLOntologyCreationException e) {
			System.out.println("Error thrown by OwlApiLayer.  Abort");
			this.logger.error("Error thrown by OwlApiLayer.  Abort", e);
		} catch (URISyntaxException e) {
	        System.out.println("Deprecated Branch is invalid URI "+ defaultDeprecatedBranch);
	        e.printStackTrace();
        } 
		
	}

	public void addAnnotationProperty(URI uri, Property newProp) {

		// addAnnotationProperty(this.getDefaultNamespace(), conceptCode, prop);
		this.addAnnotationProperty(IRI.create(uri), newProp);

	}


	//Use attQualifiedAnnotationPropertyToConcept
	public void addAnnotationProperty(IRI conceptCode, Property prop) {
		// TODO Auto-generated method stub
		this.addQualifiedAnnotationPropertytoConcept(conceptCode, prop);
	}
	
	public void addQualifiedAnnotationPropertytoConcept(IRI conceptCode, Property prop){
		this.api.addProperty(conceptCode, IRI.create(prop.getURI()), prop.getValue());
		if(prop.getQualifiers().size()>0){
		this.api.addAnnotationAssertionAxiom(prop.getCode(), conceptCode, prop.getValue(),
				prop.getQualifiers());
		}
	}
	
	public void addQualifiedAnnotationPropertytoConcept(URI conceptCode, Property prop){
		addQualifiedAnnotationPropertytoConcept(IRI.create(conceptCode), prop);
	}
	
	public void updatePropertyOnConcept(URI conceptCode, Property oldProp, Property newProp){
		removePropertyFromConcept(conceptCode, oldProp);
		addQualifiedAnnotationPropertytoConcept(conceptCode, newProp);
	}

	public void addParent(Concept concept, Concept parent) {
		// Vector<String> parents = concept.getParentCodes();
		// if (!parents.contains(parent.getCode())) {
		// this.api.addParent(concept.getCode(), parent.getCode());
		// }

		this.addParent(concept.getURI(), parent.getURI());

	}

	public void addParent(Concept concept, URI parentCode) {
		this.addParent(concept.getURI(), parentCode);
		// Vector<URI> parents = concept.getParentCodes();
		// if (!parents.contains(parentCode)) {
		//
		// this.api.addParent(IRI.create(concept.getURI()),
		// IRI.create(parentCode));
		// }
	}

	public void addParent(URI child, URI newParent) {

		ConceptProxy childConcept = this.getConcept(child);
		Vector<URI> parents = childConcept.getParentCodes();
		if (!parents.contains(newParent)) {
			this.api.addParent(IRI.create(child), IRI.create(newParent));
		}
	}

//	public void addPropertyDeclarationToOntology(String propertyName,
//	        String propertyValue) {
//		
//	}
	
	public void assignRdfLabelToConcept(URI conceptCode, String label){
		this.api.assignRDFLabel(IRI.create(conceptCode), label);
	}
	

	// public void diffHeader(OWLKb kb2, PrintWriter pw) {
	// Diff the header
	// Compare roles here vs there

	// final HashMap<String, String> roles = getAllRoles();
	// final HashMap<String, String> foreignRoles = kb2.getAllRoles();
	// Set<String> keySet = roles.keySet();
	// pw.println("Diff of Roles");
	// System.out.println("Diffing Roles");
	// for (final String key : keySet) {
	// if (!foreignRoles.containsKey(key)) {
	// pw.println(">>   " + key + " " + roles.get(key));
	// } else {
	// pw.println("     " + key + " " + roles.get(key));
	// }
	// }
	// pw.println(" ");
	// keySet = foreignRoles.keySet();
	// for (final String key : keySet) {
	// if (!roles.containsKey(key)) {
	// pw.println("<<   " + key + " " + foreignRoles.get(key));
	// } else {
	// pw.println("     " + key + " " + foreignRoles.get(key));
	// }
	// }
	//
	// final HashMap<String, String> associations = getAllAssociations();
	// final HashMap<String, String> foreignAssociations = kb2
	// .getAllAssociations();
	// keySet = associations.keySet();
	// pw.println(" ");
	// pw.println("Diff of Associations");
	// System.out.println("Diffing Associations");
	// for (final String key : keySet) {
	// if (!foreignAssociations.containsKey(key)) {
	// pw.println(">>   " + key + " " + associations.get(key));
	// } else {
	// pw.println("     " + key + " " + associations.get(key));
	// }
	// }
	// pw.println(" ");
	// keySet = foreignAssociations.keySet();
	// for (final String key : keySet) {
	// if (!associations.containsKey(key)) {
	// pw.println("<<   " + key + " " + foreignAssociations.get(key));
	// } else {
	// pw.println("     " + key + " " + foreignAssociations.get(key));
	// }
	// }
	//
	// final HashMap<String, String> properties = getAllProperties();
	// final HashMap<String, String> foreignProperties = kb2
	// .getAllProperties();
	// keySet = properties.keySet();
	// pw.println(" ");
	// pw.println("Diff of Properties");
	// System.out.println("Diffing Properties");
	// for (final String key : keySet) {
	// if (!foreignProperties.containsKey(key)) {
	// pw.println(">>   " + key + " " + properties.get(key));
	// } else {
	// pw.println("     " + key + " " + properties.get(key));
	// }
	// }
	// pw.println(" ");
	// keySet = foreignProperties.keySet();
	// for (final String key : keySet) {
	// if (!properties.containsKey(key)) {
	// pw.println("<<   " + key + " " + foreignProperties.get(key));
	// } else {
	// pw.println("     " + key + " " + foreignProperties.get(key));
	// }
	// }
	// pw.flush();
	//
	// // Diff the concepts
	// final HashMap<String, ConceptProxy> firstSetConcepts = getAllConcepts();
	// // HashMap<String, ConceptProxy> secondSetConcepts = new
	// // HashMap<String,ConceptProxy>();
	// Set<String> codes = firstSetConcepts.keySet();
	// Vector<String> diff = new Vector<String>();
	// pw.println(" ");
	// pw.println("-------------------------------------");
	// pw.println("Diff of concepts");
	// System.out.println("Diffing Concepts");
	// pw.println("");
	// for (final String code : codes) {
	// final ConceptProxy firstConcept = firstSetConcepts.get(code);
	// final ConceptProxy secondConcept = kb2.getConcept(code);
	// if (secondConcept == null
	// || secondConcept.getProperties().size() == 0) {
	// pw.println(" ");
	// pw.println(" ");
	// pw.println("-----------------------------------------------------");
	// pw.println(" ");
	// pw.println("Concept has no match");
	// diff = firstConcept.diff(null);
	// } else {
	// diff = firstConcept.diff(secondConcept);
	// }
	// for (final String s : diff) {
	// pw.println(s);
	// }
	// pw.flush();
	// diff = new Vector<String>();
	// }
	// // flip and look for concepts in kb2 that aren't in sceondSetConcepts
	// codes = kb2.getAllConcepts().keySet();
	// for (final String code : codes) {
	// if (getConcept(code) == null
	// || getConcept(code).getProperties().size() == 0) {
	// pw.println(" ");
	// pw.println(" ");
	// pw.println("-----------------------------------------------------");
	// pw.println(" ");
	// pw.println("New Concept");
	// diff = kb2.getConcept(code).diff(null);
	// }
	// for (final String s : diff) {
	// pw.println(s);
	// }
	// pw.flush();
	// diff = new Vector<String>();
	// }
	// }

	public void addPropertyToConcept(URI conceptId, PropertyProxy prop) {
		this.addPropertyToConcept(conceptId, prop.getURI(), prop.getValue());

	}

	public void addPropertyToConcept(URI conceptId, URI propertyCode,
	        String propertyValue) {
		this.api.addProperty(IRI.create(conceptId), IRI.create(propertyCode),
		        propertyValue, true);
	}

	public void addRole(URI code, Role role) {
		this.api.addRole(IRI.create(code),
		        IRI.create(role.getTarget().getURI()),
		        IRI.create(role.getCode()));
	}

	public void addRole(URI source, URI target, Role role) {
		ConceptProxy sourceConcept = this.getConcept(source);
		Vector<Role> roles = sourceConcept.getRoles();
		if (!roles.contains(role)) {
			// this.api.addRole(source,target,role.getCode(),
			// role.getRoleModifier());
			this.api.addRole(IRI.create(source), IRI.create(target),
			        IRI.create(role.getCode()));
		}
	}

	// public void addRole(URI code, URI targetCode, String targetNamespace,
	// Role role) {
	// // TODO Auto-generated method stub
	// this.api.addRole(code, targetCode, targetNamespace, role.getCode());
	// }

	public void ChangeURI(URI oldURI, URI newURI) {
		this.api.changeURI(oldURI, newURI);
		// TODO change concept map

	}

	public void ChangeIRI(IRI oldURI, IRI newURI) {
		this.api.changeIRI(oldURI, newURI);
		// TODO change concept map

	}

	public boolean conceptExists(URI conceptCode) {
		// final ConceptProxy concept = getConcept(conceptCode);
		// if (concept.getProperties().size() == 0) {
		// return false;
		// }
		// return true;

		return this.api.conceptExists(IRI.create(conceptCode));

	}

	public void constructCleanProperty(String newName, String propertyName,
	        String tagName) {
		this.api.constructCleanProperty(newName, propertyName, tagName,
		        this.hasLiterals);
	}

	public void convertUnicodeToAscii(Configuration uniConfig) {
		final Hashtable<Character, String> unicodeMap = ToMapUnicodeToAscii
		        .GetUnicodeMapFromFile(uniConfig);

		final Hashtable<Character, String> symbolMap = ToMapSymbolToAscii
		        .GetSymbolMapFromFile(uniConfig);

		final Hashtable<Character, String> stripMap = ToStripMapUnicode
		        .GetNonStripMapFromFile(uniConfig);

		this.api.convertUnicodeToAscii(unicodeMap, symbolMap, stripMap,
		        this.hasLiterals);
	}

	public ConceptProxy createConcept(URI id) {
		this.api.createClass(IRI.create(id));

		if (!this.concepts.containsKey(id)) {
			final ConceptProxy concept = new ConceptProxy(id, this);

			this.concepts.put(id, concept);
			return concept;
		}
		System.out.println("Concept already exists " + id);
		return this.getConcept(id);

	}

	// public ConceptProxy createConcept(URI id, String namespace) {
	// api.createClass(id, namespace);
	//
	// if (!this.concepts.containsKey(id)) {
	// final ConceptProxy concept = new ConceptProxy(id, this);
	//
	// this.concepts.put(id, concept);
	// return concept;
	// } else {
	// System.out.println("Concept already exists " + id);
	// return getConcept(id);
	// }
	// }

	// public Vector<Property> getAnnotationPropertiesForConcept(String
	// conceptCode) {
	// Vector<Property> properties = new Vector<Property>();
	// String propertyValue = "";
	// try {
	// Vector<OWLAnnotation> owlProps = api
	// .getAnnotationPropertiesForClass(conceptCode);
	// for (OWLAnnotation owlProp : owlProps) {
	//
	// String propertyCode = owlProp.getProperty().getIRI()
	// .getFragment();
	// String propertyName = api.getPropertyName(propertyCode);
	// // String propertyValue =
	// // owlProp.getAnnotationValueAsConstant().getLiteral();
	// // TODO switch some of this to OWLAPI
	// OWLAnnotationValue annotationValue = owlProp.getValue();
	// // Set<OWLEntity> signatures = annotationValue.getSignature();
	// // System.out.println(annotationValue.toString());
	// if (annotationValue instanceof OWLLiteral) {
	// OWLLiteral propertyTemp = (OWLLiteral) annotationValue;
	// propertyValue = propertyTemp.getLiteral();
	// } else if (annotationValue instanceof IRI) {
	// propertyValue = IRI.create(annotationValue.toString())
	// .getFragment();
	// } else {
	// propertyValue = annotationValue.toString();
	// }
	// // } else if (annotationValue instanceof IRI) {
	// // IRI propertyTemp = (IRI) annotationValue;
	// // propertyValue = propertyTemp.getFragment();
	// // System.out.println("IRI " + propertyValue);
	// // } else if (annotationValue instanceof OWLAnonymousIndividual)
	// // {
	// // OWLAnonymousIndividual propertyTemp =
	// // (OWLAnonymousIndividual) annotationValue;
	// // propertyValue = propertyTemp.toString();
	// // System.out.println("Individual " + propertyValue);
	// // // TODO figure out what this will look like;
	// // }
	//
	// Property prop = new Property(propertyCode, propertyName,
	// propertyValue);
	// properties.add(prop);
	// }
	// Collections.sort(properties);
	// } catch (Exception e) {
	// System.out.println(conceptCode);
	// e.printStackTrace();
	// }
	// return properties;
	//
	// }

	public ConceptProxy createConcept(URI id, String name) {
		this.api.createClass(IRI.create(id));
		if (!this.concepts.containsKey(id)) {
			final ConceptProxy concept = new ConceptProxy(id, name, this);
			this.concepts.put(id, concept);
			return concept;
		}
		System.out.println("Concept already exists " + id);
		return this.getConcept(id);
	}

	public Vector<URI> getAllAncestorsForConcept(URI conceptCode) {
		final Vector<IRI> codes = this.api.getAllAncestorsForConcept(IRI
		        .create(conceptCode));
		Vector<URI> codeURIs = new Vector<URI>();
		for (IRI iri : codes) {
			codeURIs.add(iri.toURI());
		}
		return codeURIs;
	}

	/**
	 * Gets the all associations.
	 * @return 
	 *
	 * @return the all associations
	 */
public HashMap<URI, String> getAllAssociations(){
	HashMap<URI, String> names = api.getAllAssociations();
	final HashMap<URI, String> sortedMap = sortMapByURIKey(names);
	return sortedMap;
}

	public Set<URI> getAllConceptCodes() {
		return this.getAllConcepts().keySet();
	}

	/**
	 * Gets the all concepts.
	 *
	 * @return a hashmap of all concepts in the ontology
	 */
	public HashMap<URI, ConceptProxy> getAllConcepts() {
		// HashMap<String,ConceptProxy> concepts= new HashMap<String,
		// ConceptProxy>();
		if (this.concepts.size() > 0) return this.concepts;
		final Vector<IRI> conceptCodes = this.api.getAllConceptIRIs();
		for (final IRI conceptIRI : conceptCodes) {
			// if (conceptCode
			// .contains("Childhood_Central_Nervous_System_Neoplasm")) {
			// String debug = "TRUE";
			// }
			IRI conceptCode = conceptIRI;
			String conceptNamespace = OwlApiLayer.getDomainFromIRI(conceptIRI);
			final ConceptProxy concept = new ConceptProxy(conceptCode.toURI(),
			        this);
			this.concepts.put(conceptCode.toURI(), concept);
		}

		final HashMap<URI, ConceptProxy> sortedMap = this
		        .sortConceptsByKey(this.concepts);
		return sortedMap;
	}

	// public ConceptProxy getConcept(IRI conceptIRI){
	// String tempCode = conceptIRI.getFragment();
	// ConceptProxy concept = new ConceptProxy(tempCode, this);
	// return concept;
	// }

	/**
	 * Gets the children for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the parents for concept
	 */
	public Vector<URI> getAllDescendantsForConcept(URI conceptCode) {
		final Vector<IRI> codes = this.api.getAllDescendantsForConcept(IRI
		        .create(conceptCode));
		Vector<URI> codeURIs = new Vector<URI>();
		for (IRI iri : codes) {
			codeURIs.add(iri.toURI());
		}
		return codeURIs;
	}

	@Deprecated
	/**
	 * Use getAllDescendantsForConcept(URI conceptCode). Assumes NCIt namespace
	 * @param conceptCode
	 * @return
	 */
	public Vector<URI> getAllDescendantsForConcept(String conceptCode) {
		return this.getAllDescendantsForConcept(this.api
		        .makeUriFromCode(conceptCode));
	}

	/**
	 * Gets the all properties.
	 *
	 * @return the all properties
	 */
	public HashMap<URI, String> getAllProperties() {
		final HashMap<IRI, String> names = this.api.getAllProperties();
		HashMap<URI, String> nameURIs = new HashMap<URI, String>();
		for (IRI iri : names.keySet()) {
			nameURIs.put(iri.toURI(), names.get(iri));
		}
		final HashMap<URI, String> sortedMap = sortMapByURIKey(nameURIs);
		return sortedMap;
	}

	@Deprecated
	/*
	 * Cannot possibly work.  Could get multiple properties and not know
	 * which qualifiers are applicable
	 */
	public Vector<QualifierProxy> getAllQualifiersForProperty(URI conceptCode,
	        PropertyProxy property) {

		// load axioms on property
		return this.api.getQualifiers(IRI.create(conceptCode), property);
		// return qualifiers;

	}

	/**
	 * Gets the all roles.
	 *
	 * @return the all roles as a map of <code, name>
	 */
	public HashMap<URI, String> getAllRoles() {
		final HashMap<IRI, String> names = this.api.getAllRoleNames();
		HashMap<URI, String> nameURIs = new HashMap<URI, String>();
		for (IRI iri : names.keySet()) {
			nameURIs.put(iri.toURI(), names.get(iri));
		}
		final HashMap<URI, String> sortedMap = sortMapByURIKey(nameURIs);
		return sortedMap;
	}

	public Vector<URI> getAncestorCodes(URI code) {
		Vector<URI> ancestorURIs = new Vector<URI>();
		for (IRI iri : this.api.getAllAncestorsForConcept(IRI.create(code))) {
			ancestorURIs.add(iri.toURI());
		}
		return ancestorURIs;
	}

	public Vector<Property> getAnnotationPropertiesForConcept(URI conceptURI) {

		// Set<OWLAnnotationAssertionAxiom> oaaa =
		// ontology.getAnnotationAssertionAxioms(cls.getIRI());

		IRI conceptIRI = IRI.create(conceptURI);
		final Vector<Property> properties = new Vector<Property>();
		Vector<Qualifier> qualifiers = new Vector<Qualifier>();
		try {
			Set<OWLAnnotationAssertionAxiom> propertyAxioms = this.api
			        .getAnnotationAssertionAxiomsForClass(conceptIRI);
			Iterator<OWLAnnotationAssertionAxiom> iter = propertyAxioms
			        .iterator();
			while (iter.hasNext()) {
				// if ((propertyAxioms != null) && (propertyAxioms.size() > 0))
				// {
				// for (OWLAnnotationAssertionAxiom oaaax : propertyAxioms) {
				OWLAnnotationAssertionAxiom oaaax = iter.next();
				qualifiers = new Vector<Qualifier>();
				Set<OWLAnnotation> quals = oaaax.getAnnotations();
				for (OWLAnnotation annoQual : quals) {
					OWLAnnotationProperty aqp = annoQual.getProperty();
					OWLAnnotationValue aqv = annoQual.getValue();
					PropertyDef propD = new PropertyDef(aqp.getIRI(), api.getPropertyNameByCode(aqp.getIRI()));
					String qualName = aqp.getIRI().getFragment();
					String qualValue = aqv.toString();
					Qualifier qualifier;
					try {
						if (aqv instanceof OWLLiteral) {
							qualifier = new Qualifier(propD,
							        ((OWLLiteral) aqv).getLiteral(),this);
							qualifiers.add(qualifier);
						}

					} catch (Exception e) {
						this.logger.error("Unable to instantiate qualifier "
						        + qualName + " " + qualValue);
						e.printStackTrace();
					}

				}

				OWLAnnotationProperty owlProp = oaaax.getProperty();

				OWLAnnotationValue av = oaaax.getValue();

				final IRI propertyCode = owlProp.getIRI();
				String propertyDomain = owlProp.getIRI().getNamespace();

				if (propertyDomain.endsWith("#")
				        || propertyDomain.endsWith("/")) {
					propertyDomain = propertyDomain.substring(0,
					        propertyDomain.length() - 1);
				}

				final String propertyName = this.api
				        .getPropertyNameByCode(propertyCode);
				if (av instanceof OWLLiteral) {
					final OWLLiteral propertyValue = (OWLLiteral) av;
					PropertyDef pd = new PropertyDef(propertyCode,propertyName);
					final Property prop = new Property(pd,
					        propertyValue.getLiteral(), this, qualifiers);
					// prop.setDomain(owlProp.getIRI().getStart());
//					prop.setQualifiers(qualifiers);
					// EVS specific property names used here
					if(prop.getName()!=null){
					if (prop.getName().toUpperCase().contains("DEFINITION")
					        || prop.getName().toUpperCase().equals("DEF")) {
						prop.setPropertyType(PropertyType.DEFINITION);
					} else if (prop.getName().toUpperCase().equals("FULL_SYN")
					        || prop.getName().toUpperCase().contains("SYNONYM")
					        || prop.getName().toUpperCase().equals("SYN")) {
						prop.setPropertyType(PropertyType.SYNONYM);
					} else if (prop.getName().toUpperCase().equals("CODE")) {
						prop.setPropertyType(PropertyType.CODE);
					} else if (prop.getName().toUpperCase().contains("COMMENT")) {
						prop.setPropertyType(PropertyType.COMMENT);
					}} else {
//						System.out.println("Property has no name :"+ prop.getURI().toString());
					}

					properties.add(prop);
				} else {
					// TODO - these are likely to be inSubset dataproperties
					// put
					// into annotations by Obo2Owl. Ignore them
					String debug = "stop here";
				}

			}

		} catch (Exception e) {
			String debug = "Stop here";
			System.out.println("Error retrieving properties for "+ conceptURI);
		}
		// final Vector<OWLAnnotationProperty> owlProps = this.api
		// .getAnnotationPropertiesForClass(conceptCode);
		// for (final OWLAnnotationProperty owlProp : owlProps) {
		// // String propertyCode = owlProp.getAnnotationURI().getFragment();
		// final String propertyCode = owlProp.getProperty().getIRI()
		// .getFragment();
		// final String propertyName = this.api.getPropertyName(propertyCode);
		// // String propertyValue =
		// // owlProp.getAnnotationValueAsConstant().getLiteral();
		// // TODO switch some of this to OWLAPI
		// OWLAnnotationValue av = owlProp.getValue();
		// if (av instanceof OWLLiteral){
		// final OWLLiteral propertyValue = (OWLLiteral) owlProp.getValue();
		// final Property prop = new Property(propertyCode, propertyName,
		// propertyValue.getLiteral());
		// properties.add(prop);
		// } else {
		// //TODO - these are likely to be inSubset dataproperties put into
		// annotations but Obo2Owl. Ignore them
		// // String debug = "stop here";
		// }
		// }

		Collections.sort(properties);
		return properties;
	}

//	public Vector<Association> getAssociationsForSource(ConceptProxy source) {
//		// Vector<Relationship> associations = new Vector<Relationship>();
//		// Vector<OWLAnnotation> owlProps =
//		// api.getAssociationsForSource(source.getCode());
//		return this.getAssociationsForSource(source.getURI());
//		// Vector<Relationship> associations =
//		// api.getAssociationsForSource(source.getCode());
//
//		// for (OWLAnnotation owlProp : owlProps) {
//		// String assocCode = owlProp.getProperty().getIRI().getFragment();
//		// String assocName = getAllAssociations().get(assocCode);
//		// RelationDef rel = new RelationDef(assocCode, assocName);
//		//
//		// IRI assocTarget = (IRI) owlProp.getValue();
//		// ConceptProxy target = getConcept(assocTarget);
//		//
//		// Association assoc = new Association(rel, source, target);
//		// associations.add(assoc);
//		// }
//		// Collections.sort((List<Relationship>) associations);
//		// return associations;
//	}


	// @SuppressWarnings("unchecked")
	// public Vector<Relationship> getAssociationsForConcept(String conceptCode)
	// {
	//
	// Vector<Relationship> r = new Vector<Relationship>();
	// ConceptProxy concept = new ConceptProxy(conceptCode, this);
	//
	// String propertyValue = "";
	// try {
	// Vector<OWLAnnotation> owlProps = api
	// .getAnnotationPropertiesForClass(conceptCode);
	// for (OWLAnnotation owlProp : owlProps) {
	//
	// String propertyCode = owlProp.getProperty().getIRI()
	// .getFragment();
	// String propertyName = api.getPropertyName(propertyCode);
	//
	// OWLAnnotationValue annotationValue = owlProp.getValue();
	//
	// if (annotationValue instanceof IRI) {
	// IRI propertyTemp = (IRI) annotationValue;
	// propertyValue = propertyTemp.getFragment();
	//
	// }
	// if (api.getAllAssociations().containsKey(propertyCode)) {
	// AssociationDef assocDef = new AssociationDef(propertyCode);
	//
	//
	// ConceptProxy target = new ConceptProxy(propertyValue, this);
	//
	//
	// Association rel = new Association(assocDef, concept, target);
	//
	//
	// }
	//
	// // Property prop = new Property(propertyCode, propertyName,
	// // propertyValue);
	// // properties.add(prop);
	// }
	// // Collections.sort(properties);
	// } catch (Exception e) {
	// // System.out.println(conceptCode);
	// e.printStackTrace();
	// }
	// Collections.sort((List) r);
	// return r;
	// }

	@Deprecated
	/**
	 * User getAssociationsForSource(URI uri)
	 * @param string
	 * @return
	 */
	public Vector<Association> getAssociationsForSource(String sourceCode) {

		return this.getAssociationsForSource(this.api
		        .makeUriFromCode(sourceCode));

	}
	
	public Vector<Association> getAssociationsForSource(URI code){
		final ConceptProxy source = this.getConcept(code);
		return getAssociationsForSource(source);
	}

	public Vector<Association> getAssociationsForSource(ConceptProxy source) {

//		final ConceptProxy source = this.getConcept(code);
		final Vector<Association> associations = new Vector<Association>();
		final Vector<AssociationVisitor> aVisitors = this.api
		        .getAssociationsForSource(source);

		for (final AssociationVisitor owlProp : aVisitors) {
			// final String assocCode = owlProp.getProperty().getIRI()
			// .getFragment();
			URI assocCode = owlProp.getAssociationCode().toURI();

			HashMap<URI,String> associationMap = this.getAllAssociations();
			String assocName = associationMap.get(assocCode);

			final RelationDef rel = new RelationDef(assocCode,
			        assocName);


			try {
				final IRI assocTarget = owlProp.getTargetCode();
				if (assocTarget == null) {
					System.out.println("WARNING: assocTarget == null for assocCode " + assocCode.toString() + " at (code: " + source.getCode() + ")");
				} else {
					final ConceptProxy target = this.getConcept(assocTarget.toURI());

					if (target == null) {
						System.out.println("target == null for concept " + source.getCode()+ " assoc +" + assocCode.toString());
					} else {

						final Association assoc = new Association(rel, source, target);
						associations.add(assoc);
					}
				}


			} catch (Exception ex) {
				ex.printStackTrace();
			}


		}

		Collections.sort(associations);
		return associations;



	}

	public Vector<Association> getAssociationsForSource(URI conceptCode,
	        String assocID) {
		Vector<Association> r = new Vector<Association>();
		final ConceptProxy concept = new ConceptProxy(conceptCode, this);

		r = concept.getAssociations(assocID);
		return r;
	}
	
	public Vector<Association> getAssociationsForTarget(URI code){
		ConceptProxy target = this.getConcept(code);
		return getAssociationsForTarget(target);
	}

	public Vector<Association> getAssociationsForTarget(ConceptProxy target) {
		final Vector<Association> associations = new Vector<Association>();
//TODO Results in Exception in thread "main" java.lang.UnsupportedOperationException: RDF Literals do not have IRIs



		// TODO build a map of reverse association - someday
		// r = api.getAssociationsForTarget(concept);

		// try{
		// Vector<RoleDescriptionVisitor> visitors =
		// api.getAssociationsForTarget(concept.getCode());
		// }
		Collection<RDFTriple> triples = api.getAssociationsForTarget(target);
		
    	for(RDFTriple triple:triples){
    		
			
			final String assocName = this.getAllAssociations().get(triple.getPredicate().getIRI().toURI());
			final RelationDef rel = new RelationDef(triple.getPredicate().getIRI().toURI(),
			        assocName);
			
			ConceptProxy source = new ConceptProxy(triple.getSubject().getIRI().toURI(),this);

			final Association assoc = new Association(rel, source, target);
			associations.add(assoc);
		
    	}
		Collections.sort(associations);
		return associations;
		

	}

	/**
	 * Gets the children for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the parents for concept
	 */
	public Vector<URI> getChildrenForConcept(URI conceptCode) {
		Vector<URI> children = new Vector<URI>();
		final Vector<IRI> codes = this.api.getChildrenForConcept(IRI
		        .create(conceptCode));
		for (IRI iri : codes) {
			children.add(iri.toURI());
		}
		return children;
	}


	// public ConceptProxy getConcept(URI conceptCode) {
	// // final String tempCode = OWLKb.getCodeFromIRIString(conceptCode);
	// // final URI tempCode = IRI.create(conceptCode);
	// final ConceptProxy concept = new ConceptProxy(conceptCode, this);
	// return concept;
	// }

	public ConceptProxy getConcept(URI conceptURI) {
		// final String tempCode = conceptURI.getFragment();
//		final ConceptProxy concept = new ConceptProxy(conceptURI, this);
//		return concept;
		return this.getAllConcepts().get(conceptURI);
	}

	@Deprecated
	/**
	 * Use getConcept(URI uri)
	 * @param string
	 * @return
	 */
	public ConceptProxy getConcept(String code) {
		return this.getConcept(this.api.makeUriFromCode(code));
	}

	public boolean isValidURL(String testString) {
		try {
			URL testURL = new URL(testString);
			testURL.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	public URI makeURIfromCode(String code) {

		return this.api.makeUriFromCode(code);

	}

	/**
	 * Gets the concept name by code.
	 *
	 * @param code
	 *            the code
	 * @return the concept name by code
	 */
	public String getConceptNameByCode(URI code) {
		return this.getConceptNameByCode(IRI.create(code));
	}

	public String getConceptNameByCode(IRI code) {
		return this.api.getClassName(code);
	}

	public Vector<ConceptProxy> getConceptVector() {
		final Set<URI> keySet = this.getAllConcepts().keySet();
		// Iterator iter = key.iterator();
		final Vector<ConceptProxy> conceptVector = new Vector<ConceptProxy>();
		for (final URI key : keySet) {
			conceptVector.add(this.concepts.get(key));
		}
		return conceptVector;

	}

	/**
	 * Gets the properties for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the properties for concept
	 */

	// public Vector<Property> getDataPropertiesForConcept(String conceptCode) {
	// return getDataPropertiesForConcept(this.api.getNamespace(), conceptCode);

	// final Vector<Property> properties = new Vector<Property>();
	// final Vector<OWLAnnotation> owlProps = this.api
	// .getDataPropertiesForClass(conceptCode);
	// for (final OWLAnnotation owlProp : owlProps) {
	// // String propertyCode = owlProp.getAnnotationURI().getFragment();
	// final String propertyCode = owlProp.getProperty().getIRI()
	// .getFragment();
	// String propertyDomain = owlProp.getProperty().getIRI()
	// .getStart();
	// if (propertyDomain.endsWith("#") || propertyDomain.endsWith("//")) {
	// propertyDomain = propertyDomain.substring(0,
	// propertyDomain.length() - 1);
	// }
	// final String propertyName = this.api
	// .getPropertyNameByCode(propertyCode);
	// // String propertyValue =
	// // owlProp.getAnnotationValueAsConstant().getLiteral();
	// // TODO switch some of this to OWLAPI
	// final OWLLiteral propertyValue = (OWLLiteral) owlProp.getValue();
	//
	// final Property prop = new Property(propertyDomain, propertyCode,
	// propertyName,
	// propertyValue.getLiteral());
	// properties.add(prop);
	// }
	// Collections.sort(properties);
	// return properties;

	// }

	public Vector<Property> getDataPropertiesForConcept(URI conceptCode) {

		final Vector<Property> properties = new Vector<Property>();
		final Vector<OWLAnnotationAssertionAxiom> owlProps = this.api
		        .getDataPropertiesForClass(IRI.create(conceptCode));
		for (final OWLAnnotationAssertionAxiom owlProp : owlProps) {
			// String propertyCode = owlProp.getAnnotationURI().getFragment();
			final IRI propertyCode = owlProp.getProperty().getIRI();
			String propertyDomain = owlProp.getProperty().getIRI()
			        .getNamespace();
			if (propertyDomain.endsWith("#") || propertyDomain.endsWith("//")) {
				propertyDomain = propertyDomain.substring(0,
				        propertyDomain.length() - 1);
			}
			final String propertyName = this.api
			        .getPropertyNameByCode(propertyCode);
			// String propertyValue =
			// owlProp.getAnnotationValueAsConstant().getLiteral();
			// TODO switch some of this to OWLAPI
			final OWLLiteral propertyValue = (OWLLiteral) owlProp.getValue();

			final Property prop = new Property(propertyCode.toURI(),propertyName,
			        propertyValue.getLiteral(), this);
			properties.add(prop);
		}
		Collections.sort(properties);
		return properties;

	}

	public String getDefaultNamespace() {
		return this.api.getNamespace();
	}

	public URI getDeprecatedBranch() {
		try {
			if (this.deprecatedBranch == null) {
				this.deprecatedBranch = new URI(this.defaultDeprecatedBranch);
			}
			return this.deprecatedBranch;
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Gets the domain for role.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the domain for role
	 */
	public Vector<URI> getDomainForRole(URI roleCode) {
		Vector<URI> domainURIs = new Vector<URI>();
		for (IRI iri : this.api.getDomainForRole(IRI.create(roleCode))) {
			domainURIs.add(iri.toURI());
		}
		return domainURIs;
	}

	public String getEncodingFormat() {
		return this.encodingFormat;
	}

	public Vector<Role> getEquivalentClassRoles(ConceptProxy concept) {
		final Vector<Role> r = new Vector<Role>();
		try {
			final Vector<RoleDescriptionVisitor> visitors = this.api
			        .getEquivalentClassRoles(IRI.create(concept.getURI()));

			for (final RoleDescriptionVisitor visitor : visitors) {
				if (visitor.getExpression() != null) {
					// TODO move this to api.
					final String roleID = this.api
					        .getRDFSid((OWLEntity) visitor.getExpression());

					final String roleLabel = this.api
					        .getRDFSLabel((OWLEntity) visitor.getExpression());
					IRI roleIRI = visitor.getExpression().asOWLObjectProperty()
					        .getIRI();
					URI targetURI = visitor.getFillerIRI().toURI();
					final RoleProxy roleDef = new RoleProxy(roleIRI.toURI(),
					        this);
					// URI targetURI = IRI.create(target).toURI();
					// TODO watch this. Not sure it will convert correctly
					// final String targetCode = OWLKb
					// .getCodeFromIRIString(visitor.getFillerString());
					final ConceptProxy target = new ConceptProxy(targetURI,
					        this);
					final Role rel = new Role(roleDef, concept, target);
					r.add(rel);
					roleDef.getRange();
				}
				// else {
				// new RoleDef("subClassOf", this);
				// }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;

	}

	public Vector<Role> getEquivalentClassRoles(URI conceptCode) {
		final ConceptProxy concept = new ConceptProxy(conceptCode, this);
		return this.getEquivalentClassRoles(concept);
	}

	public boolean getHasLiterals() {
		return this.hasLiterals;
	}

	public boolean getIsAnonymous(URI code) {
		return this.api.getIsAnonymous(IRI.create(code));
	}

	public boolean getIsDefined(URI uri) {
		return this.api.getIsDefined(IRI.create(uri));
	}

	/**
	 *
	 * Gets the is transitive role.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the checks if is transitive role
	 */
	public boolean getIsTransitiveRole(String roleCode) {
		return this.api.getIsTransitive(roleCode);
	}

	public void getNamespacePrefixes() {
		this.api.getNamespacePrefixes();
	}

	public int getNumberOfFullyDefinedConcepts() {
		if (!this.countsInitialized) {
			this.initializeCounts();
		}
		return this.numberFullyDefined;
	}

	// public HashMap<String, Relationship> getRelationships() {
	// return relationships;
	// }

	public int getNumberOfLogicallyDefinedConcepts() {
		if (!this.countsInitialized) {
			this.initializeCounts();
		}
		return this.numberLogicallyDefined;
	}

	public int getNumberOfTextuallyDefinedConcepts() {
		if (!this.countsInitialized) {
			this.initializeCounts();
		}
		return this.numberTextDefined;
	}

	/**
	 * Gets the parents for concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the parents for concept
	 */
	// public Vector<String> getParentCodesForConcept(String conceptCode) {
	// final Vector<String> codes = this.api
	// .getParentCodesForConcept(conceptCode);
	// return codes;
	// }

	public Vector<URI> getParentCodesForConcept(URI conceptCode) {
		final Vector<IRI> iris = this.api.getParentCodesForConcept(IRI
		        .create(conceptCode));
		Vector<URI> codes = new Vector<URI>();
		for (IRI iri : iris) {
			codes.add(iri.toURI());
		}
		return codes;
	}

	@Deprecated
	/** Use getParentCodesForConcept(URI conceptCode)
	 *
	 * @param string
	 * @return
	 */
	public Vector<String> getParentsForConcept(String string) {

		Vector<URI> uris;

		uris = this.getParentCodesForConcept(this.api.makeUriFromCode(string));

		Vector<String> parentCodes = new Vector<String>();
		for (URI parentURI : uris) {
			parentCodes.add(parentURI.toString());
		}
		return parentCodes;

	}

	public Vector<PropertyDef> getParentsForProperty(URI propCode) {
		Vector<PropertyDef> parents = new Vector<PropertyDef>();
		Vector<IRI> parentCodes = this.api.getParentsForDataProperty(IRI
		        .create(propCode));
		if (!(parentCodes.size() > 0)) {
			parentCodes = this.api.getParentsForAnnotationProperty(IRI
			        .create(propCode));
		}

		for (IRI code : parentCodes) {
			try {
				String name = this.api.getPropertyNameByCode(code);
				PropertyDef property = new PropertyDef(code, name);
				parents.add(property);
			} catch (Exception e) {
				this.logger.error("Unable to instantiate property definition "
				        + code);
			}
		}
		return parents;
	}

	/**
	 * Gets the parents for role.
	 *
	 * @param uri
	 *            the role code
	 * @return the parents for role
	 */
	public Vector<RoleDef> getParentsForRole(URI uri) {
		final Vector<IRI> codes = this.api.getParentsForRole(IRI.create(uri));
		final Vector<RoleDef> roles = new Vector<RoleDef>();
		for (final IRI code : codes) {
			try {
				final RoleDef role = new RoleDef(code.toURI(), this);
				roles.add(role);
			} catch (RoleException e) {
				this.logger.error("Unable to instantiate role definition "
				        + code);
			}
		}
		return roles;
	}

	public Vector<URI> getParentURIForConcept(URI uri) {

		Vector<IRI> temp = this.api.getParentCodesForConcept(IRI.create(uri));
		return this.api.getParentURIForConcept(IRI.create(uri));
	}

	public void getPathToRoot(URI uri) {
		NodeSet treeList = this.api.getPathToRoot(IRI.create(uri));
	}

	// // This exists for debugging
	// @SuppressWarnings("unused")
	// private void getPropertiesForConcept(IRI conceptCode) {
	// final Vector<OWLAnnotation> owlProps = this.api
	// .getDataPropertiesForClass(conceptCode);
	// final Vector<OWLAnnotationProperty> owlAnnos = this.api
	// .getAnnotationPropertiesForClass(conceptCode);
	// if ((owlProps.size() > 0) || (owlAnnos.size() > 0)) {
	// boolean hasProps = true;
	// }
	// }

	public PropertyDef getPropertyForCode(URI propCode) {
		PropertyDef propDef = null;
		try {
			// OWLObjectProperty oProp = api.getRoleMap().get(code);
			String name = this.api.getPropertyNameByCode(IRI.create(propCode));

			propDef = new PropertyDef(IRI.create(propCode), name);
			return propDef;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return propDef;
	}

	// public PropertyDef getPropertyForDomainAndCode(String domain,
	// String propCode) {
	// PropertyDef propDef = null;
	// try {
	// String name = api.getPropertyNameByCode(propCode);
	// propDef = new PropertyDef(domain, propCode, name);
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// return propDef;
	// }

	/**
	 * Gets the property name by code.
	 *
	 * @param propertyCode
	 *            the property code
	 * @return the property name by code
	 */
	public String getPropertyNameByCode(String propertyCode) {
		final String name = this.api.getPropertyNameByCode(IRI
		        .create(propertyCode));
		return name;
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
	public Vector<String> getPropertyValues(URI conceptCode, URI propertyCode) {
		final Vector<String> values = this.api.getPropertyValues(
		        IRI.create(conceptCode), IRI.create(propertyCode));
		return values;
	}

	/**
	 * Gets the property values. Assumes NCIt namespace
	 *
	 * @param conceptCode
	 *            the concept code
	 * @param propertyCode
	 *            the property code
	 * @return the property values
	 */
	public Vector<String> getPropertyValues(String conceptCode,
	        String propertyCode) {
		URI conceptCodeURI = URI.create(this.getDefaultNamespace() + "#"
		        + conceptCode);
		URI propertyCodeURI = URI.create(this.getDefaultNamespace() + "#"
		        + propertyCode);
		return this.getPropertyValues(conceptCodeURI, propertyCodeURI);
	}

	public Vector<String> getQualifiers(URI conceptCode, URI propertyCode,
	        String qualifierCode) {
		return this.api.getQualifierStrings(IRI.create(conceptCode),
		        IRI.create(propertyCode), IRI.create(qualifierCode));
	}

	public Vector<String> getQualifiers(URI conceptCode, URI propertyCode,
	        URI qualifierCode) {
		return this.api.getQualifierStrings(IRI.create(conceptCode),
		        IRI.create(propertyCode), IRI.create(qualifierCode));
	}

	@Deprecated
	/**
	 * Use getQualifiers(URI conceptCode, URI propertyCode, String qualifierCode)
	 * @param conceptCode
	 * @param propertyCode
	 * @param qualifierCode
	 * @return
	 */
	public Vector<String> getQualifiers(String conceptCode,
	        String propertyCode, String qualifierCode) {
		URI conceptURI = this.api.makeUriFromCode(conceptCode);
		URI propertyURI = this.api.makeUriFromCode(propertyCode);
		URI qualifierURI = this.api.makeUriFromCode(qualifierCode);
		return this.getQualifiers(conceptURI, propertyURI, qualifierURI);
	}

	/**
	 * Gets the range for role.
	 *
	 * @param uri
	 *            the role code
	 * @return the range for role
	 */
	public Vector<URI> getRangeForRole(URI uri) {
		Vector<URI> rangeURI = new Vector<URI>();
		for (IRI iri : this.api.getRangeForRole(IRI.create(uri))) {
			rangeURI.add(iri.toURI());
		}
		return rangeURI;
	}

	/**
	 * Gets the role for code.
	 *
	 * @param code
	 *            the id of the role
	 * @return the roleDef object for the code
	 */
	public RoleDef getRoleForCode(URI code) {
		RoleDef roleDef = null;
		try {
			// OWLObjectProperty oProp = api.getRoleMap().get(code);
			roleDef = new RoleDef(code, this);
			roleDef.setTransitive(this.api.isRoleTransitive(IRI.create(code)));
			roleDef.setName(this.api.getRoleNameByCode(IRI.create(code)));
			return roleDef;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return roleDef;
	}

	/**
	 * Gets the role has parent.
	 *
	 * @param roleCode
	 *            the role code
	 * @return the role has parent
	 */
	public boolean getRoleHasParent(URI roleCode) {
		return this.api.getRoleHasParent(IRI.create(roleCode));
	}

	public String getRoleNameByCode(URI uri) {
		return this.api.getRoleNameByCode(IRI.create(uri));
	}

	/**
	 * Gets the roles for source.
	 *
	 * @param concept
	 *            the concept
	 * @return the roles for source
	 */
	public Vector<Role> getRolesForSource(ConceptProxy concept) {
		final Vector<Role> r = new Vector<Role>();

		try {
			final Vector<RoleDescriptionVisitor> visitors = this.api
			        .getRolesForSource(IRI.create(concept.getURI()));

			for (final RoleDescriptionVisitor visitor : visitors) {
				if (visitor.getExpression() != null) {
					final String roleLabel = this.api
					        .getRDFSid((OWLEntity) visitor.getExpression());
					final RoleProxy roleDef = new RoleProxy(visitor
					        .getExpression().asOWLObjectProperty().getIRI()
					        .toURI(), this);
					final String targetCode = OWLKb.getCodeFromIRI(visitor
					        .getFillerIRI());
					final String targetNamespace = OWLKb.getPathFromIRI(visitor
					        .getFillerIRI());

					final IRI targetIRI = visitor.getFillerIRI();
					final ConceptProxy target = new ConceptProxy(
					        targetIRI.toURI(), this);
					final Role rel = new Role(roleDef, concept, target);
					r.add(rel);
					// roleDef.getRange();
				}
				// else {
				// new RoleDef("subClassOf", this);
				// }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	// public void removeBranch(IRI conceptIRI) {
	// api.removeBranch(conceptIRI);
	// api.fixReferences();
	// }

	/**
	 * Gets the roles for source.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the roles for source
	 */
	public Vector<Role> getRolesForSource(URI conceptCode) {
		final ConceptProxy concept = new ConceptProxy(conceptCode, this);
		return this.getRolesForSource(concept);
	}

	public Vector<Role> getRolesForSource(URI conceptCode, String roleID) {
		final ConceptProxy concept = new ConceptProxy(conceptCode, this);
		return concept.getRoles(roleID);
	}

	public Vector<Role> getRolesForTarget(URI conceptCode){
		final ConceptProxy conceptProxy = new ConceptProxy(conceptCode, this);
		return this.getRolesForTarget(conceptProxy);
	}

	/**
	 * Gets the roles for target.
	 *
	 * @param concept
	 *            the concept
	 * @return the roles for target
	 */
	public Vector<Role> getRolesForTarget(ConceptProxy concept) {
		final Vector<Role> r = new Vector<Role>();
		try {
			final Vector<RoleDescriptionVisitor> visitors = this.api
			        .getRolesForTarget(IRI.create(concept.getURI()));

			// for(String roleString : roleNames) {
			// Relationship rel = parseRoleString(roleString, concept);
			// }
			for (final RoleDescriptionVisitor visitor : visitors) {
				if (visitor.getExpression() != null) {
					final String roleCode = this.api
					        .getRDFSLabel((OWLEntity) visitor.getExpression());
					final RoleProxy roleDef = new RoleProxy(visitor
					        .getExpression().asOWLObjectProperty().getIRI()
					        .toURI(), this);
					final String targetCode = OWLKb.getCodeFromIRI(visitor
					        .getFillerIRI());
					final String sourceCode = OWLKb.getCodeFromIRI(visitor
					        .getSourceIRI());
					IRI targetIRI = visitor.getFillerIRI();
					IRI sourceIRI = visitor.getSourceIRI();
					final ConceptProxy target = new ConceptProxy(
					        targetIRI.toURI(), this);
					final ConceptProxy source = new ConceptProxy(
					        sourceIRI.toURI(), this);
					final Role rel = new Role(roleDef, source, target);
					r.add(rel);
					// roleDef.getRange();
				}
				// else {
				// new RoleDef("subClassOf", this);
				// }
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;

	}

	public URI getRootConceptCodeforConcept(URI conceptCode) {
		return this.api.getRootConceptCodeForConcept(IRI.create(conceptCode))
		        .toURI();
	}

	public Vector<URI> getRootConceptCodes() {
		Vector<URI> rootURIs = new Vector<URI>();
		for (IRI iri : this.api.getRootConceptCodes()) {
			rootURIs.add(iri.toURI());
		}
		return rootURIs;
	}

	// public String removeNamespaceFromIdentifier(String rawID) {
	// return api.removeNamespaceFromIdentifier(rawID);
	// }

	/**
	 * Gets the root concept for the branch where this concept s located.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return the concept proxy
	 */
	public ConceptProxy getRootConceptforConcept(URI conceptCode) {
		final IRI rootCode = this.api.getRootConceptCodeForConcept(IRI
		        .create(conceptCode));
		final ConceptProxy outConcept = new ConceptProxy(rootCode.toURI(), this);
		return outConcept;
	}

	public Vector<ConceptProxy> getRootConcepts() {
		final Vector<IRI> conceptCodes = this.api.getRootConceptCodes();
		final Vector<ConceptProxy> tmpConcepts = new Vector<ConceptProxy>();
		for (final IRI code : conceptCodes) {
			String name = this.getConceptNameByCode(code);
			final ConceptProxy concept = new ConceptProxy(code.toURI(), name,
			        this);
			tmpConcepts.add(concept);
		}
		return tmpConcepts;
	}

	public String getSolePropertyValue(URI conceptCode, URI propertyCode) {
		return this.api.getSolePropertyValue(IRI.create(conceptCode),
		        IRI.create(propertyCode));
	}

	@Deprecated
	/**
	 * Use getSolePropertyValue(URI conceptCode, URI propertyCode)
	 * @param conceptCode
	 * @param propertyCode
	 * @return
	 */
	public String getSolePropertyValue(String conceptCode, String propertyCode) {
		URI conceptURI = this.api.makeUriFromCode(conceptCode);
		URI propertyURI = this.api.makeUriFromCode(propertyCode);
		return this.getSolePropertyValue(conceptURI, propertyURI);
	}

	private void initializeCounts() {
		if (this.concepts.size() == 0) {
			this.getAllConcepts();
		}
		final Set<URI> keyIterator = this.concepts.keySet();
		final Iterator<URI> iter = keyIterator.iterator();
		while (iter.hasNext()) {
			final ConceptProxy concept = this.concepts.get(iter.next());
			final Vector<Property> defs = concept.getProperties("DEFINITION");
			if (defs.size() > 0) {
				this.numberTextDefined++;
			}
			final Vector<Role> roles = concept.getRoles();
			if (roles.size() > 0) {
				this.numberLogicallyDefined++;
			}
			final Vector<Role> equivalents = concept.getEquivalentClasses();
			if (equivalents.size() > 0) {
				this.numberFullyDefined++;
			}
		}
		this.countsInitialized = true;
	}

	public boolean isDeprecated(URI conceptCode) {
		return this.api.isDeprecated(IRI.create(conceptCode));

	}

	public boolean isDeprecated(ConceptProxy concept) {
		return this.isDeprecated(concept.getURI());
	}

	@Deprecated
	/**
	 * Use isDeprecated(URI conceptCode)
	 * @param conceptCode
	 * @return
	 * @throws URISyntaxException
	 */
	public boolean isDeprecated(String conceptCode) throws URISyntaxException {

		return this.isDeprecated(this.api.makeUriFromCode(conceptCode));
	}

	
	Vector<URI> retiredConcepts = null;
	/**
	 * Checks if is deprecated. For OWL1, need to set the root name for the
	 * DeprecatedTree. Default = 'Retired_Concepts'
	 *
	 * @param conceptCode
	 *            the concept code
	 * @return true, if checks if is deprecated
	 */
	public boolean isRetired(URI conceptCode) {
		if(retiredConcepts == null){
		    retiredConcepts = this.getAllDescendantsForConcept(this.deprecatedBranch);
		}
		
		if(retiredConcepts.contains(conceptCode))
		{
			return true;
		}
		return false;
		
//		return this.api.isInRetiredBranch(IRI.create(conceptCode),
//		        IRI.create(this.getDeprecatedBranch()));
	}

	public URI makeUriFromCode(String code) {
		return this.api.makeUriFromCode(code);
	}

	/**
	 * Parses the all some values.
	 *
	 * @param roleString
	 *            the role string
	 * @param concept
	 *            the concept
	 * @param mod
	 *            the mod
	 * @return the relationship
	 */
	private Relationship parseAllSomeValues(String roleString,
	        ConceptProxy concept, RoleModifier mod) {
		// TODO Ug. This is bad. Pull the role annotations some other way
		Relationship r = null;
		final int begin = roleString.indexOf("(") + 1;
		final int end = roleString.indexOf(")");
		try {
			if ((0 < begin) && (begin < end)) {
				final String value = roleString.substring(begin, end);
				final int firstSpace = value.indexOf(" ");
				final String roleCode = value.substring(0, firstSpace);
				final String targetCode = value.substring(firstSpace + 1);
				URI targetURI = this.api.makeUriFromCode(targetCode);
				// OWLClass target = getOWLClass(targetCode);
				final ConceptProxy target = new ConceptProxy(targetURI, this);
				final RoleProxy roleDef = new RoleProxy(OwlApiLayer.createURI(
				        roleCode, this.api.getNamespace()), this);
				// role.setRoleModifier(mod);
				r = new Role(roleDef, concept, target, mod);
			} else if (roleString.length() > 0) {
				// TODO There has to be a better way than hardcoding this
				final String targetCode = roleString;
				URI targetURI = this.api.makeUriFromCode(targetCode);
				final ConceptProxy target = new ConceptProxy(targetURI, this);
				final RoleProxy roleDef = new RoleProxy(OwlApiLayer.createURI(
				        "subClassOf", this.api.getNamespace()), this);
				// role.setRoleModifier(mod);
				roleDef.setTransitive(true);
				r = new Role(roleDef, concept, target, mod);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Parses the role string.
	 *
	 * @param roleString
	 *            the role string
	 * @param concept
	 *            the concept
	 * @return the relationship
	 */
	public Relationship parseRoleString(String roleString, ConceptProxy concept) {
		Relationship r = null;
		RoleModifier mod = RoleModifier.SOME;
		// ObjectSomeValuesFrom(Anatomic_Structure_Is_Physical_Part_Of
		// Hematopoietic_System)
		try {
			if (roleString.contains("ObjectAllValuesFrom")) {
				mod = RoleModifier.ALL;
				r = this.parseAllSomeValues(roleString, concept, mod);
			} else if (roleString.contains("ObjectUnionOf")) {
				this.parseUnion();
			} else {
				r = this.parseAllSomeValues(roleString, concept, mod);
			}
		}
		// Eventually add checks for POSS, MIN, MAX,etc. Once I figure out how.}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Parses the union.
	 */
	private void parseUnion() {
		// TODO parse UnionOf

	}

	// public boolean getIsAnonymous(String code) {
	// return api.getIsAnonymous(code);
	// }

	public void refreshRootNodes() {
		this.api.refreshRootNodes();
	}

	/**
	 * This will reload the ontology from a file
	 */
	public void reloadOntology() {
		try {
			this.api = null;
			this.concepts = null;
			this.api = new OwlApiLayer(this.ontologyURI,
			        this.api.getNamespace());
			this.initializeCounts();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.logger.error("Unable to relaod ontology", e);
		}
	}

	/*
	 * This will reload the ontology into the KB Incorporating any changes done
	 * to the underlying dataset
	 */
	public void refreshOntology() {
		this.concepts = new HashMap<URI, ConceptProxy>();
		this.initializeCounts();
	}

	@Deprecated
	/**
	 * Use removeBranch(URI conceptCode)
	 * @param conceptCode
	 */
	public void removeBranch(String conceptCode) {
		URI uri = this.makeUriFromCode(conceptCode);
		this.removeBranch(uri);
		// this.api.removeBranch(conceptCode);
		// this.api.fixReferences();
	}

	public void removeBranch(URI conceptCode) {
		// IRI doIRI = IRI.create(inputURI);
		// removeBranch(doIRI);
		this.api.removeBranch(conceptCode);
		// this.api.fixReferences();
		this.concepts = new HashMap<URI, ConceptProxy>();
		this.getAllConcepts();
	}

	/**
	 * Removes the tag from a complex property.
	 *
	 * @param tag
	 *            the tag and property in format Property/tTag
	 */
	public void removeComplexTag(String tag) {
		this.api.removeInternalTag(tag, this.hasLiterals);
	}

	public void removeComplexTag(String property, String tag) {

		this.api.removeInternalTag(property, tag, this.hasLiterals);
	}

	public void removeEmptyProperties() {
		this.api.removeEmpty();
	}

	public void removeIndividuals() {
		this.api.removeIndividuals();
	}

	public void removeProperty(URI propertyCode) {
		this.api.removeProperty(IRI.create(propertyCode));
	}

	@Deprecated
	/**
	 * Use removeProperty(URI propertyCode)
	 * @param propertyCode
	 */
	public void removeProperty(String propertyCode) {
		this.removeProperty(this.api.makeUriFromCode(propertyCode));
	}
	
	public void removePropertyFromConcept(URI conceptCode, Property prop){
		//Check to see if there is only one property that matches this concept code and property code
		//If so, simply remove it.
		Vector<String> propValues = api.getPropertyValues(IRI.create(conceptCode), prop.getCode());
		if(propValues.size()==1){
			api.removePropertyFromConcept(IRI.create(conceptCode), prop.getCode());
		}
		
		//If more than one property matches the code, then start comparing qualifiers to come up with a match.
		//Once a match is found, remove that annotation and any annotations on it.
		
	}

	/**
	 * Save ontology.
	 *
	 * @param path
	 *            the path
	 * @throws URISyntaxException
	 */
	public void saveOntology(String path) throws URISyntaxException {
		URI pathURI = new URI(path);
		// this.api.saveOntology(path, this.encodingFormat);
		this.saveOntology(pathURI);
	}

	/**
	 * Save ontology.
	 *
	 * @param uri
	 *            the uri
	 */
	public void saveOntology(URI uri) {
		this.api.saveOntology(uri, this.encodingFormat);
	}

	public void setDeprecatedBranch(URI deprecatedBranch) {
		this.deprecatedBranch = deprecatedBranch;
	}


	// public void setDeprecatedTree(URI conceptCode) {
	// this.deprecatedBranch = conceptCode;
	// }

	public void setEncodingFormat(String encodingFormat) {
		this.encodingFormat = encodingFormat;
	}

	public void setHasLiterals(boolean hasLiterals) {
		this.hasLiterals = hasLiterals;
	}

	/**
	 * Sort concepts by key.
	 *
	 * @param hmap
	 *            the hmap
	 * @return the hash map< string, concept proxy>
	 */
	public HashMap<URI, ConceptProxy> sortConceptsByKey(
	        HashMap<URI, ConceptProxy> hmap) {
		final HashMap<URI, ConceptProxy> map = new LinkedHashMap<URI, ConceptProxy>();
		final List<URI> mapKeys = new ArrayList<URI>(hmap.keySet());
		// List mapValues = new ArrayList(hmap.values());
		final TreeSet<URI> sortedSet = new TreeSet<URI>(mapKeys);
		for (URI key : sortedSet) {
			map.put(key, hmap.get(key));
		}

		// final String[] sortedArray = (String[]) sortedSet.toArray();
		// final int size = sortedArray.length;
		// for (int i = 0; i < size; i++) {
		// // map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])),
		// // sortedArray[i]);
		// map.put(sortedArray[i], hmap.get(sortedArray[i]));
		// }
		return map;
	}

	/**
	 * Sort map by key.
	 *
	 * @param hmap
	 *            the hmap
	 * @return the hash map< string, string>
	 */
	public static HashMap<String, String> sortMapByKey(
	        HashMap<String, String> hmap) {

		final HashMap<String, String> map = new LinkedHashMap<String, String>();
		if (hmap.size() < 1) return map;
		;
		final List<String> mapKeys = new ArrayList<String>(hmap.keySet());
		// List mapValues = new ArrayList(hmap.values());
		final TreeSet<String> sortedSet = new TreeSet<String>(mapKeys);
		final Object[] sortedArray = sortedSet.toArray();
		final int size = sortedArray.length;
		for (int i = 0; i < size; i++) {
			// map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])),
			// sortedArray[i]);
			map.put(sortedArray[i].toString(), hmap.get(sortedArray[i])
			        .toString());
		}
		return map;

	}

	public static HashMap<URI, String> sortMapByURIKey(HashMap<URI, String> hmap) {

		final HashMap<URI, String> map = new LinkedHashMap<URI, String>();
		if (hmap.size() < 1) return map;
		;
		final List<URI> mapKeys = new ArrayList<URI>(hmap.keySet());
		// List mapValues = new ArrayList(hmap.values());
		final TreeSet<URI> sortedSet = new TreeSet<URI>(mapKeys);
		for (URI uri : sortedSet) {
			map.put(uri, hmap.get(uri));
		}

		// final URI[] sortedArray = (URI[]) sortedSet.toArray();
		// final int size = sortedArray.length;
		// for (int i = 0; i < size; i++) {
		// // map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])),
		// // sortedArray[i]);
		// map.put(sortedArray[i], hmap.get(sortedArray[i])
		// .toString());
		// }
		return map;

	}

	/**
	 * Sort map by value.
	 *
	 * @param hmap
	 *            the hmap
	 * @return the hash map< string, string>
	 */
	public HashMap<String, String> sortMapByValue(HashMap<String, String> hmap) {
		final HashMap<String, String> map = new LinkedHashMap<String, String>();
		final List<String> mapKeys = new ArrayList<String>(hmap.keySet());
		final List<String> mapValues = new ArrayList<String>(hmap.values());
		hmap.clear();
		final TreeSet<String> sortedSet = new TreeSet<String>(mapValues);
		final String[] sortedArray = (String[]) sortedSet.toArray();
		final int size = sortedArray.length;
		// a) Ascending sort

		for (int i = 0; i < size; i++) {
			map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])),
			        sortedArray[i]);
		}
		return map;
	}

	public void stopReasoner() {
		this.api.stopReasoner();
	}

	
//	public void addQualifierToProperty(Property property, Qualifier qual) {
//		// TODO Auto-generated method stub
//		this.api.addQualifier(property, qual);
//	}

	public void convertToCode() {
		// check if this is already byCode
		Vector<ConceptProxy> roots = this.getRootConcepts();
		boolean isByCode = false;
		for (ConceptProxy rootConcept : roots) {
			String codeTest = rootConcept.getCode();
			if (codeTest.startsWith("C") && codeTest.length() < 8) {
				// This is likely a code
				String numberTest = codeTest.substring(1, codeTest.length());
				try {
					Integer.parseInt(numberTest);
					isByCode = true;
				} catch (Exception e) {
					// is not by code
					isByCode = false;
					break;
				}
			} else {
				// Not by code
				isByCode = false;
				break;
			}
		}

		if (isByCode) return;

		// if not, iterate through all concepts to get the code property and
		// load that into a byCode concept map.
		HashMap<URI, ConceptProxy> byCodeConceptMap = new HashMap<URI, ConceptProxy>();
		HashMap<URI, URI> nameToCodeMap = new HashMap<URI, URI>();
		for (URI key : this.getAllConcepts().keySet()) {
			ConceptProxy concept = this.getAllConcepts().get(key);
			String namespace = concept.getNamespace();
			String code = concept.getProperty("code").getValue();
			URI newURI = createURI(code, namespace);
			nameToCodeMap.put(key, newURI);
			concept.changeURI(newURI);
			byCodeConceptMap.put(newURI, concept);
		}

		// The go though the concepts again looking for all axioms with a
		// subject or target and swap in the byCode.

		// We also need to go through all the property declarations and
		// instances and change those to byCode. Will need to use the annotated
		// properties.
	}

//	public void convertComplexProperties() {
//		// Go through and find complex properties - FULL-SYN, DEFINITION,
//		// GO_Annotation. Maps_To.
//		// Convert those to annotated properties.
//	}
//
//	public void convertToAxioms() {
//		this.api.convertToAxiom();
//
//	}

	public void removeQualifier(String prop, String qual) {
		// TODO Auto-generated method stub
		IRI property = IRI.create(prop);
		IRI qualifier = IRI.create(qual);
		this.api.removeQualifier(property, qualifier);
		this.refreshOntology();
	}
	
	public int getAxiomCount(){
		return this.api.getAxiomCount();
	}
	
	public int getGciCount(){
		return this.api.getGciCount();
	}

	public int getHiddenGciCount(){
		return this.api.getHiddenGciCount();
	}
	
	public int getMultiInheritanceCount(){
		return this.api.getMultiParentCount();
	}
	
	public String getDLExpressivity(){
		return this.api.getDLExpressivity();
	}

	public int getReferencedIndividualsCount() { return this.api.getReferencedIndividualsCount();}

}
