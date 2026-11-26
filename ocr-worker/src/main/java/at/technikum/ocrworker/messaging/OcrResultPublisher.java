package at.technikum.ocrworker.messaging;

import at.technikum.ocrworker.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Veröffentlicht das OCR-Ergebnis auf der Fanout-Exchange "ocr.results".
 * Die Exchange wird von diesem Modul (der Seite, die publiziert) deklariert;
 * jede gebundene Queue erhält eine Kopie der Nachricht.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcrResultPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(OcrDocumentEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.OCR_RESULTS_EXCHANGE, "", event);
        log.info("Published OCR result for document ID {}", event.documentId());
    }
}
