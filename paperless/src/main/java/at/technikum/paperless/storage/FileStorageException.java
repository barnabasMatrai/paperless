package at.technikum.paperless.storage;

/**
 * Fehler im Storage-Layer: Datei konnte nicht abgelegt oder gelesen werden.
 * Wird ab Sprint 4 von der MinIO-Implementierung geworfen.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}