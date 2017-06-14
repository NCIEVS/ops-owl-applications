/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl
 * Relationship.java
 * Apr 22, 2009
 */
package gov.nih.nci.evs.owl.meta;

import gov.nih.nci.evs.owl.exceptions.RoleException;

import java.net.URI;

/**
 * The Class Relation.
 *
 * @author safrant
 */
public class RelationDef {

	/** The r code. */
	private URI rCode;

	/** The r name. */
	private String rName;

	/**
	 * Instantiates a new relation.
	 */
	public RelationDef() {
		this.rCode = null;
	}

	/**
	 * The Constructor.
	 * 
	 * @param roleCode
	 *            the role code
	 */
	public RelationDef(URI roleCode) {
		this.rCode = roleCode;
	}

	/**
	 * Instantiates a new relation.
	 * 
	 * @param uri
	 *            the rel code
	 * @param relName
	 *            the rel name
	 */
	public RelationDef(URI uri, String relName) {
		this.rCode = uri;
		this.rName = relName;
	}

	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public URI getCode() {
		return this.rCode;
	}

	/**
	 * Gets the r name.
	 * 
	 * @return the rName
	 */
	public String getName() {
		return this.rName;
	}

	/**
	 * Sets the code.
	 * 
	 * @param relationCode
	 *            the new code
	 * 
	 * @throws RoleException
	 *             the role exception
	 */
	public void setCode(URI relationCode) throws RoleException {
		try {
			this.rCode = relationCode;
		} catch (final Exception e) {
			throw new RoleException(e.getMessage());
		}
	}

	/**
	 * Sets the r name.
	 * 
	 * @param rName1
	 *            the rName to set
	 */
	public void setName(String rName1) {
		this.rName = rName1;
	}

}
