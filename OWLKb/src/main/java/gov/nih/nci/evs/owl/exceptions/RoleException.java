/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb
 * gov.nih.nci.evs.owl.exceptions
 * QualifierException.java
 * Apr 30, 2009
 */
package gov.nih.nci.evs.owl.exceptions;

/**
 * Tags exceptions originating in Qualifier as Qualifier exceptions
 */
public class RoleException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;

	/**
	 * @param inMessage
	 */
	public RoleException(String inMessage) {
		this.message = inMessage;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "Exception in RoleDef: " + this.message;
	}

}
