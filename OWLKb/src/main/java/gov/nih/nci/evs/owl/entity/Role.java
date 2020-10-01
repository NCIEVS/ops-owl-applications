/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl.entity RoleDef.java Jun 12, 2009
 *
 */
/**
 * <!-- LICENSE_TEXT_START --> The OWLKb Copyright 2009 Science Applications
 * International Corporation (SAIC) Copyright Notice. The software subject to
 * this notice and license includes both human readable source code form and
 * machine readable, binary, object code form (the EVSAPI Software). The EVSAPI
 * Software was developed in conjunction with the National Cancer Institute
 * (NCI) by NCI employees and employees of SAIC. To the extent government
 * employees are authors, any rights in such works shall be subject to Title 17
 * of the United States Code, section 105. This OWLKb Software License (the
 * License) is between NCI and You. You (or Your) shall mean a person or an
 * entity, and all other entities that control, are controlled by, or are under
 * common control with the entity. Control for purposes of this definition means
 * (i) the direct or indirect power to cause the direction or management of such
 * entity, whether by contract or otherwise, or (ii) ownership of fifty percent
 * (50%) or more of the outstanding shares, or (iii) beneficial ownership of
 * such entity. This License is granted provided that You agree to the
 * conditions described below. NCI grants You a non-exclusive, worldwide,
 * perpetual, fully-paid-up, no-charge, irrevocable, transferable and
 * royalty-free right and license in its rights in the OWLKb Software to (i)
 * use, install, access, operate, execute, copy, modify, translate, market,
 * publicly display, publicly perform, and prepare derivative works of the
 * EVSAPI Software; (ii) distribute and have distributed to and by third parties
 * the EVSAPI Software and any modifications and derivative works thereof; and
 * (iii) sublicense the foregoing rights set out in (i) and (ii) to third
 * parties, including the right to license such rights to further third parties.
 * For sake of clarity, and not by way of limitation, NCI shall have no right of
 * accounting or right of payment from You or Your sublicensees for the rights
 * granted under this License. This License is granted at no charge to You. 1.
 * Your redistributions of the source code for the Software must retain the
 * above copyright notice, this list of conditions and the disclaimer and
 * limitation of liability of Article 6, below. Your redistributions in object
 * code form must reproduce the above copyright notice, this list of conditions
 * and the disclaimer of Article 6 in the documentation and/or other materials
 * provided with the distribution, if any. 2. Your end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgment: This product includes software developed by SAIC and the
 * National Cancer Institute. If You do not include such end-user documentation,
 * You shall include this acknowledgment in the Software itself, wherever such
 * third-party acknowledgments normally appear. 3. You may not use the names
 * "The National Cancer Institute", "NCI" Science Applications International
 * Corporation and "SAIC" to endorse or promote products derived from this
 * Software. This License does not authorize You to use any trademarks, service
 * marks, trade names, logos or product names of either NCI or SAIC, except as
 * required to comply with the terms of this License. 4. For sake of clarity,
 * and not by way of limitation, You may incorporate this Software into Your
 * proprietary programs and into any third party proprietary programs. However,
 * if You incorporate the Software into third party proprietary programs, You
 * agree that You are solely responsible for obtaining any permission from such
 * third parties required to incorporate the Software into such third party
 * proprietary programs and for informing Your sublicensees, including without
 * limitation Your end-users, of their obligation to secure any required
 * permissions from such third parties before incorporating the Software into
 * such third party proprietary software programs. In the event that You fail to
 * obtain such permissions, You agree to indemnify NCI for any claims against
 * NCI by such third parties, except to the extent prohibited by law, resulting
 * from Your failure to obtain such permissions. 5. For sake of clarity, and not
 * by way of limitation, You may add Your own copyright statement to Your
 * modifications and to the derivative works, and You may provide additional or
 * different license terms and conditions in Your sublicenses of modifications
 * of the Software, or any derivative works of the Software as a whole, provided
 * Your use, reproduction, and distribution of the Work otherwise complies with
 * the conditions stated in this License. 6. THIS SOFTWARE IS PROVIDED "AS IS,"
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A
 * PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER
 * INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * <!-- LICENSE_TEXT_END -->
 */
package gov.nih.nci.evs.owl.entity;

import gov.nih.nci.evs.owl.exceptions.RoleException;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.owl.proxy.RoleProxy;

/**
 * @author safrant
 *
 */
public class Role extends Relationship {

	/**
	 * Type of class the qualifier modifies: Property, RoleDef or Association.
	 */
	public static enum RoleModifier {

		/** The SOME. */
		SOME,

		/** The ALL. */
		ALL,

		/** The POSS. */
		POSS,

		/** The ATLEAST. */
		ATLEAST,

		/** The ATMOST. */
		ATMOST,

		/** The THE. */
		THE;

		/**
		 * Equals.
		 *
		 * @param mod
		 *            the mod
		 *
		 * @return boolean true if RoleModifier are equal, false otherwise
		 */
		// public boolean equals(RoleModifier mod) {
		// if (this.toString().equals(mod.toString()))
		// return true;
		// return false;
		//
		// }

		/**
		 * Determines if the input string is a valid modifier If match found,
		 * return RoleModifier.
		 *
		 * @param input
		 *            the input
		 *
		 * @return the role modifier
		 */
		public RoleModifier getRoleModifier(String input) {
			for (RoleModifier r : RoleModifier.values()) {
				if (input.compareTo(r.toString()) == 0) return r;
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			// make all lower case
			String s = super.toString().toLowerCase();
			return s;
		}
	}

	/** The role modifier. */
	private RoleModifier roleModifier;

	/** The role def. */
	private RoleProxy roleDef;

	/**
	 * Instantiates a new relationship.
	 *
	 * @param roleDef2
	 *            the relation
	 * @param concept
	 *            the source
	 * @param target
	 *            the target
	 */
	public Role(RoleProxy roleDef2, ConceptProxy concept, ConceptProxy target) {
		super(roleDef2.getRoleDef(), concept, target);
		this.roleModifier = RoleModifier.SOME;
		this.setRoleDef(roleDef2);
	}

	// /**
	// * Instantiates a new relationship.
	// *
	// * @param relation
	// * the relation
	// * @param source
	// * the source
	// * @param target
	// * the target
	// */
	// public Role(RoleDef relation, ConceptProxy source, ConceptProxy target) {
	// super(relation, source, target);
	// this.roleModifier = RoleModifier.SOME;
	// this.setRoleDef(relation);
	// }

	/**
	 * Instantiates a new relationship.
	 *
	 * @param relation
	 *            the relation
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param mod
	 *            the mod
	 */
	public Role(RoleProxy relation, ConceptProxy source, ConceptProxy target,
	        RoleModifier mod) {
		super(relation.getRoleDef(), source, target);
		this.roleModifier = mod;
		this.setRoleDef(relation);
	}

	/**
	 * @return the roleDef
	 */
	public RoleProxy getRoleDef() {
		return this.roleDef;
	}

	/**
	 * Gets the role modifier.
	 *
	 * @return the role modifier
	 */
	public RoleModifier getRoleModifier() {
		return this.roleModifier;
	}

	/**
	 * @param roleDef1
	 *            the roleDef to set
	 */
	public void setRoleDef(RoleProxy roleDef1) {
		this.roleDef = roleDef1;
	}

	/**
	 * Sets the role modifier.
	 *
	 * @param roleModifier1
	 *            the role modifier
	 *
	 * @throws RoleException
	 *             the role exception
	 */
	public void setRoleModifier(RoleModifier roleModifier1)
	        throws RoleException {
		try {
			this.roleModifier = roleModifier1;
		} catch (Exception e) {
			throw new RoleException(e.getMessage());
		}
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) return true;
		if (!(that instanceof Role)) return false;
		Role thatRole = (Role) that;
		return this.isEqualRole(thatRole);
	}

	private boolean isEqualRole(Role that) {
		return (this.getCode() == null ? that.getCode() == null : this
		        .getCode().equals(that.getCode())
		        && (this.getSourceCode() == null ? that.getSourceCode() == null
		                : this.getSourceCode().equals(that.getSourceCode()))
		        && (this.getRoleModifier().toString() == null ? that
		                .getRoleModifier().toString() == null : this
		                .getRoleModifier().toString()
		                .equals(that.getRoleModifier().toString()))
		        && (this.getRelAndTarget().equals(that.getRelAndTarget()))

				);
	}

	@Override
	public int hashCode() {
		int result = this.getCode().toString().length();
		result = hash(result, this.getName());
		result = hash(result, this.getSourceCode());
		result = hash(result, this.getRoleModifier().toString());
		result = hash(result, this.getRelAndTarget());

		return result;

	}

	public static int hash(int aSeed, String hashableString) {
		int result = aSeed;
		if (hashableString == null) {
			result = hash(result, 0);
		} else {

			result = hash(result, hashableString.hashCode());
		}
		return result;
	}

	private static int hash(int aSeed, int hashableint) {
		return (11 * aSeed) + hashableint;
	}

	public String printRole() {
		return this.getSourceCode() + " " + this.getRelAndTarget();
	}

}
