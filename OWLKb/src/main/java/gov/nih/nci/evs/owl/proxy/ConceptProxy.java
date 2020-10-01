/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl.proxy ConceptProxy.java May 13, 2009
 */
package gov.nih.nci.evs.owl.proxy;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Concept;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.entity.RoleGroup;
import gov.nih.nci.evs.owl.interfaces.ConceptInterface;

import java.net.URI;
import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author safrant
 *
 */
public class ConceptProxy implements ConceptInterface {

	private final String conceptCode;
	private final String namespace;
	private final String name;
	private ConceptInterface ci;
	private final URI uri;
	OWLKb api;


	// public ConceptProxy(URI conceptURI, OWLKb kb) {
	// this(conceptURI, kb, kb.getDefaultNamespace());
	// // this.conceptCode = conceptCode;
	// // this.api = kb;
	// // this.namespace = kb.getDefaultNamespace();
	//
	// }
	//
	// public ConceptProxy(URI conceptURI, String name, OWLKb kb) {
	// this(conceptURI, name, kb, kb.getDefaultNamespace());
	// }

	public ConceptProxy(URI conceptURI, OWLKb kb) {
		this(conceptURI, null, kb);
		// this.conceptCode = conceptCode;
		// this.api = kb;
		// this.namespace = namespace;
		// this.label=kb.getConceptNameByCode(conceptCode);
	}

	public ConceptProxy(URI conceptURI, String name, OWLKb kb) {
		this.conceptCode = conceptURI.getFragment();
		this.api = kb;
		this.namespace = conceptURI.getScheme()
		        + conceptURI.getSchemeSpecificPart();
//		this.name = kb.getConceptNameByCode(IRI.create(conceptURI)); // TODO get
																	 // label
		this.name = name;
		this.uri = conceptURI;
	}

	private void checkConceptLoad() {
		if (this.ci == null && this.api != null) {
			this.ci = new Concept(this.uri, this.name, this.api);
			this.ci.getProperties();

		} else if (this.api == null) {
			System.out.println("api could not be instantiated.");
			System.exit(0);
		} else {
			this.ci.getProperties();

		}
	}

	/**
	 * @param secondConcept
	 * @return
	 */
	public Vector<String> diff(ConceptProxy secondConcept) {
		this.checkConceptLoad();
		if (secondConcept != null) return this.getConcept().diff(
		        secondConcept.getConcept());
		return this.getConcept().diff(null);
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptCode()
	 */
	@Override
	public String getCode() {
		this.checkConceptLoad();
		return this.conceptCode;
	}

	@Override
	public String getNamespace() {
		this.checkConceptLoad();
		return this.ci.getNamespace();
	}

	@Override
	public boolean isDeprecated() {
		this.checkConceptLoad();
		return this.ci.isDeprecated();
	}

	//
	// public ConceptInterface getConceptInterface()
	// {
	// checkConceptLoad();
	// return ci;
	// }

	/**
	 * Gets the concept.
	 *
	 * @return the concept
	 */
	public Concept getConcept() {
		this.checkConceptLoad();
		return (Concept) this.ci;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptName()
	 */
	@Override
	public String getName() {
		this.checkConceptLoad();
		return this.ci.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptProperty()
	 */
	@Override
	public Vector<Property> getProperties() {
		this.checkConceptLoad();
		return this.ci.getProperties();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptRoleGroups()
	 */
	@Override
	public Vector<RoleGroup> getRoleGroups() {
		this.checkConceptLoad();
		return this.ci.getRoleGroups();
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.ConceptInterface#getConceptRoles()
	 */
	@Override
	public Vector<Role> getRoles() {
		this.checkConceptLoad();
		return this.ci.getRoles();
	}

	@Override
	public Vector<Association> getAssociations() {
		this.checkConceptLoad();
		return this.ci.getAssociations();
	}

	@Override
	public Vector<Property> getProperties(String propId) {
		this.checkConceptLoad();
		return this.ci.getProperties(propId);
	}

	@Override
	public Property getProperty(String propId) {
		this.checkConceptLoad();
		return this.ci.getProperty(propId);
	}

	@Override
	public void addProperty(Property newProp) {
		this.checkConceptLoad();
		this.ci.addProperty(newProp);
	}

	// @Override
	// public void addProperty(String concept_namespace, Property newProp) {
	// checkConceptLoad();
	// ci.addProperty(concept_namespace, newProp);
	// }

	@Override
	public void addParent(URI parentCode) {
		this.checkConceptLoad();
		this.ci.addParent(parentCode);
	}

	@Override
	public Vector<URI> getChildren() {
		this.checkConceptLoad();
		return this.ci.getChildren();
	}

	@Override
	public boolean isDefined() {
		this.checkConceptLoad();
		return this.ci.isDefined();
	}

	@Override
	public boolean isAnonymous() {
		this.checkConceptLoad();
		return this.ci.isAnonymous();
	}

	@Override
	public Vector<URI> getAllAncestorCodes() {
		this.checkConceptLoad();
		return this.ci.getAllAncestorCodes();
	}

	@Override
	public Vector<URI> getAllDescendantCodes() {
		this.checkConceptLoad();
		return this.ci.getAllDescendantCodes();
	}

	@Override
	public Vector<URI> getParentCodes() {
		this.checkConceptLoad();
		return this.ci.getParentCodes();
	}

	public int compareTo(ConceptProxy c2) {
		this.checkConceptLoad();
		if (c2 != null) return this.getConcept().compareTo(c2.getConcept());
		return this.getConcept().compareTo(null);
	}

	@Override
	public String toString() {
		this.checkConceptLoad();
		return this.ci.getName();
	}

	@Override
	public Vector<Role> getRolesAndEquivalents() {
		this.checkConceptLoad();
		return this.ci.getRolesAndEquivalents();
	}

	@Override
	public Vector<Role> getEquivalentClasses() {
		this.checkConceptLoad();
		return this.ci.getEquivalentClasses();
	}

	@Override
	public Vector<Association> getAssociations(String assocID) {
		this.checkConceptLoad();
		return this.ci.getAssociations(assocID);
	}

	@Override
	public Vector<Role> getRoles(String roleID) {
		this.checkConceptLoad();
		return this.ci.getRoles(roleID);
	}

	@Override
	public boolean isRetired() {
		this.checkConceptLoad();
		return this.ci.isRetired();
	}

	@Override
	public void addRole(Role role) {
		// api.addRole(this.getCode(), role);
		this.checkConceptLoad();
		this.ci.addRole(role);
	}

	@Override
	public Vector<URI> getParentURI() {
		// TODO Auto-generated method stub
		this.checkConceptLoad();
		return this.ci.getParentURI();
	}

	@Override
	public URI getURI() {
		this.checkConceptLoad();
		return this.ci.getURI();
	}

	@Override
	public void changeURI(URI newURI) {
		// TODO Auto-generated method stub
		this.checkConceptLoad();
		this.ci.changeURI(newURI);
	}
	
	public void unloadProperties() {
		if(! (ci==null)){
		this.ci.unloadProperties();
		}
	}

	@Override
	public Vector<Association> getIncomingAssociations() {
		return this.ci.getIncomingAssociations();
	}

	@Override
	public Vector<Role> getIncomingRoles() {
		return this.ci.getIncomingRoles();
	}

}
