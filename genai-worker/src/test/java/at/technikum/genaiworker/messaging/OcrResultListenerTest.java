package at.technikum.genaiworker.messaging;

import at.technikum.genaiworker.genai.GenAiException;
import at.technikum.genaiworker.genai.GeminiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OcrResultListenerTest {

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private OcrResultListener listener;

    @Test
    void rejectsNullEvent() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(null));
        verifyNoInteractions(geminiClient);
    }

    @Test
    void rejectsBlankDocumentId() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMessage(new OcrDocumentEvent("  ", "some text")));
        verifyNoInteractions(geminiClient);
    }

    @Test
    void skipsEmptyContentWithoutCallingGemini() {
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", null)));
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", "   ")));
        verifyNoInteractions(geminiClient);
    }

    @Test
    void summarizesValidEvent() {
        when(geminiClient.summarize("Rechnung Nr. 123")).thenReturn("Eine Rechnung mit der Nummer 123.");

        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", "Rechnung Nr. 123")));

        verify(geminiClient).summarize("Rechnung Nr. 123");
    }

    @Test
    void rejectsMessageWhenGeminiFails() {
        when(geminiClient.summarize("Rechnung Nr. 123"))
                .thenThrow(new GenAiException("Gemini API returned HTTP 403"));

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMessage(new OcrDocumentEvent("42", "Rechnung Nr. 123")));
    }
}