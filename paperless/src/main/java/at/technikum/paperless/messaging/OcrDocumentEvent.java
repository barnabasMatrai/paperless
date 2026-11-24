package at.technikum.paperless.messaging;

/**
 * OCR-Ergebnis eines Dokuments: das erkannte Textfragment. Wird vom OCR-Worker
 * auf der Fanout-Exchange "ocr.results" publiziert (vgl.
 * at.technikum.ocrworker.messaging.OcrDocumentEvent).
 */
public record OcrDocumentEvent(String documentId, String content) {}
