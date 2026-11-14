package at.technikum.paperless.search;

/**
 * Fehler bei der Volltextsuche: Elasticsearch ist nicht erreichbar oder die Anfrage ist fehlgeschlagen.
 */
public class SearchException extends RuntimeException {

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
