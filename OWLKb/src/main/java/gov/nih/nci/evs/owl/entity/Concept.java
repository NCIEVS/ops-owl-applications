/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl Concept.java Apr 22, 2009
 */
package gov.nih.nci.evs.owl.entity;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.interfaces.ConceptInterface;

import java.net.URI;
import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;

/**
 * The Class Concept.
 *
 * @author safrant
 */
public class Concept implements ConceptInterface, Comparable<Concept> {

	/**
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static int compare(Concept c1, Concept c2) {

		final String code1 = c1.getCode();
		final String code2 = c2.getCode();
		final int out = code1.compareTo(code2);
		return out;
	}

	/** The code. */
	private String code;
	private String namespace;

	/** The name. */
	private String name;

	private URI uri;
	/** The roles. */
	private Vector<Role> roles;
	private Vector<Role> equivalentClasses; // TODO fix this

	/** The role groups. */
	private Vector<RoleGroup> roleGroups;

	/** The properties. */
	private Vector<Property> properties;
	private Vector<Property> annotationProperties;
	private Vector<Property> datatypeProperties;
	private Vector<Association> associations;

	// whether it is listed as owl:DeprecatedClass
	private boolean deprecated;
	// whether it is in the Retired kind
	private boolean retired;
	private boolean isDefined=false;

	/** The api. */
	protected final OWLKb api;

	/**
	 * Instantiates a new concept.
	 *
	 * @param conceptCode
	 *            the concept code
	 * @param inApi
	 *            the api
	 */
	public Concept(URI conceptCode, OWLKb inApi) {
		
		this(conceptCode, inApi.getConceptNameByCode(conceptCode), inApi);
		// this.uri = conceptCode;
		// this.code = conceptCode.getFragment();
		// this.api = inApi;
		// this.namespace = conceptCode.getPath();
		// deprecated = api.isDeprecated(conceptCode);
		// retired = api.isRetired(conceptCode);
	}

	public Concept(URI conceptCode, String name, OWLKb inApi) {
		this.uri = conceptCode;
		this.code = conceptCode.getFragment();
		this.api = inApi;
		this.namespace = conceptCode.getScheme()
		        + conceptCode.getSchemeSpecificPart();
		this.name = name;
		this.deprecated = this.api.isDeprecated(conceptCode);
		this.retired = this.api.isRetired(conceptCode);
		this.isDefined = this.api.getIsDefined(conceptCode);
	}

	/**
	 * @param c2
	 * @return
	 */
	@Override
	public int compareTo(Concept c2) {
		return compare(this, c2);
	}

	private Vector<String> conceptSummary() {
		final Vector<String> diff = new Vector<String>();
		diff.add("Code: " + this.code);
		// change in immediate parent
		final Vector<URI> myParents = this.getParentCodes();
		for (final URI myParent : myParents) {
			diff.add("        Defining Concept:     " + myParent);
		}
		final Vector<Property> myProperties = this.getProperties();
		for (final Property myProperty : myProperties) {
			diff.add("         Property:    " + myProperty.toString());
		}
		return diff;

	}

	@Override
	public boolean isDeprecated() {
		return this.deprecated;
	}
	


	/**
	 * @param c2
	 * @return Vector of concept details, indicating changes
	 */
	public Vector<String> diff(Concept c2) {

		if (c2 == null) return this.conceptSummary();

		final Vector<String> diff = new Vector<String>();
		boolean changesmade = false;

		diff.add(" ");
		diff.add(" ");
		diff.add("-----------------------------------------------------");
		diff.add(" ");
		diff.add("Code: " + this.code);
		// System.out.println(code);
		// if (code.equals("Oxidation"))
		// {
		// System.out.println("gotit");
		// }

		// change in root node first
		// TODO how to detect change in root node

		// change in immediate parent
		final Vector<URI> myParents = this.getParentCodes();
		final Vector<URI> yourParents = c2.getParentCodes();
		boolean match = false;
		for (final URI myParent : myParents) {
			for (final URI yourParent : yourParents) {
				if (myParent.toString().equalsIgnoreCase(yourParent.toString())) {
					match = true;
				}
			}
			if (!match) {
				diff.add(">>    Defining Concept:    " + myParent);
				changesmade = true;
			} else {
				diff.add("      Defining Concept:    " + myParent);
			}
			match = false;
		}

		// change in Associations - alphabetical
		final Vector<Association> myAssociations = this.getAssociations();
		final Vector<Association> yourAssociations = c2.getAssociations();
		match = false;
		for (final Relationship myAssociation : myAssociations) {
			for (final Relationship yourAssociation : yourAssociations) {
				if (myAssociation.equals(yourAssociation)) {
					match = true;
				}
			}
			if (!match) {
				diff.add(">>   Association:        "
				        + myAssociation.getRelAndTarget());
				changesmade = true;
			} else {
				diff.add("      Association:       "
				        + myAssociation.getRelAndTarget());
				match = false;
			}
		}

		// change in properties in the following order
		// Preferred_Name, Semantic_Type, Full-Syn, Definition, Alt-Definition
		// NCI_Meta_Cui, UMLS_CUI, other properties alphabetical.
		// build it so there is no error if some of these properties are
		// missing.
		// Should be able to run this diff on any generic OWL file
		final Vector<Property> myProperties = this.getProperties();
		final Vector<Property> yourProperties = c2.getProperties();
		match = false;
		for (final Property myProperty : myProperties) {
			for (final Property yourProperty : yourProperties) {
				if (myProperty.equals(yourProperty)) {
					match = true;
				}
			}
			if (!match) {
				diff.add(">>    Property:      " + myProperty.toString());
				changesmade = true;
			} else {
				diff.add("      Property:      " + myProperty.toString());
			}
			match = false;
		}

		// change in Roles - alphabetical (These may be subsumed in parents)

		// Switch now and compare the other way.
		// Parents
		diff.add(" ");
		diff.add(" ");
		for (final URI yourParent : yourParents) {
			for (final URI myParent : myParents) {
				if (yourParent.toString().equalsIgnoreCase(myParent.toString())) {
					match = true;
				}
			}
			if (!match) {
				diff.add("<<    Defining Concept:    " + yourParent);
				changesmade = true;
			} else {
				diff.add("      Defining Concept:     " + yourParent);
			}
			match = false;
		}

		// change in Associations - alphabetical
		match = false;
		for (final Relationship yourAssociation : yourAssociations) {
			for (final Relationship myAssociation : myAssociations) {
				if (myAssociation.equals(yourAssociation)) {
					match = true;
				}
			}
			if (!match) {
				diff.add("<<   Association:        "
				        + yourAssociation.getRelAndTarget());
				changesmade = true;
			} else {
				diff.add("      Association:         "
				        + yourAssociation.getRelAndTarget());
				match = false;
			}
		}

		// Properties
		match = false;
		for (final Property yourProperty : yourProperties) {
			for (final Property myProperty : myProperties) {
				if (yourProperty.equals(myProperty)) {
					match = true;
				}
			}
			if (!match) {
				diff.add("<<    Property:      " + yourProperty.toString());
				changesmade = true;
			} else {
				diff.add("      Property:      " + yourProperty.toString());
			}
			match = false;
		}

		if (changesmade) return diff;
		return new Vector<String>();
	}

	/**
	 * @return
	 */
	@Override
	public Vector<Association> getAssociations() {
		if (this.associations == null) {
			this.loadAssociations();
		}
		return this.associations;
	}

	@Override
	public Vector<Association> getAssociations(String assocID) {
		if (this.associations == null) {
			this.loadAssociations();
		}

		final Vector<Association> selectRoles = new Vector<Association>();
		for (final Association assoc : this.associations) {
			if (assocID.equals(assoc.getCode())) {
				selectRoles.add(assoc);
			}
		}

		return selectRoles;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptCode()
	 */
	@Override
	public String getCode() {
		return this.code;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptName()
	 */
	@Override
	public String getName() {
		if (this.name == null) {
			this.name = this.api.getConceptNameByCode(this.getURI());
		}
		return this.name;
	}

	/**
	 * @return Vector of parent codes for concept
	 */
	@Override
	public Vector<URI> getParentCodes() {
		final Vector<URI> parents = this.api.getParentCodesForConcept(this
		        .getURI());
		return parents;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptRoleGroups()
	 */
//	@Override
	public Vector<RoleGroup> getRoleGroups() {
		if (this.roleGroups == null) {
			this.loadRoleGroups();
		}
		return this.roleGroups;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptRoles()
	 */
	@Override
	public Vector<Role> getRoles() {
		if (this.roles == null) {
			this.loadRoles();
		}
		return this.roles;
	}

	@Override
	public Vector<Role> getRoles(String roleID) {
		if (this.roles == null) {
			this.loadRoles();
		}

		final Vector<Role> selectRoles = new Vector<Role>();
		for (final Role role : this.roles) {
			if (roleID.equals(role.getCode())) {
				selectRoles.add(role);
			}
		}

		return selectRoles;
	}

	@Override
	public Vector<Role> getEquivalentClasses() {
		if (this.equivalentClasses == null) {
			this.loadRoles();
		}
		return this.equivalentClasses;
	}

	@Override
	public Vector<Role> getRolesAndEquivalents() {
		final Vector<Role> relates = new Vector<Role>();
		relates.addAll(this.getRoles());
		relates.addAll(this.getEquivalentClasses());
		return relates;
	}

	private void loadAssociations() {
		this.associations = this.api.getAssociationsForSource(this.uri);
//		this.associations = new Vector<Association>();
	}

	/**
	 * Load properties.
	 */
	private void loadProperties() {
		this.properties = this.api.getDataPropertiesForConcept(this.uri);
		
		for(Property prop:this.api.getAnnotationPropertiesForConcept(this
			        .getURI())){
			this.properties.add(prop);
		}
//			this.properties = this.api.getAnnotationPropertiesForConcept(this
//			        .getURI());
//		}

	}

	// private void loadAnnotationProperties() {
	// this.annotationProperties = this.api
	// .getAnnotationPropertiesForConcept(this.code);
	// }
//
	/**
	 * Load role groups.
	 * This currently has no functionality	
	 */
	private void loadRoleGroups() {
		// TODO load the role groups
		// TODO this would involve equivalent classes?
	}

	/**
	 * Load roles.
	 */
	private void loadRoles() {
		this.roles = this.api.getRolesForSource(this.uri);
		this.equivalentClasses = this.api
		        .getEquivalentClassRoles(this.getURI());
	}

	/**
	 * retrieves the first instance of a given property Used for Preferred_Name
	 * or other cases where we know there is only one instance
	 *
	 * @param propId
	 * @return
	 */
	@Override
	public Property getProperty(String propId) {
		this.checkPropertiesLoad();

		for (final Property prop : this.properties) {
			if (prop.getCode().equals(propId)) return prop;
		}
		return null;
	}

	private void checkPropertiesLoad() {
		if ((this.properties == null) || (this.properties.size() < 1)) {
			this.loadProperties();
		}
	}
	
	public void unloadProperties(){
		this.properties = null;
		this.roles = null;
	}

	@Override
	public Vector<Association> getIncomingAssociations() {
		return this.api.getAssociationsForTarget(this.getURI());
	}

	@Override
	public Vector<Role> getIncomingRoles() {
		return this.api.getRolesForTarget(this.getURI());
	}

	private void checkRolesLoad() {
		if ((this.roles == null) || (this.roles.size() < 1)) {
			this.loadRoles();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptProperty()
	 */
	@Override
	public Vector<Property> getProperties() {
		this.checkPropertiesLoad();
		return this.properties;
	}

	@Override
	public Vector<Property> getProperties(String propId) {

		this.checkPropertiesLoad();
		final Vector<Property> returnProp = new Vector<Property>();
		for (final Property prop : this.properties) {
			if (prop.getCode().equals(propId)) {
				returnProp.add(prop);

			}
		}
		return returnProp;
	}

	@Override
	public void addProperty(Property newProp) {
		// if (newProp.getDomain() == null) {
		// newProp.setDomain(namespace);
		// }

		if (this.properties == null) {
			this.loadProperties();
		}
		this.properties.add(newProp);

		this.api.addAnnotationProperty(this.getURI(), newProp);
	}
	
	public void addQualifierToProperty(Property prop, Qualifier qual){
		if (this.properties == null) {
			this.loadProperties();
		}
		if (this.properties.contains(prop)){
			api.addQualifiedAnnotationPropertytoConcept(IRI.create(this.uri), prop);
			
		}
	}
	
	/*
	 * Returns true if property found and removed
	 * Returns false if property not found
	 */
	public boolean removeProperty(Property prop) {
		if (this.properties.contains(prop)){
			this.properties.remove(this.properties.indexOf(prop));
			this.api.removePropertyFromConcept(this.getURI(), prop);
			return true;
		} 
		return false;
	}

	// public void addProperty(String concept_namespace, Property newProp) {
	// if (this.properties == null) {
	// this.loadProperties();
	// }
	// this.properties.add(newProp);
	//
	// this.api.addAnnotationProperty(concept_namespace, code, newProp);
	// }

	@Override
	public Vector<URI> getChildren() {
		return this.api.getChildrenForConcept(this.getURI());
	}

	@Override
	public boolean isDefined() {
//		return this.api.getIsDefined(this.getURI());
		return this.isDefined;
	}

	@Deprecated
	//Use getAllAncestorCodes
	public Vector<URI> getAllAncestors() {
		return this.api.getAllAncestorsForConcept(this.getURI());
	}

	@Deprecated
	//Use getAllDescendantCodes
	public Vector<URI> getAllDescendants() {
		return this.api.getAllDescendantsForConcept(this.getURI());
	}

	public void addParent(Concept parent) {
		this.api.addParent(this, parent);

	}

	@Deprecated
	// Use getParentCodes
	public Vector<URI> getParents() {
		return this.api.getParentCodesForConcept(this.getURI());
	}

	@Override
	public Vector<URI> getParentURI() {
		return this.api.getParentURIForConcept(this.getURI());
	}

	@Override
	public boolean isAnonymous() {
		return this.api.getIsAnonymous(this.getURI());
	}

	@Override
	public String getNamespace() {
		return this.namespace;
	}

	@Override
	public Vector<URI> getAllDescendantCodes() {
		return this.api.getAllDescendantsForConcept(this.getURI());
	}

	@Override
	public Vector<URI> getAllAncestorCodes() {
		return this.api.getAllAncestorsForConcept(this.getURI());
	}

	@Override
	public boolean isRetired() {
		// return api.isRetired(this.code);
		return this.retired;
	}

	@Override
	public void addParent(URI parentCode) {
		this.api.addParent(this, parentCode);

	}

	public void getPathToRoot() {
		this.api.getPathToRoot(this.getURI());
	}

	@Override
	public void addRole(Role role) {

		try {
			this.checkRolesLoad();
			this.api.addRole(this.getURI(), role);

			this.roles.add(role);

			Vector<Role> debug = this.getRoles();
			if (debug.size() < 1) {
				// TODO this is an error.
				System.out.println("Role not successfully added");
			}
		} catch (Exception e) {
			String debug = "true";
		}
	}

	@Override
	public URI getURI() {

		return this.uri;
		// try {
		// URI uri = new URI(this.namespace + "#" + this.code);
		// // IRI iri = IRI.create(this.namespace + "#" + this.code);
		// return uri;
		// } catch (Exception e) {
		//
		// System.out.println("Unable to create IRI from " + namespace + "#"
		// + code);
		// return null;
		// }
	}

	@Override
	public void changeURI(URI newURI) {
		this.api.ChangeURI(this.getURI(), newURI);
		this.namespace = newURI.getPath();
		// String ns = "http" + newURI.getPath();
		// this.namespace = ns;
		this.code = newURI.getFragment();

	}
}