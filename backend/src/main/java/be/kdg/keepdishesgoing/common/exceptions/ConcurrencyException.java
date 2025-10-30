package be.kdg.keepdishesgoing.common.exceptions;

/**
 * Exception thrown when optimistic locking fails in event store.
 * This happens when two processes try to modify the same aggregate simultaneously.
 */
public class ConcurrencyException extends DomainException {
    
    public ConcurrencyException(String message) {
        super(message);
    }
    
    public ConcurrencyException(Object aggregateId, long expectedVersion, long actualVersion) {
        super(String.format(
                "Concurrency conflict for aggregate %s. Expected version %d but found %d",
                aggregateId, expectedVersion, actualVersion
        ));
    }
}
