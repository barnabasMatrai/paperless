package at.technikum.paperless.messaging;

/**
 * Ergebnis der Löschung eines Dokuments: documentId und der Speicherschlüssel
 * der zugehörigen Datei in MinIO. Wird auf der Fanout-Exchange "ocr.results"
 * publiziert (Consumer: Indexing-Listener, der den Elasticsearch-Eintrag
 * entfernt).
 */
public record DocumentDeletedEvent(String documentId, String storageKey) {}
