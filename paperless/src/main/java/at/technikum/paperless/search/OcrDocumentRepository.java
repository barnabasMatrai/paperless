package at.technikum.paperless.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Zugriff auf den Elasticsearch-Index "documents" (speichern, lesen, löschen).
 */
public interface OcrDocumentRepository extends ElasticsearchRepository<OcrDocument, String> {
}
