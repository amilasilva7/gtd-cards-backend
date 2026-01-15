package org.ostech.gtdcardsbackend.exception;

public class ExternalAPIUnavailableException extends RuntimeException {
    public ExternalAPIUnavailableException(String message) {
        super(message);
    }

    public ExternalAPIUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
