/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl
 * ConceptInterface.java
 * May 13, 2009
 */
package gov.nih.nci.evs.owl.interfaces;

import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.entity.RoleGroup;

import java.net.URI;
import java.util.Vector;

/**
 * The Interface ConceptInterface.
 *
 * @author safrant
 */
public interface ConceptInterface {

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode();

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName();

	public String getNamespace();

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Vector<Property> getProperties();

	/**
	 * Gets the role groups.
	 *
	 * @return the role groups
	 */
	public Vector<RoleGroup> getRoleGroups();

	/**
	 * Gets the roles.
	 *
	 * @return the roles
	 */
	public Vector<Role> getRoles();

	/**
	 *
	 * @return whether the concept is marked as deprecated
	 */
	public boolean isDeprecated();

	/**
	 *
	 * @return whether the concept is in the Retired branch
	 */
	public boolean isRetired();

	public Vector<Association> getAssociations();

	public Vector<Property> getProperties(String propId);

	public Property getProperty(String propId);

	public void addProperty(Property newProp);

	// public void addProperty(String namespace, Property newProp);

	public void addRole(Role role);

	public Vector<URI> getChildren();

	public Vector<URI> getAllDescendantCodes();

	public Vector<URI> getParentCodes();

	public Vector<URI> getAllAncestorCodes();

	public boolean isDefined();

	public boolean isAnonymous();

	public Vector<Role> getRolesAndEquivalents();

	public Vector<Role> getEquivalentClasses();

	public Vector<Association> getAssociations(String assocID);

	public Vector<Role> getRoles(String roleID);

	void addParent(URI parentCode);

	public Vector<URI> getParentURI();

	public URI getURI();

	public void changeURI(URI newURI);
	
	public void unloadProperties();

}
