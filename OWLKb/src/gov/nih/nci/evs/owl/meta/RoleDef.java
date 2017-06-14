/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl RoleDef.java Apr 22, 2009
 */
package gov.nih.nci.evs.owl.meta;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.exceptions.RoleException;
import gov.nih.nci.evs.owl.interfaces.RoleInterface;

import java.net.URI;
import java.util.Vector;

/**
 * The Class RoleDef.
 *
 * @author safrant
 */
public class RoleDef extends RelationDef implements RoleInterface {

	@Override
	public String toString() {
		return "RoleDef [transitive=" + this.transitive + ", parents="
		        + this.parents + ", range=" + this.range + ", domain="
		        + this.domain + ", getCode()=" + this.getCode()
		        + ", getName()=" + this.getName() + "]";
	}

	/** The transitive. */
	private boolean transitive;

	// /** The hierarchical. */
	// private boolean hierarchical;

	/** The parent. */
	private Vector<RoleDef> parents;

	/** The range. */
	private Vector<URI> range;

	/** The domain. */
	private Vector<URI> domain;

	OWLKb api;

	/**
	 * Instantiates a new role.
	 *
	 * @param uri
	 *            the role code
	 * @param api1
	 *
	 * @throws RoleException
	 *             the role exception
	 */
	public RoleDef(URI uri, OWLKb api1) throws RoleException {
		super(uri);
		this.api = api1;
		try {
			this.range = api1.getRangeForRole(uri);
			this.domain = api1.getDomainForRole(uri);
			this.parents = api1.getParentsForRole(uri);
			String name = api1.getRoleNameByCode(uri);
			this.setName(name);
		} catch (Exception e) {
			System.out.println("debug - role threw error during instantiation "
			        + uri);
		}
		// TODO init roleName, transitive, parent
	}

	/**
	 * Instantiates a new role.
	 *
	 * @param roleCode
	 *            the role code
	 * @param roleName
	 *            the role name
	 * @param api1
	 *
	 * @throws RoleException
	 *             the role exception
	 */
	public RoleDef(URI roleCode, String roleName, OWLKb api1)
	        throws RoleException {
		super(roleCode, roleName);
		// TODO init transitive, parent, range and domain
		this.api = api1;
		try {
			this.range = api1.getRangeForRole(roleCode);
			this.domain = api1.getDomainForRole(roleCode);
			this.parents = api1.getParentsForRole(roleCode);
		} catch (Exception e) {
			System.out.println("debug - role threw error during instantiation "
			        + roleCode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#compare()
	 */
	@Override
	public int compare(RoleDef role) {
		return this.getCode().compareTo(role.getCode());
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getDomain()
	 */
	@Override
	public Vector<URI> getDomain() {

		return this.domain;
	}

	/**
	 * Checks if is hierarchical.
	 *
	 * @return true, if is hierarchical
	 */
	// public boolean isHierarchical() {
	// return hierarchical;
	// }

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getParent()
	 */
	public Vector<RoleDef> getParents() {
		return this.parents;
	}

	/**
	 * Sets the hierarchical.
	 *
	 * @param hierarchical
	 *            the new hierarchical
	 */
	// public void setHierarchical(boolean hierarchical) {
	// this.hierarchical = hierarchical;
	// }

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getRange()
	 */
	@Override
	public Vector<URI> getRange() {

		return this.range;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getHasParent()
	 */
	@Override
	public boolean hasParent() {

		if (this.parents == null) return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#isEqual()
	 */
	@Override
	public boolean isEqual(RoleDef role) {
		if (!role.getCode().equals(super.getCode())) return false;
		if (!role.getName().equals(super.getName())) return false;
		if (role.isTransitive() != this.isTransitive()) return false;
		return true;
	}

	/**
	 * Checks if is transitive.
	 *
	 * @return true, if is transitive
	 */
	@Override
	public boolean isTransitive() {
		return this.transitive;
	}

	/**
	 * Sets the domain.
	 *
	 * @param domain1
	 *            the new domain
	 */
	@Override
	public void setDomain(Vector<URI> domain1) {
		this.domain = domain1;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            the new parent
	 */
	public void setParents(Vector<RoleDef> parents1) {
		this.parents = parents1;
	}

	/**
	 * Sets the range.
	 *
	 * @param range1
	 *            the new range
	 */
	@Override
	public void setRange(Vector<URI> range1) {
		this.range = range1;
	}

	/**
	 * Sets the transitive.
	 *
	 * @param transitive1
	 *            the new transitive
	 */
	@Override
	public void setTransitive(boolean transitive1) {
		this.transitive = transitive1;
	}

}
