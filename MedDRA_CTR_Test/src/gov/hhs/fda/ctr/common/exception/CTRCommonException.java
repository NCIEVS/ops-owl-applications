package gov.hhs.fda.ctr.common.exception;

/**
 * 
 * @author Ajay Nalamala
 *
 */
public class CTRCommonException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3447728820041499533L;

	public CTRCommonException() {
	}

	public CTRCommonException(String arg0) {
		super(arg0);
	}

	public CTRCommonException(Throwable arg0) {
		super(arg0);
	}

	public CTRCommonException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
