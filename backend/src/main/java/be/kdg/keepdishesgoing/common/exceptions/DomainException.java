package be.kdg.keepdishesgoing.common.exceptions;

/**
 * Base exception for all domain-level exceptions.
 * These exceptions represent violations of business rules.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
