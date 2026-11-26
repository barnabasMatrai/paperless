package at.technikum.genaiworker.messaging;

import at.technikum.genaiworker.genai.GenAiException;
import at.technikum.genaiworker.genai.GeminiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrResultListenerTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private SummaryResultPublisher summaryResultPublisher;

    @InjectMocks
    private OcrResultListener listener;

    @Test
    void rejectsNullEvent() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(null));
        verifyNoInteractions(geminiClient, summaryResultPublisher);
    }

    @Test
    void rejectsBlankDocumentId() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMessage(new OcrDocumentEvent("  ", "some text")));
        verifyNoInteractions(geminiClient, summaryResultPublisher);
    }

    @Test
    void skipsEmptyContentWithoutCallingGemini() {
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", null)));
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", "   ")));
        verifyNoInteractions(geminiClient, summaryResultPublisher);
    }

    @Test
    void summarizesAndPublishesValidEvent() {
        when(geminiClient.summarize("Rechnung Nr. 123")).thenReturn("Eine Rechnung mit der Nummer 123.");

        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", "Rechnung Nr. 123")));

        verify(summaryResultPublisher).publish(new SummaryResultEvent("42", "Eine Rechnung mit der Nummer 123."));
    }

    @Test
    void rejectsMessageWhenGeminiFails() {
        when(geminiClient.summarize("Rechnung Nr. 123"))
                .thenThrow(new GenAiException("Gemini API returned HTTP 403"));

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMessage(new OcrDocumentEvent("42", "Rechnung Nr. 123")));
        verifyNoInteractions(summaryResultPublisher);
    }

    @Test
    void rejectsMessageWhenPublishingFails() {
        when(geminiClient.summarize("Rechnung Nr. 123")).thenReturn("Zusammenfassung");
        doThrow(new AmqpException("broker down")).when(summaryResultPublisher).publish(any());

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMessage(new OcrDocumentEvent("42", "Rechnung Nr. 123")));
    }
}