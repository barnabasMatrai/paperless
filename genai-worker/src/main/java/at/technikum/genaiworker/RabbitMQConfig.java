package at.technikum.genaiworker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Deklariert die Queue "ocr.results.genai" (Consumer auf dem OCR-Ergebnis) und
 * bindet sie an die Fanout-Exchange "ocr.results", die von OcrResultPublisher im
 * OCR-Worker einmalig deklariert wird (vgl. RabbitMQConfig im OCR-Worker). Die
 * GenAI-Listener selbst landen im GenAI-Worker (Sprint 5).
 */
@Configuration
public class RabbitMQConfig {

    public static final String OCR_RESULTS_EXCHANGE = "ocr.results";
    public static final String OCR_RESULTS_GENAI_QUEUE = "ocr.results.genai";

    @Bean
    public Queue ocrResultsGenaiQueue() {
        return new Queue(OCR_RESULTS_GENAI_QUEUE, false);
    }

    @Bean
    public Binding ocrResultsGenaiBinding() {
        return BindingBuilder.bind(ocrResultsGenaiQueue())
                .to(new FanoutExchange(OCR_RESULTS_EXCHANGE));
    }
}
