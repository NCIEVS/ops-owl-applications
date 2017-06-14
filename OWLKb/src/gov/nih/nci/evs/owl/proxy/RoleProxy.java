/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl.proxy
 * RoleProxy.java
 * May 13, 2009
 */
package gov.nih.nci.evs.owl.proxy;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.interfaces.RoleInterface;
import gov.nih.nci.evs.owl.meta.RoleDef;

import java.net.URI;
import java.util.Vector;

/**
 * The Class RoleProxy.
 *
 * @author safrant
 */
public class RoleProxy implements RoleInterface {

	/** The code. */
	private URI code;

	/** The ri. */
	private RoleInterface ri;

	OWLKb api;

	/**
	 * Instantiates a new role proxy.
	 *
	 * @param roleCode
	 *            the role code
	 * @param api1
	 */
	public RoleProxy(URI roleCode, OWLKb api1) {
		this.code = roleCode;
		this.api = api1;
	}

	/**
	 * Check role load.
	 */
	private void checkRoleLoad() {
		try {
			if (this.ri == null) {
				this.ri = new RoleDef(this.code, this.api);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * gov.nih.nci.evs.owl.interfaces.RoleInterface#compare(gov.nih.nci.evs.
	 * owl.entity.RoleDef)
	 */
	@Override
	public int compare(RoleDef roleDef) {
		this.checkRoleLoad();
		return this.ri.compare(roleDef);
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getCode()
	 */
	@Override
	public URI getCode() {
		return this.code;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getDomain()
	 */
	@Override
	public Vector<URI> getDomain() {
		this.checkRoleLoad();
		return this.ri.getDomain();
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getIsHierarchical()
	 */
	// public boolean isHierarchical() {
	// checkRoleLoad();
	// return ri.isHierarchical();
	// }

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getRange()
	 */
	@Override
	public Vector<URI> getRange() {
		this.checkRoleLoad();
		return this.ri.getRange();
	}

	/**
	 * Gets the role def.
	 *
	 * @return the role def
	 */
	public RoleDef getRoleDef() {
		this.checkRoleLoad();
		return (RoleDef) this.ri;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getHasParent()
	 */
	@Override
	public boolean hasParent() {
		this.checkRoleLoad();
		return this.ri.hasParent();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * gov.nih.nci.evs.owl.interfaces.RoleInterface#isEqual(gov.nih.nci.evs.
	 * owl.entity.RoleDef)
	 */
	@Override
	public boolean isEqual(RoleDef roleDef) {
		this.checkRoleLoad();
		return this.ri.isEqual(roleDef);
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#getIsTransitive()
	 */
	@Override
	public boolean isTransitive() {
		this.checkRoleLoad();
		return this.ri.isTransitive();
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#setDomain(boolean)
	 */
	@Override
	public void setDomain(Vector<URI> domain) {

		this.checkRoleLoad();
		this.ri.setDomain(domain);
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#setRange(boolean)
	 */
	@Override
	public void setRange(Vector<URI> range) {

		this.checkRoleLoad();
		this.ri.setRange(range);
	}

	/**
	 * Sets the hierarchichal.
	 *
	 * @param hier
	 *            the new hierarchichal
	 */
	// public void setHierarchichal(boolean hier) {
	// checkRoleLoad();
	// ri.setHierarchical(hier);
	// }

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.evs.owl.interfaces.RoleInterface#setTransitive(boolean)
	 */
	@Override
	public void setTransitive(boolean trans) {
		this.checkRoleLoad();
		this.ri.setTransitive(trans);
	}

}
