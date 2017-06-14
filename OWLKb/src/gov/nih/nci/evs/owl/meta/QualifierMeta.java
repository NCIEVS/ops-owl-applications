/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl.meta QualifierMeta.java Apr 30, 2009
 */
package gov.nih.nci.evs.owl.meta;

import gov.nih.nci.evs.owl.exceptions.PropertyException;


import java.util.HashMap;
import java.util.Vector;

/**
 * The Class QualifierMeta.
 *
 * @author safrant QualifierMeta describes metadata about Qualifiers such as
 *         Type and Picklist
 */
public abstract class QualifierMeta {

	/**
	 * Type of class the qualifier modifies: Property, RoleDef or Association.
	 */
	public static enum QualifierType {

		/** The PROPERTY. */
		PROPERTY,
		/** The ROLE. */
		ROLE,
		/** The ASSOCIATION. */
		ASSOCIATION;

		/**
		 * Equals.
		 *
		 * @param type
		 *            the type
		 *
		 * @return boolean true if QualifierType's are equal, false otherwise
		 */
		// public boolean equals(QualifierType type) {
		// if (this.toString().equals(type.toString()))
		// return true;
		// return false;
		//
		// }

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			// only capitalize the first letter
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}
	}

	/** The P qpicklists. */
	private HashMap<String, Vector<String>> PQpicklists;

	/** The A qpicklists. */
	private HashMap<String, Vector<String>> AQpicklists;

	/** The R qpicklists. */
	private HashMap<String, Vector<String>> RQpicklists;

	/**
	 * Gets the all a qpicklists.
	 *
	 * @return all Association Qualifier picklists
	 */
	public HashMap<String, Vector<String>> getAllAQpicklists() {
		return this.AQpicklists;
	}

	/**
	 * Gets the all p qpicklists.
	 *
	 * @return all Property Qualifier picklists
	 */
	public HashMap<String, Vector<String>> getAllPQpicklists() {
		return this.PQpicklists;
	}

	/**
	 * Gets the all r qpicklists.
	 *
	 * @return all RoleDef Qualifier picklists
	 */
	public HashMap<String, Vector<String>> getAllRQpicklists() {
		return this.RQpicklists;
	}

	/**
	 * Gets the a qpicklist.
	 *
	 * @param qualName
	 *            the qual name
	 *
	 * @return Association Qualifier picklist for a single Qualifier
	 */
	public Vector<String> getAQpicklist(String qualName) {
		Vector<String> picklist = this.PQpicklists.get(qualName);
		return picklist;
	}

	/**
	 * Gets the pick list.
	 *
	 * @param qualName
	 *            the qual name
	 * @param type
	 *            the type
	 *
	 * @return picklist - get a picklist for a single Qualifier
	 *
	 *         some Qualifiers are not free text, they come with a range of
	 *         values to choose from
	 */
	public Vector<String> getPickList(String qualName, QualifierType type) {

		if (type == QualifierType.PROPERTY) return this.getPQpicklist(qualName);
		if (type == QualifierType.ASSOCIATION) return this
		        .getAQpicklist(qualName);
		return this.getRQpicklist(qualName);
	}

	/**
	 * Gets the p qpicklist.
	 *
	 * @param qualName
	 *            the qual name
	 *
	 * @return Property Qualifier picklist for a single Qualifier
	 */
	public Vector<String> getPQpicklist(String qualName) {
		Vector<String> picklist = this.PQpicklists.get(qualName);
		return picklist;
	}

	/**
	 * Gets the r qpicklist.
	 *
	 * @param qualName
	 *            the qual name
	 *
	 * @return RoleDef Qualifier picklist for a single Qualifier
	 */
	public Vector<String> getRQpicklist(String qualName) {
		Vector<String> picklist = this.PQpicklists.get(qualName);
		return picklist;
	}

	/**
	 * Set all Association picklists.
	 *
	 * @param qpicklists
	 *            the qpicklists
	 */
	public void setAQpicklists(HashMap<String, Vector<String>> qpicklists) {
		this.AQpicklists = qpicklists;
	}

	/**
	 * Set picklist for a single Qualifier.
	 *
	 * @param pickList
	 *            the pick list
	 * @param qualName
	 *            the qual name
	 * @param type
	 *            the type
	 *
	 * @throws QualifierException
	 *             the qualifier exception
	 */
	public void setPickList(Vector<String> pickList, String qualName,
	        QualifierType type) throws PropertyException {
		try {
			Vector<String> qPicklist = new Vector<String>();
			int size = pickList.size();
			for (int i = 0; i < size; i++) {
				qPicklist.add(pickList.get(i));
			}

			if (type == QualifierType.PROPERTY) {
				if (!this.PQpicklists.containsKey(qualName)) {
					this.PQpicklists.put(qualName, qPicklist);
				}
			} else if (type == QualifierType.ASSOCIATION) {
				if (!this.AQpicklists.containsKey(qualName)) {
					this.AQpicklists.put(qualName, qPicklist);
				}
			} else {
				if (!this.RQpicklists.containsKey(qualName)) {
					this.RQpicklists.put(qualName, qPicklist);
				}
			}

		} catch (Exception e) {
			throw new PropertyException(e.getMessage());
		}

	}

	/**
	 * Set all Property picklists.
	 *
	 * @param qpicklists
	 *            the qpicklists
	 */
	public void setPQpicklists(HashMap<String, Vector<String>> qpicklists) {
		this.PQpicklists = qpicklists;
	}

	/**
	 * Set all RoleDef picklists.
	 *
	 * @param qpicklists
	 *            the qpicklists
	 */
	public void setRQpicklists(HashMap<String, Vector<String>> qpicklists) {
		this.RQpicklists = qpicklists;
	}

}
