package jnode.protocol.io.exception;

/**
 * 
 * @author kreon
 * 
 */
@Deprecated
public class ProtocolException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProtocolException() {
	}

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(Throwable cause) {
		super(cause);
	}

	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

}
