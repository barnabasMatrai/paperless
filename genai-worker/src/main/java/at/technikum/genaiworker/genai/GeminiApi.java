package at.technikum.genaiworker.genai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * JSON-Struktur des Gemini-Endpunkts "models/{model}:generateContent".
 * Unbekannte Felder der Antwort (usageMetadata, safetyRatings, ...) werden ignoriert.
 */
public final class GeminiApi {

    private GeminiApi() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {}

    public record GenerateContentRequest(List<Content> contents) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content, String finishReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PromptFeedback(String blockReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenerateContentResponse(List<Candidate> candidates, PromptFeedback promptFeedback) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorDetail(int code, String message, String status) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorResponse(ErrorDetail error) {}

}