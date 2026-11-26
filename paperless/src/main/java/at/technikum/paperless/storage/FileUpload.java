package at.technikum.paperless.storage;

import java.io.InputStream;

/**
 * Die Daten eines hochgeladenen Datei-Objekts, so wie sie der Controller an die
 * Service-Schichten weiterleitet: Dateiname, Content-Type, Größe und der
 * Datenstrom. Stattdessen wird kein Spring-Web-Typ (MultipartFile) durch die
 * Service-Schichten gereicht.
 */
public record FileUpload(String filename, String contentType, long size, InputStream content) {
}
