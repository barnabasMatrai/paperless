package at.technikum.paperless.messaging;

import at.technikum.paperless.config.RabbitMQConfig;
import at.technikum.paperless.exception.ResourceNotFoundException;
import at.technikum.paperless.service.IDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryResultListener {

    private final IDocumentService documentService;

    @RabbitListener(queues = RabbitMQConfig.GENAI_RESULTS_QUEUE)
    public void onMessage(SummaryResultEvent event) {
        if (event == null || event.documentId() == null || event.documentId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException("Summary result has an empty document ID");
        }

        Long documentId;
        try {
            documentId = Long.valueOf(event.documentId().trim());
        } catch (NumberFormatException e) {
            throw new AmqpRejectAndDontRequeueException(
                    "Summary result has an invalid document ID: " + event.documentId(), e);
        }

        if (event.summary() == null || event.summary().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "Summary result for document ID " + documentId + " is empty");
        }

        log.info("Received summary for document ID {} ({} characters)", documentId, event.summary().length());

        try {
            documentService.saveSummary(documentId, event.summary());
        } catch (ResourceNotFoundException e) {
            log.warn("Document ID {} no longer exists, summary discarded", documentId);
            return;
        }

        log.info("Summary for document ID {} stored in database", documentId);
    }
}