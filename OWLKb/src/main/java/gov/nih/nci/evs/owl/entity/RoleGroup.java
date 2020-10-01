/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl.entity
 * RoleGroup.java
 * May 4, 2009
 */
package gov.nih.nci.evs.owl.entity;

import java.net.URI;
import java.util.HashMap;

/**
 * The Class RoleGroup.
 *
 * @author safrant
 */
public class RoleGroup {

	/** The group name. */
	private String groupName;

	/** The role defs. */
	private HashMap<URI, Relationship> roleDefs;

	/**
	 * Instantiates a new role group.
	 */
	public RoleGroup() {
		super();
	}

	/**
	 * The Constructor.
	 *
	 * @param groupName1
	 *            the group name
	 * @param roleDefs1
	 *            the role defs
	 */
	public RoleGroup(String groupName1, HashMap<URI, Relationship> roleDefs1) {
		super();
		this.groupName = groupName1;
		this.roleDefs = roleDefs1;
	}

	/**
	 * Adds the role.
	 *
	 * @param roleDef
	 *            the role def
	 */
	public void addRole(Relationship roleDef) {
		if (!this.roleDefs.containsKey(roleDef.getRelation().getCode())) {
			this.roleDefs.put(roleDef.getRelation().getCode(), roleDef);
		}
	}

	/**
	 * Gets the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * Gets the roles.
	 *
	 * @return the roles
	 */
	public HashMap<URI, Relationship> getRoles() {
		return this.roleDefs;
	}

	/**
	 * Removes the role.
	 *
	 * @param roleDef
	 *            the role def
	 */
	public void removeRole(Relationship roleDef) {
		if (this.roleDefs.containsKey(roleDef.getRelation().getCode())) {
			this.roleDefs.remove(roleDef.getRelation().getCode());
		}
	}

	/**
	 * Sets the group name.
	 *
	 * @param groupName1
	 *            the new group name
	 */
	public void setGroupName(String groupName1) {
		this.groupName = groupName1;
	}

	/**
	 * Sets the roles.
	 *
	 * @param roleDefs1
	 *            the role defs
	 */
	public void setRoles(HashMap<URI, Relationship> roleDefs1) {
		this.roleDefs = roleDefs1;
	}

}
