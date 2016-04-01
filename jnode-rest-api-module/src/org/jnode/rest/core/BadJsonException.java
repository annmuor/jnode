package org.jnode.rest.core;

public class BadJsonException extends Exception {
    public BadJsonException() {
        super();
    }

    public BadJsonException(String message) {
        super(message);
    }

    public BadJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadJsonException(Throwable cause) {
        super(cause);
    }
}
