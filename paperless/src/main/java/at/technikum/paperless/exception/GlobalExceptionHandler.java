package at.technikum.paperless.exception;

import at.technikum.paperless.messaging.MessagePublishException;
import at.technikum.paperless.storage.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== Validation / Request ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        log.warn("Request validation failed: {}", errors);
        return errors;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadableMessage(
            HttpMessageNotReadableException exception) {

        log.warn("Malformed request body: {}", exception.getMessage());
        return Map.of("error", "Malformed request body");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public Map<String, String> handleMaxUploadSize(
            MaxUploadSizeExceededException exception) {

        log.warn("Upload rejected: file exceeds maximum size");
        return Map.of("error", "File must not be larger than 20 MB");
    }

    // ==================== Service Layer ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleResourceNotFound(
            ResourceNotFoundException exception) {

        log.warn("Resource not found: {}", exception.getMessage());
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidFile(
            InvalidFileException exception) {

        log.warn("Invalid file: {}", exception.getMessage());
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(
            InvalidCredentialsException exception) {

        log.warn("Authentication failed: {}", exception.getMessage());
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException exception) {

        log.warn("Conflict: {}", exception.getMessage());
        return Map.of("error", exception.getMessage());
    }

    // Fallback für zukünftige ServiceExceptions ohne eigenen Handler
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleServiceException(
            ServiceException exception) {

        log.warn("Service error: {}", exception.getMessage());
        return Map.of("error", exception.getMessage());
    }

    // ==================== Messaging Layer ====================

    @ExceptionHandler(MessagePublishException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleMessagePublish(
            MessagePublishException exception) {

        // Details + Stacktrace wurden bereits im OcrMessagePublisher geloggt
        log.error("Messaging error: {}", exception.getMessage());
        return Map.of("error", "The messaging service is currently unavailable");
    }

    // ==================== Storage Layer ====================

    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleFileStorage(
            FileStorageException exception) {

        log.error("File storage error: {}", exception.getMessage(), exception);
        return Map.of("error", "The file could not be stored");
    }

    // ==================== Security ====================
    // Explizit behandelt, damit der Catch-all unten sie nicht als 500 ausgibt.

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccessDenied(
            AccessDeniedException exception) {

        log.warn("Access denied: {}", exception.getMessage());
        return Map.of("error", "Access denied");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthentication(
            AuthenticationException exception) {

        log.warn("Authentication error: {}", exception.getMessage());
        return Map.of("error", "Unauthorized");
    }

    // ==================== Catch-all ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception exception) {

        // Standard-Fehler von Spring MVC (z.B. ResponseStatusException, 405,
        // fehlender Request-Part) behalten ihren eigentlichen Statuscode
        if (exception instanceof ErrorResponse errorResponse) {
            String detail = errorResponse.getBody().getDetail();

            log.warn("Request failed with status {}: {}",
                    errorResponse.getStatusCode(), detail);

            return ResponseEntity
                    .status(errorResponse.getStatusCode())
                    .body(Map.of("error",
                            detail != null ? detail : "Request could not be processed"));
        }

        // Alles andere ist ein unerwarteter Fehler: voll loggen,
        // aber keine internen Details an den Client geben
        log.error("Unexpected error", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}