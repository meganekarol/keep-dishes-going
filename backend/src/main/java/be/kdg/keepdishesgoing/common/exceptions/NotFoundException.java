package be.kdg.keepdishesgoing.common.exceptions;

/**
 * Exception thrown when a requested entity is not found.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String entityType, Object id) {
        super(String.format("%s with id %s not found", entityType, id));
    }
}
