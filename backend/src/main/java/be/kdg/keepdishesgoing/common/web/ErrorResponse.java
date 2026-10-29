package be.kdg.keepdishesgoing.common.web;

import java.time.LocalDateTime;

/**
 * The single error shape every endpoint returns, so clients can handle failures
 * uniformly instead of guessing per controller.
 *
 * @param status    HTTP status code, repeated in the body for convenience
 * @param error     the status' reason phrase, e.g. "Bad Request"
 * @param message   what went wrong, safe to show to a user
 * @param path      the request URI that failed
 * @param timestamp when the failure was handled
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
}
