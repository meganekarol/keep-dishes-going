package be.kdg.keepdishesgoing.common.exceptions;

/**
 * Exception thrown when an aggregate is in an invalid state for a requested operation.
 * For example: trying to publish a dish when the restaurant already has 10 published dishes.
 */
public class IllegalStateException extends DomainException {

    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
