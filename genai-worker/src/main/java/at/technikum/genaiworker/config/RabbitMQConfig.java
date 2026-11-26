package at.technikum.genaiworker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String OCR_RESULTS_EXCHANGE = "ocr.results";
    public static final String OCR_RESULTS_GENAI_QUEUE = "ocr.results.genai";
    public static final String GENAI_RESULTS_QUEUE = "genai.results";

    @Bean
    public FanoutExchange ocrResultsExchange() {
        return new FanoutExchange(OCR_RESULTS_EXCHANGE, false, false);
    }

    @Bean
    public Queue ocrResultsGenaiQueue() {
        return new Queue(OCR_RESULTS_GENAI_QUEUE, false);
    }

    @Bean
    public Binding ocrResultsGenaiBinding() {
        return BindingBuilder.bind(ocrResultsGenaiQueue()).to(ocrResultsExchange());
    }

    @Bean
    public Queue genaiResultsQueue() {
        return new Queue(GENAI_RESULTS_QUEUE, false);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter jsonMessageConverter) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);

        return factory;
    }
}