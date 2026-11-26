package at.technikum.paperless.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * Queue, auf die die OCR-Jobs kommen. Deklariert wird sie einmalig hier (der
     * Seite, die publiziert, vgl. UploadDocumentPublisher).
     */
    public static final String OCR_QUEUE = "ocr.queue";

    /**
     * Fanout-Exchange, auf dem die OCR-Ergebnisse publiziert werden. Deklariert
     * wird dieser Exchange einmalig durch den OCR-Worker (der Seite, die
     * publiziert, vgl. at.technikum.ocrworker.config.RabbitMQConfig).
     */
    public static final String OCR_RESULTS_EXCHANGE = "ocr.results";

    /**
     * Queue des Indexing-Listeners (Consumer auf ocr.results.index). Deklariert
     * wird sie einmalig hier, dem Consumer.
     */
    public static final String OCR_RESULTS_INDEX_QUEUE = "ocr.results.index";


    /**
     * Queue, auf die der GenAI-Worker die fertigen Zusammenfassungen legt.
     * Wird hier (Consumer) und im GenAI-Worker (Publisher) mit identischen
     * Parametern deklariert, damit die Startreihenfolge egal ist.
     */
    public static final String GENAI_RESULTS_QUEUE = "genai.results";

    @Bean
    public Queue genaiResultsQueue() {
        return new Queue(GENAI_RESULTS_QUEUE, false);
    }

    @Bean
    public Queue ocrQueue() {
        return new Queue(OCR_QUEUE, false);
    }

    @Bean
    public Queue ocrResultsIndexQueue() {
        return new Queue(OCR_RESULTS_INDEX_QUEUE, false);
    }

    @Bean
    public Binding ocrResultsIndexBinding() {
        return BindingBuilder.bind(ocrResultsIndexQueue())
                .to(new FanoutExchange(OCR_RESULTS_EXCHANGE));
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
