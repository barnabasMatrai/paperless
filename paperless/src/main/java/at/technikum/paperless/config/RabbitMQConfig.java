package at.technikum.paperless.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OCR_QUEUE = "ocr.queue";

    @Bean
    public Queue ocrQueue() {
        return new Queue(OCR_QUEUE, false);
    }
}
