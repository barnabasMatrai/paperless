package at.technikum.paperless.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Speichert den OCR-Text eines Dokuments im Elasticsearch-Index "documents",
 * damit das Dokument später über die Volltextsuche gefunden wird.
 * Aufgerufen wird dieser Service vom Indexing-Listener
 * (messaging.OcrResultsIndexListener), der das OCR-Ergebnis von der
 * Fanout-Exchange "ocr.results" konsumiert.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final OcrDocumentRepository repository;

    public void index(String documentId, String content) {
        repository.save(new OcrDocument(documentId, content, Instant.now()));
        log.info("Document ID {} indexed in Elasticsearch", documentId);
    }

    public void delete(String documentId) {
        repository.deleteById(documentId);
        log.info("Document ID {} removed from Elasticsearch", documentId);
    }
}
