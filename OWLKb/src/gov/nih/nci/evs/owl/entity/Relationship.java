/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl.entity RelationshipController.java May 4, 2009
 */
package gov.nih.nci.evs.owl.entity;

import gov.nih.nci.evs.owl.meta.RelationDef;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.net.URI;

/**
 * The Class Relationship.
 *
 * @author safrant
 *
 *         A Relationship is a combination of a relation, its source, and its
 *         target
 */
public class Relationship implements Comparable<Relationship> {

	/** The relation. */
	private final RelationDef relation;

	/** The source. */
	private final Concept source;

	/** The target. */
	private final Concept target;

	/**
	 * Instantiates a new relationship.
	 *
	 * @param relation1
	 *            the relation
	 * @param source1
	 *            the source
	 * @param target1
	 *            the target
	 */
	public Relationship(RelationDef relation1, Concept source1, Concept target1) {
		// System.out.println("Using Concept Relation constructor");
		this.relation = relation1;
		this.source = source1;
		this.target = target1;
	}

	/**
	 * Instantiates a new relationship.
	 *
	 * @param relation1
	 *            the relation
	 * @param source1
	 *            the source
	 * @param target1
	 *            the target
	 */
	public Relationship(RelationDef relation1, ConceptProxy source1,
			ConceptProxy target1) {
		// System.out.println("Using ConceptProxy Relation constructor");
		this.relation = relation1;
		this.source = source1.getConcept();
		this.target = target1.getConcept();
	}

	/**
	 * Compare.
	 *
	 * @param rel
	 *            the rel
	 *
	 * @return the int
	 */
	public int compare(Relationship rel) {
		return 1;
		// TODO code stuff
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Relationship o) {
		String myRelationship = this.getRelation().getCode() + " "
				+ this.getSource().getCode() + " " + this.getTarget().getCode();
		String foreignRelationship = o.getRelation().getCode() + " "
				+ o.getSource().getCode() + " " + o.getTarget().getCode();
		return myRelationship.compareTo(foreignRelationship);
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public RelationDef getRelation() {
		return this.relation;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public Concept getSource() {
		return this.source;
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public Concept getTarget() {
		return this.target;
	}

	/**
	 * @return
	 */
	public String getRelAndTarget() {
		String out = this.getRelation().getCode() + " "

		+ this.getTarget().getCode();
		return out;
	}

	/**
	 * Checks if is equal.
	 *
	 * @param rel
	 *            the rel
	 *
	 * @return true, if is equal
	 */
	public boolean isEqual(Relationship rel) {
		if (this.getRelation().getCode().equals(rel.getRelation().getCode())) {
			if (this.getSource().getCode().equals(rel.getSource().getCode())) {
				if (this.getTarget().getCode()
						.equals(rel.getTarget().getCode())) return true;
			}
		}
		return false;
	}

	public String getName() {
		return this.getRelation().getName();
	}

	public URI getCode() {
		return this.getRelation().getCode();
	}

	public String getSourceCode() {
		return this.source.getCode();
	}

	public String getSourceName() {
		return this.source.getName();
	}

	public String getTargetCode() {
		return this.target.getCode();
	}

	public String getTargetName() {
		return this.target.getName();
	}

}
