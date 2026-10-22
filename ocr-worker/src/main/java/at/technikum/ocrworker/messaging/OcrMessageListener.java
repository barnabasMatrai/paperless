
package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.config.RabbitMQConfig;
import at.technikum.ocrworker.exception.OcrProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OcrMessageListener {

    @RabbitListener(queues = RabbitMQConfig.OCR_QUEUE)
    public void onMessage(UploadDocumentEvent event) {
        if (event == null
                || event.documentId() == null
                || event.documentId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "OCR job has an empty document ID");
        }

        String documentId = event.documentId().trim();

        log.info("Received OCR job for document ID {}", documentId);

        try {
            // TODO Sprint 4: Load document, perform OCR, save result

            log.info(
                    "Finished processing OCR job for document ID {} (no-op)",
                    documentId
            );

        } catch (OcrProcessingException e) {
            log.error(
                    "OCR job for document ID {} failed: {}",
                    documentId,
                    e.getMessage(),
                    e
            );

            throw new AmqpRejectAndDontRequeueException(
                    "OCR processing failed", e);
        }
    }
}
