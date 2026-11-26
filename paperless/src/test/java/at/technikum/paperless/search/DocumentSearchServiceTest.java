package at.technikum.paperless.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.util.ObjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSearchServiceTest {

    @Mock
    private ElasticsearchClient client;

    private DocumentSearchService service;

    @BeforeEach
    void setUp() {
        service = new DocumentSearchService(client);
    }

    /** Passt zum Aufruf des Services: client.search(Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>, Class). */
    @SuppressWarnings("unchecked")
    private static Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> anyFunction() {
        return (Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>) any();
    }

    @Test
    void returnsHitIdsWhenSearchSucceeds() throws Exception {
        doReturn(searchResponse(List.of(hit("7"), hit("8"))))
                .when(client).search(anyFunction(), eq(Void.class));

        assertThat(service.searchDocumentIds("bayern")).containsExactly(7L, 8L);
    }

    @Test
    void returnsEmptyListWhenNoHits() throws Exception {
        doReturn(searchResponse(List.of()))
                .when(client).search(anyFunction(), eq(Void.class));

        assertThat(service.searchDocumentIds("bayern")).isEmpty();
    }

    @Test
    void returnsEmptyListWhenIndexNotFound() throws Exception {
        ElasticsearchException exception = mock(ElasticsearchException.class);
        when(exception.status()).thenReturn(404);
        doThrow(exception).when(client).search(anyFunction(), eq(Void.class));

        assertThat(service.searchDocumentIds("bayern")).isEmpty();
    }

    @Test
    void throwsWhenElasticsearchFails() throws Exception {
        ElasticsearchException exception = mock(ElasticsearchException.class);
        when(exception.status()).thenReturn(500);
        doThrow(exception).when(client).search(anyFunction(), eq(Void.class));

        assertThatThrownBy(() -> service.searchDocumentIds("bayern"))
                .isInstanceOf(SearchException.class);
    }

    @Test
    void throwsWhenElasticsearchIsUnreachable() throws Exception {
        doThrow(new IOException("connection refused"))
                .when(client).search(anyFunction(), eq(Void.class));

        assertThatThrownBy(() -> service.searchDocumentIds("bayern"))
                .isInstanceOf(SearchException.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> SearchResponse<T> searchResponse(List<Hit<T>> hits) {
        return SearchResponse.of(b -> b
                .took(1)
                .timedOut(false)
                .shards(co.elastic.clients.elasticsearch._types.ShardStatistics.of(sb -> sb.total(1).successful(1).failed(0)))
                .hits(HitsMetadata.of(hm -> hm.hits(hits))));
    }

    @SuppressWarnings("unchecked")
    private static <T> Hit<T> hit(String id) {
        return Hit.of(b -> b.index("documents").id(id));
    }
}
