package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Sprint 3: "leerer" OCR-Worker – empfängt die Nachricht und loggt sie.
// Die eigentliche OCR-Verarbeitung folgt in Sprint 4.
@Slf4j
@Component
public class OcrMessageListener {

    @RabbitListener(queues = RabbitMQConfig.OCR_QUEUE)
    public void onMessage(String documentId) {
        log.info("Received OCR job for document with ID {}", documentId);

        // TODO Sprint 4: Dokument laden, OCR durchführen, Ergebnis speichern

        log.info("Finished processing OCR job for document with ID {} (no-op)", documentId);
    }
}
