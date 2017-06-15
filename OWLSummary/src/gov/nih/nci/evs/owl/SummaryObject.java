/*
 *
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;


/**
 * The Class SummaryObject.
 */
public class SummaryObject {
	/** The ontology namespace. */
	private final String ontologyNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";


	/** The ontology. */
	private OWLKb owlApi = null;

	/** The property map. */
	private HashMap<URI, String> propertyMap = new HashMap<URI, String>();

	/** The reverse property map. */
	private final HashMap<String, URI> reversePropertyMap = new HashMap<String, URI>();

	/** The role map. */
	private HashMap<URI, String> roleMap = new HashMap<URI, String>();

	/** The reverse role map. */
	private final HashMap<String, URI> reverseRoleMap = new HashMap<String, URI>();

	/** The assoc map. */
	private HashMap<URI, String> assocMap = new HashMap<URI, String>();

	/** The reverse assoc map. */
	private final HashMap<String, URI> reverseAssocMap = new HashMap<String, URI>();

	/** The root map. */
	private final HashMap<URI, RootConcept> rootMap = new HashMap<URI, RootConcept>();

	/** The namespaces. */
	private Set<String> namespaces;

	/** The concept count. */
	private final Integer conceptCount;
	
	/** The number of concepts in each kind **/
	HashMap<URI, Integer> conceptCountsPerKind = new HashMap<URI, Integer>();

	/** Vector of concept codes per kind **/
	HashMap<URI, Vector<URI>> conceptsPerKind = new HashMap<URI, Vector<URI>>();
	
	/**
	 * Instantiates a new summary object.
	 * 
	 * @param uri
	 *            the uri
	 */
	public SummaryObject(final URI uri) {
		owlApi = new OWLKb(uri, ontologyNamespace);

		loadPropertyClasses();
		conceptCount = new Integer(owlApi.getAllConcepts().size());
		System.out.println("Number of concepts: " + conceptCount.toString());

	}

	/**
	 * Gets the all concept codes.
	 * 
	 * @return the all concept codes
	 */
	public final Vector<URI> getAllConceptCodes() {

		owlApi.getAllConcepts();
		Set<URI> keySet = owlApi.getAllConcepts().keySet();
		Vector<URI> keySetVector = new Vector<URI>(keySet);
		return keySetVector;
	}

	/**
	 * Gets the associations per kind.
	 * 
	 * @return the associations per kind
	 */
	public final HashMap<URI, Vector<Association>> getAssociationsPerKind() {
		// loop through root vector. Build Map and return it.
		// Then set Map to null for garbage collection
		HashMap<URI, Vector<Association>> associationsPerKind = new HashMap<URI, Vector<Association>>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			associationsPerKind.put(key, root.getAssociations());
		}
		return associationsPerKind;
	}

	public final HashMap<URI, HashMap<URI, Integer>> getAssociationCountPerKind() {
		// loop through root vector. Build Map and return it.
		// Then set Map to null for garbage collection
		HashMap<URI, HashMap<URI, Integer>> associationsPerKind = new HashMap<URI, HashMap<URI, Integer>>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			associationsPerKind.put(key, root.getDescendantAssociationsCount());
		}
		return associationsPerKind;
	}

	/**
	 * Gets the assoc map.
	 * 
	 * @return the assoc map
	 */
	public final HashMap<URI, String> getAssocMap() {
		return assocMap;
	}

	/**
	 * Gets the concept and def.
	 * 
	 * @return the concept and def
	 */
	public final HashMap<URI, String> getConceptAndDef() {
		// process each class for it's Definition
		// only needed for Details

		HashMap<URI, String> conceptAndDef = new HashMap<URI, String>();

		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			Vector<URI> descendants = root.getDescendantMap();
			// Set<String> rootSet = descendants.keySet();
			Iterator<URI> rootIter = descendants.iterator();
			while (rootIter.hasNext()) {
				URI rootKey = rootIter.next();
				ConceptProxy concept = owlApi.getConcept(rootKey);

				String definition = getDefinition(concept);
				// String definition = getPropertyValue(cls, "DEFINITION");
				conceptAndDef.put(rootKey, definition);
			}
		}

		return conceptAndDef;
	}

	/**
	 * Gets the concept count.
	 * 
	 * @return the concept count
	 */
	public final Integer getConceptCount() {
		return conceptCount;
	}

	public Vector<URI> getConceptParents(URI conceptCode) {
		ConceptProxy concept = owlApi.getConcept(conceptCode);
		return concept.getParentCodes();
	}

	/**
	 * Gets the concept counts per kind.
	 * 
	 * @return the concept counts per kind
	 */
	public final HashMap<URI, Integer> getConceptCountsPerKind() {
		if (conceptCountsPerKind.size() < 1)
		{
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			conceptCountsPerKind.put(key, root.getDescendantMapSize());
//			conceptCountsPerKind.put(key, root.getAllDescendantCodes().size());
		}}
		return conceptCountsPerKind;
	}

	/**
	 * Gets the concepts and parents.
	 * 
	 * @return the concepts and parents
	 */
	// public final HashMap<String, Vector<String>> getConceptsAndParents() {
	// // process each class for it's Parent
	// // only needed for Details
	// HashMap<String, Vector<String>> conceptsAndParents = new HashMap<String,
	// Vector<String>>();
	// Set<String> set = rootMap.keySet();
	// Iterator<String> iter = set.iterator();
	// while (iter.hasNext()) {
	// String key = iter.next();
	// RootConcept root = rootMap.get(key);
	// conceptsAndParents.put(key, root.getParents());
	// }
	//
	// return conceptsAndParents;
	// }

	public final Vector<URI> getParentsForConcept(URI conceptCode) {
		ConceptProxy concept = owlApi.getConcept(conceptCode);
		return concept.getParentCodes();
	}

	/**
	 * Gets the concepts per kind.
	 * 
	 * @return the concepts per kind
	 */
	public final HashMap<URI, Vector<URI>> getConceptsPerKind() {
		if (conceptsPerKind.size()<1){

		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			Vector<URI> descendantCodes = root.getAllDescendantCodes();
			conceptsPerKind.put(key, descendantCodes);
		}}
		return conceptsPerKind;
	}

	/**
	 * Gets the defined concept counts per kind.
	 * 
	 * @return the defined concept counts per kind
	 */
	public final HashMap<URI, Integer> getDefinedConceptCountsPerKind() {
		HashMap<URI, Integer> definedConceptCountsPerKind = new HashMap<URI, Integer>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			definedConceptCountsPerKind.put(key, root.getDefinedDescendantSize());
		}

		return definedConceptCountsPerKind;
	}

	private String getDefinition(final ConceptProxy c) {

		String propertyname = "DEFINITION";

		URI propertyCode = reversePropertyMap.get(propertyname);
		Property prop = c.getProperty(propertyCode.getFragment());
		if(prop != null){
			return prop.getValue();
		} 
		return "";
	}
	

	private String getPreferredName(final ConceptProxy c) {

//		String propertyname = "PREFERRED_NAME";
		String propertyname = "Preferred_Name";

		URI propertyCode = reversePropertyMap.get(propertyname);
		
		if(propertyCode != null)
		  return c.getProperty(propertyCode.getFragment()).getValue();
		
		return c.getName();
	}

//	public String getPreferredName(String conceptCode) {
//		ConceptProxy concept = owlApi.getConcept(conceptCode);
//		return getPreferredName(concept);
//	}
	
	public String getPreferredName(URI conceptCode){
		ConceptProxy concept = owlApi.getConcept(conceptCode);
		return getPreferredName(concept);
	}

	/**
	 * Gets the namespaces.
	 * 
	 * @return the namespaces
	 */
	public final Set<String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Gets the primitive concept counts per kind.
	 * 
	 * @return the primitive concept counts per kind
	 */
	public final HashMap<URI, Integer> getPrimitiveConceptCountsPerKind() {
		HashMap<URI, Integer> primitiveConceptCountsPerKind = new HashMap<URI, Integer>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			primitiveConceptCountsPerKind.put(key, root
			        .getPrimitiveDescendants().size());
		}

		return primitiveConceptCountsPerKind;
	}

	/**
	 * Gets the properties per kind.
	 * 
	 * @return the properties per kind
	 */
	public final HashMap<URI, Vector<Property>> getPropertiesPerKind() {
		// loop through root vector. Build Map and return it.
		// Then set Map to null for garbage collection
		HashMap<URI, Vector<Property>> propertiesPerKind = new HashMap<URI, Vector<Property>>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			propertiesPerKind.put(key, root.getProperties());
		}

		return propertiesPerKind;
	}

	/**
	 * Gets the properties per kind.
	 * 
	 * @return the properties per kind
	 */
	public final HashMap<URI, HashMap<URI, Integer>> getPropertyCountPerKind() {
		// loop through root vector. Build Map and return it.
		// Then set Map to null for garbage collection
		HashMap<URI, HashMap<URI, Integer>> propertiesPerKind = new HashMap<URI, HashMap<URI, Integer>>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			propertiesPerKind.put(key, root.getDescendantPropertiesCount());
		}

		return propertiesPerKind;
	}

	/**
	 * Gets the property map.
	 * 
	 * @return the property map
	 */
	public final HashMap<URI, String> getPropertyMap() {
		return propertyMap;
	}

	/**
	 * Gets the reverse assoc map.
	 * 
	 * @return the reverse assoc map
	 */
	public final HashMap<String, URI> getReverseAssocMap() {
		return reverseAssocMap;
	}

	/**
	 * Gets the reverse property map.
	 * 
	 * @return the reverse property map
	 */
	public final HashMap<String, URI> getReversePropertyMap() {
		return reversePropertyMap;
	}

	/**
	 * Gets the reverse role map. Gets the concept and def.
	 * 
	 * @return the concept and def
	 */
	public final HashMap<URI, String> getConceptAndPreferredName() {
		// process each class for its PreferredName
		// only needed for Details

		HashMap<URI, String> conceptAndPreferredName = new HashMap<URI, String>();

		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			Vector<URI> descendants = root.getDescendantMap();
			// Set<String> rootSet = descendants.keySet();
			Iterator<URI> rootIter = descendants.iterator();
			while (rootIter.hasNext()) {
				URI rootKey = rootIter.next();
				ConceptProxy concept = owlApi.getConcept(rootKey);
				// OWLClass cls = root.getDescendantMap().get(rootKey);
				// String code = getConceptCode(cls);
				URI propertyCode = reversePropertyMap.get("Preferred_Name");
				Property preferredName = concept.getProperty(propertyCode.getFragment());
				// String preferredName = getPropertyValue(cls,
				// "Preferred_Name");
				// String definition = getPropertyValue(cls, "DEFINITION");
				conceptAndPreferredName.put(rootKey, preferredName
				        .getValue());
			}
		}

		return conceptAndPreferredName;
	}

	/**
	 * Gets the concept and def.
	 * 
	 * @return the concept and def
	 */
	public final HashMap<URI, Vector<String>> getSemanticTypes() {
		// process each class for its Semantic Types
		// only needed for Details

		HashMap<URI, Vector<String>> conceptAndSemanticTypes = new HashMap<URI, Vector<String>>();

		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			Vector<URI> descendants = root.getDescendantMap();
			// Set<String> rootSet = descendants.keySet();
			Iterator<URI> rootIter = descendants.iterator();
			while (rootIter.hasNext()) {
				URI rootKey = rootIter.next();
				ConceptProxy concept = owlApi.getConcept(rootKey);
				// OWLClass cls = root.getDescendantMap().get(rootKey);
//				String code = concept.getCode();
				URI propertyCode = reversePropertyMap.get("Semantic_Type");
				Vector<Property> semanticTypes = concept
				        .getProperties(propertyCode.getFragment());
				Vector<String> semanticTypeValues = new Vector<String>();
				for (Property prop : semanticTypes) {
					semanticTypeValues.add(prop.getValue());
				}
				conceptAndSemanticTypes.put(rootKey, semanticTypeValues);
			}
		}

		return conceptAndSemanticTypes;
	}

	/**
	 * Gets the concepts and parents.
	 * 
	 * @return the reverse role map
	 */
	public final HashMap<String, URI> getReverseRoleMap() {
		return reverseRoleMap;
	}

	/**
	 * Gets the role map.
	 * 
	 * @return the role map
	 */
	public final HashMap<URI, String> getRoleMap() {
		return roleMap;
	}

	/**
	 * Gets the roles per kind.
	 * 
	 * @return the roles per kind
	 */
	public final HashMap<URI, Vector<Role>> getRolesPerKind() {
		// loop through root vector. Build Map and return it.
		// Then set Map to null for garbage collection
		HashMap<URI, Vector<Role>> rolesPerKind = new HashMap<URI, Vector<Role>>();
		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			rolesPerKind.put(key, root.getRoles());
		}
		return rolesPerKind;
	}

	public HashMap<URI, HashMap<URI, Integer>> getRoleCountPerKind() {

		Set<URI> set = rootMap.keySet();
		Iterator<URI> iter = set.iterator();
		HashMap<URI, HashMap<URI, Integer>> rolesPerKind = new HashMap<URI, HashMap<URI, Integer>>();
		while (iter.hasNext()) {
			URI key = iter.next();
			RootConcept root = rootMap.get(key);
			HashMap<URI, Integer> roleCount = root.getDescendantRolesCount();
			rolesPerKind.put(key, roleCount);
		}
		return rolesPerKind;
	}

	/**
	 * Gets the root concept names.
	 * 
	 * @return the root concept names
	 */
	public final Set<URI> getRootConceptNames() {
		// return Set of rootConceptNames
		Set<URI> rootConceptNames = rootMap.keySet();
		return rootConceptNames;
	}

	/**
	 * Gets the root map.
	 * 
	 * @return the root map
	 */
	public final HashMap<URI, RootConcept> getRootMap() {
		return rootMap;
	}

	private void loadHeaders() {
		System.out.println("Load header properties, roles and associations");
		propertyMap = owlApi.getAllProperties();
		Set<URI> keys = propertyMap.keySet();
		for (URI key : keys) {
			String value = propertyMap.get(key);
			reversePropertyMap.put(value, key);
		}
		assocMap = owlApi.getAllAssociations();
		keys = assocMap.keySet();
		for (URI key : keys) {
			String value = assocMap.get(key);
			reverseAssocMap.put(value, key);
		}
		roleMap = owlApi.getAllRoles();
		keys = roleMap.keySet();
		for (URI key : keys) {
			String value = roleMap.get(key);
			reverseRoleMap.put(value, key);
		}
	}

	final void loadPropertyClasses() {
		loadHeaders();
		loadRootConcepts();
	}

	private void loadRootConcepts() {
		System.out
		        .println("Search vocabulary for root concepts and load descendants");

		Vector<URI> rootConceptCodes = owlApi.getRootConceptCodes();
		for (URI rootCode : rootConceptCodes) {
			RootConcept root = new RootConcept(rootCode, owlApi);
			rootMap.put(rootCode, root);
		}
		System.out.println("Finished loading root classes");
	}

	/**
	 * What kind is this.
	 * 
	 * @param conceptCode
	 *            the concept code
	 * 
	 * @return the string
	 */
//	public final URI whatKindIsThis(final URI conceptCode) {
//
////		ConceptProxy concept = owlApi.getRootConceptforConcept(conceptCode);
////		return concept.getCode();
//		
//		return owlApi.getRootConceptCodeforConcept(conceptCode);
//	}
	
	public URI whatKindIsThis(URI conceptURI){
		
		for(URI rootCode:rootMap.keySet()){
			if(rootMap.get(rootCode).isDescendant(conceptURI) )
			{
				return rootCode;
			}
		}
		return null;
		
		
	}
	
	
	public void doMetrics() {
	    //Start counting up FULL_SYNS.
	    Metrics metrics = new Metrics(owlApi);
	}
	
	public URI getRootForConcept(URI conceptCode){
		Iterator<URI> iter = this.getRootMap().keySet().iterator();
		while(iter.hasNext()){
			URI rootCode = iter.next();
			if(rootMap.get(rootCode).getDescendantMap().contains(conceptCode)){
				return rootCode;
			}
			
		} return null;
	}

}
