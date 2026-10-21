package at.technikum.paperless.messaging;

import at.technikum.paperless.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OcrMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public OcrMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDocumentForOcr(Long documentId) {
        log.debug("Publishing OCR job for document with ID {} to queue '{}'",
                documentId, RabbitMQConfig.OCR_QUEUE);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.OCR_QUEUE,
                    documentId.toString()
            );
        } catch (AmqpException e) {
            log.error("Failed to publish OCR job for document with ID {} to queue '{}'",
                    documentId, RabbitMQConfig.OCR_QUEUE, e);
            throw new MessagePublishException(
                    "Could not send document " + documentId + " for OCR processing", e);
        }

        log.info("OCR job for document with ID {} published to queue '{}'",
                documentId, RabbitMQConfig.OCR_QUEUE);
    }
}