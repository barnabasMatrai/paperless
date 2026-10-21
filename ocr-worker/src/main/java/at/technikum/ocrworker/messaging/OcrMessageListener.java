package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.config.RabbitMQConfig;
import at.technikum.ocrworker.exception.OcrProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Sprint 3: "leerer" OCR-Worker – empfängt die Nachricht, prüft sie und loggt sie.
// Die eigentliche OCR-Verarbeitung folgt in Sprint 4.
@Slf4j
@Component
public class OcrMessageListener {

    @RabbitListener(queues = RabbitMQConfig.OCR_QUEUE)
    public void onMessage(String message) {
        log.debug("Received OCR job with payload '{}'", message);

        try {
            Long documentId = parseDocumentId(message);
            log.info("Received OCR job for document with ID {}", documentId);

            // TODO Sprint 4: Dokument laden, OCR durchführen, Ergebnis speichern

            log.info("Finished processing OCR job for document with ID {} (no-op)", documentId);

        } catch (OcrProcessingException e) {
            log.error("OCR job with payload '{}' failed: {}", message, e.getMessage(), e);
            // Nicht erneut in die Queue legen, sonst würde dieselbe fehlerhafte
            // Nachricht endlos wieder zugestellt
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    private Long parseDocumentId(String message) {
        if (message == null || message.isBlank()) {
            throw new OcrProcessingException("OCR job message is empty");
        }

        try {
            return Long.valueOf(message.trim());
        } catch (NumberFormatException e) {
            throw new OcrProcessingException(
                    "OCR job message is not a valid document ID: '" + message + "'", e);
        }
    }
}