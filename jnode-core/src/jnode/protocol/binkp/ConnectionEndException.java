package jnode.protocol.binkp;

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
}
