package at.technikum.paperless.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Durchsucht den OCR-Text im Elasticsearch-Index "documents" und liefert die IDs
 * der passenden Dokumente. Die Dokumente selbst werden danach aus Postgres geladen.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private static final String INDEX = "documents";
    private static final int MAX_RESULTS = 100;

    private final ElasticsearchClient client;

    public List<Long> searchDocumentIds(String query) {
        try {
            // Nur die IDs abfragen (fetch=false), den Text selbst brauchen wir hier nicht
            SearchResponse<Void> response = client.search(s -> s
                            .index(INDEX)
                            .size(MAX_RESULTS)
                            .source(src -> src.fetch(false))
                            .query(q -> q.match(m -> m
                                    .field("content")
                                    .query(query)
                                    .fuzziness("AUTO"))),   // toleriert kleine OCR-/Tippfehler
                    Void.class);

            List<Long> ids = response.hits().hits().stream()
                    .map(Hit::id)
                    .map(DocumentSearchService::parseId)
                    .filter(Objects::nonNull)
                    .toList();

            log.debug("Full-text search for '{}' returned {} hits", query, ids.size());
            return ids;

        } catch (ElasticsearchException e) {
            // Index existiert noch nicht (noch nie etwas indexiert) -> einfach keine Treffer
            if (e.status() == 404) {
                return List.of();
            }
            throw new SearchException("Volltextsuche fehlgeschlagen", e);
        } catch (IOException e) {
            throw new SearchException("Elasticsearch ist nicht erreichbar", e);
        }
    }

    private static Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}