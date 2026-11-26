package at.technikum.genaiworker.messaging;

import at.technikum.genaiworker.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Legt die fertige Zusammenfassung über die Default-Exchange direkt auf die
 * Queue "genai.results"; die paperless-API speichert sie in der Datenbank.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryResultPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(SummaryResultEvent event) {
        rabbitTemplate.convertAndSend("", RabbitMQConfig.GENAI_RESULTS_QUEUE, event);
        log.info("Published summary for document ID {} to queue {}",
                event.documentId(), RabbitMQConfig.GENAI_RESULTS_QUEUE);
    }
}