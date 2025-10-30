package be.kdg.keepdishesgoing.common.exceptions;

/**
 * Exception thrown when a message fails to publish to RabbitMQ.
 */
public class MessagePublishingException extends DomainException {
    
    public MessagePublishingException(String message) {
        super(message);
    }
    
    public MessagePublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
