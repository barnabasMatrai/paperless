package at.technikum.paperless.messaging;

import at.technikum.paperless.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Veröffentlicht das Dokument-Deleted-Event auf der Fanout-Exchange
 * "ocr.results"; die gebundene Queue "ocr.results.index" erhält eine Kopie,
 * mit der der Indexing-Listener den Elasticsearch-Eintrag entfernt.
 */
@Component
@RabbitPublication(exchange = RabbitMQConfig.OCR_RESULTS_EXCHANGE, routingKey = "")
public class DocumentDeletedPublisher extends RabbitEventPublisher<DocumentDeletedEvent> {

    protected DocumentDeletedPublisher(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate);
    }
}
