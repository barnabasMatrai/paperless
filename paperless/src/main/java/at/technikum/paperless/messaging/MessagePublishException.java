package at.technikum.paperless.messaging;

/**
 * Fehler im Messaging-Layer: Nachricht konnte nicht an RabbitMQ gesendet werden.
 * Kapselt die technische AmqpException, damit Service- und Controller-Layer
 * nicht direkt von Spring AMQP abhängen.
 */
public class MessagePublishException extends RuntimeException {

    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}