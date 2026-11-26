package at.technikum.genaiworker.messaging;

import at.technikum.genaiworker.config.RabbitMQConfig;
import at.technikum.genaiworker.genai.GenAiException;
import at.technikum.genaiworker.genai.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrResultListener {

    private final GeminiClient geminiClient;
    private final SummaryResultPublisher summaryResultPublisher;

    @RabbitListener(queues = RabbitMQConfig.OCR_RESULTS_GENAI_QUEUE)
    public void onMessage(OcrDocumentEvent event) {
        if (event == null
                || event.documentId() == null
                || event.documentId().isBlank()) {
            throw new AmqpRejectAndDontRequeueException(
                    "OCR result has an empty document ID");
        }

        String documentId = event.documentId().trim();
        String text = event.content() == null ? "" : event.content();

        log.info("Received OCR result for document ID {} ({} characters)", documentId, text.length());

        if (text.isBlank()) {
            log.warn("OCR result for document ID {} contains no text, nothing to summarize", documentId);
            return;
        }

        String summary;
        try {
            summary = geminiClient.summarize(text);
        } catch (GenAiException e) {
            log.error("Could not create summary for document ID {}: {}", documentId, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(
                    "Summary generation failed for document ID " + documentId, e);
        }

        log.info("Summary for document ID {} created ({} characters)", documentId, summary.length());
        log.debug("Summary for document ID {}: {}", documentId, summary);

        try {
            summaryResultPublisher.publish(new SummaryResultEvent(documentId, summary));
        } catch (AmqpException e) {
            log.error("Could not publish summary for document ID {}: {}", documentId, e.getMessage());
            throw new AmqpRejectAndDontRequeueException(
                    "Publishing summary failed for document ID " + documentId, e);
        }

        log.info("Finished processing OCR result for document ID {}", documentId);
    }
}