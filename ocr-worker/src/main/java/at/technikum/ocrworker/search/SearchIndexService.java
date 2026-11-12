package at.technikum.ocrworker.search;

import at.technikum.ocrworker.exception.OcrProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Speichert den OCR-Text eines Dokuments im Elasticsearch-Index "documents",
 * damit das Dokument später über die Volltextsuche gefunden wird.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final OcrDocumentRepository repository;

    public void index(String documentId, String content) {
        try {
            repository.save(new OcrDocument(documentId, content, Instant.now()));
            log.info("Document ID {} indexed in Elasticsearch", documentId);
        } catch (Exception e) {
            // Als OcrProcessingException weiterwerfen, damit der Listener die Nachricht
            // verwirft statt sie endlos neu zu versuchen
            throw new OcrProcessingException(
                    "Dokument " + documentId + " konnte nicht in Elasticsearch indexiert werden", e);
        }
    }
}
