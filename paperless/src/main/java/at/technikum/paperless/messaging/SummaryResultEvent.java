package at.technikum.paperless.messaging;

/**
 * Ergebnis des GenAI-Workers: die Zusammenfassung zu einem Dokument.
 * Kommt über die Queue "genai.results" (vgl. at.technikum.genaiworker.messaging.SummaryResultEvent).
 */
public record SummaryResultEvent(String documentId, String summary) {}