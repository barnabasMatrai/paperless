package at.technikum.ocrworker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Muss mit dem Queue-Namen im REST-Server übereinstimmen
    public static final String OCR_QUEUE = "ocr.queue";

    // Der Worker deklariert die Queue selbst, damit er auch dann startet,
    // wenn er vor dem REST-Server hochkommt.
    // durable muss mit der Deklaration im REST-Server übereinstimmen (aktuell false),
    // sonst lehnt RabbitMQ die zweite Deklaration ab (PRECONDITION_FAILED).
    @Bean
    public Queue ocrQueue() {
        return new Queue(OCR_QUEUE, false);
    }
}
