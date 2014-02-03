package jnode.protocol.binkp.exceprion;

public class ConnectionEndException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConnectionEndException() {
	}

	public ConnectionEndException(String message) {
		super(message);
	}

	public ConnectionEndException(Throwable cause) {
		super(cause);
	}

	public ConnectionEndException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionEndException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
