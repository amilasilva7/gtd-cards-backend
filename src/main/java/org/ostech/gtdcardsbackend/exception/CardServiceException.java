package org.ostech.gtdcardsbackend.exception;

public class CardServiceException extends RuntimeException {
    public CardServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
