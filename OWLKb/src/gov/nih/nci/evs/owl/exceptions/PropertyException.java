package gov.nih.nci.evs.owl.exceptions;

/**
 * Tags exceptions originating in Qualifier as Qualifier exceptions
 */
public class PropertyException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;

	/**
	 * @param inMessage
	 */
	public PropertyException(String inMessage) {
		this.message = inMessage;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "Exception in PropertyDef: " + this.message;
	}

}
