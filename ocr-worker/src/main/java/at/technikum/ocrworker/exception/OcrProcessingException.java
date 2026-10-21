package at.technikum.ocrworker.exception;

/**
 * Fehler bei der Verarbeitung eines OCR-Jobs im Worker
 * (z.B. ungültige Nachricht, später: OCR fehlgeschlagen).
 */
public class OcrProcessingException extends RuntimeException {

    public OcrProcessingException(String message) {
        super(message);
    }

    public OcrProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}