package at.technikum.paperless.messaging;

import at.technikum.paperless.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OcrMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public OcrMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDocumentForOcr(Long documentId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.OCR_QUEUE,
                documentId.toString()
        );
    }
}
