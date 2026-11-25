package at.technikum.genaiworker.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OcrResultListenerTest {

    private final OcrResultListener listener = new OcrResultListener();

    @Test
    void rejectsNullEvent() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.onMessage(null));
    }

    @Test
    void rejectsBlankDocumentId() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.onMessage(new OcrDocumentEvent("  ", "some text")));
    }

    @Test
    void acceptsEmptyContent() {
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", null)));
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", "   ")));
    }

    @Test
    void acceptsValidEvent() {
        assertDoesNotThrow(() -> listener.onMessage(new OcrDocumentEvent("42", "Rechnung Nr. 123")));
    }
}