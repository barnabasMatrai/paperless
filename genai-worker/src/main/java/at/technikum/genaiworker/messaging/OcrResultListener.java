package at.technikum.genaiworker.messaging;

import at.technikum.genaiworker.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class OcrResultListener {

    @RabbitListener(queues = RabbitMQConfig.OCR_RESULTS_GENAI_QUEUE)
    public void onMessage(OcrDocumentEvent event) {
        if (event == null
                || event.documentId() == null
                || event.documentId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "OCR result has an empty document ID");
        }

        String documentId = event.documentId().trim();
        String text = event.content() == null ? "" : event.content();

        log.info("Received OCR result for document ID {} ({} characters)", documentId, text.length());

        if (text.isBlank()) {
            log.warn("OCR result for document ID {} contains no text, nothing to summarize", documentId);
            return;
        }

        // Sprint 5, Schritt 3: Text an Google Gemini senden und Zusammenfassung erhalten.
        // Schritt 4: Zusammenfassung an die REST-API zurückgeben und in der DB speichern.

        log.info("Finished processing OCR result for document ID {}", documentId);
    }
}