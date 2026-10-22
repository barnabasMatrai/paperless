package at.technikum.paperless.exception;

/**
 * Wird geworfen, wenn Document, User oder Reminder nicht existiert → 404.
 */
public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}