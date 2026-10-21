package at.technikum.paperless.exception;

/**
 * Basisklasse für alle fachlichen Fehler aus dem Service-Layer.
 */
public abstract class ServiceException extends RuntimeException {

    protected ServiceException(String message) {
        super(message);
    }

    protected ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}