package be.kdg.keepdishesgoing.common.web;

import be.kdg.keepdishesgoing.common.exceptions.DomainException;
import be.kdg.keepdishesgoing.common.exceptions.MessagePublishingException;
import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Translates exceptions into HTTP responses in one place.
 *
 * <p>Controllers used to each carry their own try/catch ladder, which meant the
 * ones without a ladder returned bare 500s for ordinary rule violations. The
 * domain throws exceptions; deciding what those mean over HTTP is the web
 * adapter's job, and this is the web adapter's one copy of that decision.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Denials from {@code @PreAuthorize} arrive here before Spring Security's
     * filter can translate them. Without this handler the catch-all below would
     * turn every 403 into a 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.FORBIDDEN, "You are not allowed to perform this action", request);
    }

    // ==================== Not found ====================

    @ExceptionHandler({NotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e, HttpServletRequest request) {
        log.warn("Not found on {}: {}", request.getRequestURI(), e.getMessage());
        String message = e.getMessage() == null ? "Resource not found" : e.getMessage();
        return build(HttpStatus.NOT_FOUND, message, request);
    }

    // ==================== Bad request ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Invalid request on {}: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    /**
     * A path variable or query parameter that will not convert — an id that is
     * not a UUID, a cuisine that is not in the enum. Spring raises this before
     * the controller runs, and it is the caller's mistake, not the server's.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest request) {
        String message = "Invalid value for '" + e.getName() + "': " + e.getValue();
        log.warn("Type mismatch on {}: {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed on {}: {}", request.getRequestURI(), details);
        return build(HttpStatus.BAD_REQUEST, details.isBlank() ? "Validation failed" : details, request);
    }

    /**
     * Business rules that are neither "bad input" nor "not found" — a violated
     * invariant such as publishing an eleventh dish.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException e, HttpServletRequest request) {
        log.warn("Domain rule violated on {}: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    // ==================== Conflict ====================

    /**
     * The aggregate exists but is in the wrong state for this operation, e.g.
     * accepting an order that was already rejected. Retrying identically will
     * fail again, so it is a conflict rather than a bad request.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        log.warn("Illegal state on {}: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    // ==================== Infrastructure ====================

    @ExceptionHandler(MessagePublishingException.class)
    public ResponseEntity<ErrorResponse> handlePublishing(MessagePublishingException e, HttpServletRequest request) {
        log.error("Failed to publish message for {}", request.getRequestURI(), e);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Could not reach the messaging service, please retry", request);
    }

    /**
     * Last resort. The message is deliberately generic: an unexpected failure
     * may carry internals that should not leave the server. The stack trace goes
     * to the log instead.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Unexpected error on {}", request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        ));
    }
}
