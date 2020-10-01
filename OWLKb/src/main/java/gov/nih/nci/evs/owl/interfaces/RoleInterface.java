/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl.interfaces
 * RoleInterface.java
 * May 13, 2009
 */
package gov.nih.nci.evs.owl.interfaces;

import gov.nih.nci.evs.owl.meta.RoleDef;

import java.net.URI;
import java.util.Vector;

/**
 * The Interface RoleInterface.
 *
 * @author safrant
 */
public interface RoleInterface {

	/**
	 * Compare.
	 *
	 * @param roleDef
	 *            the role def
	 *
	 * @return the int
	 */
	public int compare(RoleDef roleDef);

	/**
	 * Checks if is hierarchical.
	 *
	 * @return true, if is hierarchical
	 */
	// public boolean isHierarchical();

	/**
	 * Sets the hierarchical.
	 *
	 * @param hier
	 *            the new hierarchical
	 */
	// public void setHierarchical(boolean hier);

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public URI getCode();

	/**
	 * Gets the domain.
	 *
	 * @return the domain
	 */
	public Vector<URI> getDomain();

	/**
	 * Gets the range.
	 *
	 * @return the range
	 */
	public Vector<URI> getRange();

	/**
	 * Checks for parent.
	 *
	 * @return true, if successful
	 */
	public boolean hasParent();

	/**
	 * Checks if is equal.
	 *
	 * @param roleDef
	 *            the role def
	 *
	 * @return true, if checks if is equal
	 */
	public boolean isEqual(RoleDef roleDef);

	/**
	 * Checks if is transitive.
	 *
	 * @return true, if is transitive
	 */
	public boolean isTransitive();

	/**
	 * Sets the domain.
	 *
	 * @param domain
	 *            the new domain
	 */
	public void setDomain(Vector<URI> domain);

	/**
	 * Sets the range.
	 *
	 * @param range
	 *            the new range
	 */
	public void setRange(Vector<URI> range);

	/**
	 * Sets the transitive.
	 *
	 * @param trans
	 *            the new transitive
	 */
	public void setTransitive(boolean trans);

	// TODO figure out how to retrieve role annotations

}
