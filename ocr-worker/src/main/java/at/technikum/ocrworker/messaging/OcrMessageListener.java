package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.config.RabbitMQConfig;
import at.technikum.ocrworker.exception.OcrProcessingException;
import at.technikum.ocrworker.search.SearchIndexService;
import at.technikum.ocrworker.service.OcrService;
import at.technikum.ocrworker.storage.MinioDocumentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Empfängt OCR-Jobs aus der Queue, lädt das Originaldokument aus MinIO,
 * führt die Texterkennung aus und speichert den Text in Elasticsearch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcrMessageListener {

    private final MinioDocumentStorage documentStorage;
    private final OcrService ocrService;
    private final SearchIndexService searchIndexService;

    @RabbitListener(queues = RabbitMQConfig.OCR_QUEUE)
    public void onMessage(UploadDocumentEvent event) {
        if (event == null
                || event.documentId() == null
                || event.documentId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "OCR job has an empty document ID");
        }
        if (event.storageKey() == null || event.storageKey().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "OCR job for document ID " + event.documentId() + " has no storage key");
        }

        String documentId = event.documentId().trim();
        String storageKey = event.storageKey().trim();

        log.info("Received OCR job for document ID {} (storageKey={})", documentId, storageKey);

        Path file = null;
        try {
            file = documentStorage.downloadToTempFile(storageKey);
            log.info("Document ID {} downloaded from MinIO ({} bytes)",
                    documentId, file.toFile().length());

            String text = ocrService.extractText(file);
            log.info("OCR for document ID {} finished: {} characters recognized", documentId, text.length());
            log.debug("OCR text for document ID {}:\n{}", documentId, text);

            searchIndexService.index(documentId, text);

            log.info("Finished processing OCR job for document ID {}", documentId);

        } catch (OcrProcessingException e) {
            log.error("OCR job for document ID {} failed: {}", documentId, e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("OCR processing failed", e);
        } finally {
            documentStorage.deleteQuietly(file);
        }
    }
}