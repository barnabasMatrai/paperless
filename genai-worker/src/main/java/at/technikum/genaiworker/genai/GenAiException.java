package at.technikum.genaiworker.genai;


public class GenAiException extends RuntimeException {

    private final boolean retryable;

    public GenAiException(String message) {
        this(message, false);
    }

    public GenAiException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public GenAiException(String message, Throwable cause) {
        this(message, cause, false);
    }

    public GenAiException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}