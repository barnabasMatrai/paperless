package at.technikum.paperless.messaging;

import at.technikum.paperless.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RabbitPublication(exchange = "", routingKey = "ocr.queue")
public class UploadDocumentPublisher extends RabbitEventPublisher<UploadDocumentEvent> {
    protected UploadDocumentPublisher(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate);
    }

    @Override
    public void publish(UploadDocumentEvent event) {
        super.publish(event);
    }
}
