/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl
 * Association.java
 * Apr 22, 2009
 */
package gov.nih.nci.evs.owl.meta;

import gov.nih.nci.evs.owl.entity.Qualifier;

import java.net.URI;
import java.util.Vector;

/**
 * The Class Association.
 *
 * @author safrant
 */
public class AssociationDef extends RelationDef {

	/** The qualifiers. */
	Vector<Qualifier> qualifiers;

	/**
	 * The Constructor.
	 *
	 * @param code
	 *            the code
	 */
	public AssociationDef(URI code) {
		super(code);
	}

	/**
	 * Instantiates a new association.
	 *
	 * @param code
	 *            the code
	 * @param name
	 *            the name
	 */
	public AssociationDef(URI code, String name) {
		super(code, name);
	}

	/**
	 * get the Qualfiers
	 *
	 * @return the qualifiers
	 */
	public Vector<Qualifier> getQualifiers() {
		return this.qualifiers;
	}

	/**
	 * Set the Qualifiers
	 *
	 * @param inQual
	 */
	public void setQualifiers(Vector<Qualifier> inQual) {
		this.qualifiers = inQual;
	}
}
