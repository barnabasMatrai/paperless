package at.technikum.paperless.messaging;

import at.technikum.paperless.config.RabbitMQConfig;
import at.technikum.paperless.search.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer auf der Queue "ocr.results.index": nimmt das OCR-Ergebnis der
 * Fanout-Exchange "ocr.results" entgegen und schreibt den erkannten Text per
 * SearchIndexService in den Elasticsearch-Index "documents".
 * Da die Queue nicht für eine einmalige Verarbeitung wie ocr.queue geplant ist,
 * werden Fehler standardmäßig wiederqueue-t, sodass bei nicht erreichbarem
 * Elasticsearch die Indexierung automatisch retried werden kann.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcrResultsIndexListener {

    private final SearchIndexService searchIndexService;

    @RabbitListener(queues = RabbitMQConfig.OCR_RESULTS_INDEX_QUEUE)
    public void onMessage(OcrDocumentEvent event) {
        if (event == null
                || event.documentId() == null
                || event.documentId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "OCR result has an empty document ID");
        }
        log.info("Indexing OCR result for document ID {}", event.documentId());

        searchIndexService.index(event.documentId(), event.content());

        log.info("Finished indexing OCR result for document ID {}", event.documentId());
    }

    @RabbitListener(queues = RabbitMQConfig.OCR_RESULTS_INDEX_QUEUE)
    public void onDocumentDeleted(DocumentDeletedEvent event) {
        log.info("Removing OCR index entry for document ID {}", event.documentId());
        searchIndexService.delete(event.documentId());
    }
}
