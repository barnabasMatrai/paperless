package at.technikum.ocrworker.messaging;

/**
 * Ergebnis eines OCR-Jobs: das erkannte Textfragment für ein Dokument.
 * Wird auf der Fanout-Exchange "ocr.results" publiziert, sodass jeder Consumer
 * (Indexing-Listener und später GenAI-Worker) eine eigene Kopie erhält.
 */
public record OcrDocumentEvent(String documentId, String content) {}
