package at.technikum.genaiworker.genai;

import at.technikum.genaiworker.config.GeminiProperties;
import at.technikum.genaiworker.genai.GeminiApi.Candidate;
import at.technikum.genaiworker.genai.GeminiApi.Content;
import at.technikum.genaiworker.genai.GeminiApi.ErrorResponse;
import at.technikum.genaiworker.genai.GeminiApi.GenerateContentRequest;
import at.technikum.genaiworker.genai.GeminiApi.GenerateContentResponse;
import at.technikum.genaiworker.genai.GeminiApi.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GeminiClient {

    private static final String PROMPT = """
            Fasse das folgende Dokument in 3 bis 5 Sätzen zusammen.
            Schreibe die Zusammenfassung in der Sprache des Dokuments.
            Nenne die wichtigsten Fakten (z. B. Art des Dokuments, Beteiligte, Beträge, Daten).
            Antworte nur mit der Zusammenfassung, ohne Einleitung.

            Dokument:
            ---
            %s
            ---
            """;

    private static final int MAX_ERROR_BODY_LENGTH = 500;
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration INITIAL_BACKOFF = Duration.ofSeconds(2);
    private static final Set<Integer> RETRYABLE_STATUS = Set.of(429, 500, 502, 503, 504);

    private final GeminiProperties properties;
    private final HttpClient httpClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String summarize(String text) {
        if (!properties.hasApiKey()) {
            throw new GenAiException("No Gemini API key configured (GEMINI_API_KEY is empty)");
        }

        String input = truncate(text);
        HttpRequest request = buildRequest(input);

        Duration backoff = INITIAL_BACKOFF;
        for (int attempt = 1; ; attempt++) {
            try {
                return callGemini(request, input.length(), attempt);
            } catch (GenAiException e) {
                if (!e.isRetryable() || attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
                log.warn("Gemini attempt {}/{} failed: {} - retrying in {} s",
                        attempt, MAX_ATTEMPTS, e.getMessage(), backoff.toSeconds());
                sleep(backoff);
                backoff = backoff.multipliedBy(2);
            }
        }
    }

    private String callGemini(HttpRequest request, int inputLength, int attempt) {
        log.info("Sending {} characters to Gemini (model={}, attempt {}/{})",
                inputLength, properties.model(), attempt, MAX_ATTEMPTS);
        long start = System.currentTimeMillis();

        HttpResponse<String> response = send(request);

        log.info("Gemini answered with HTTP {} after {} ms",
                response.statusCode(), System.currentTimeMillis() - start);

        if (response.statusCode() != 200) {
            throw new GenAiException(
                    "Gemini API returned HTTP " + response.statusCode() + ": " + describeError(response.body()),
                    RETRYABLE_STATUS.contains(response.statusCode()));
        }

        return extractSummary(response.body());
    }

    private String truncate(String text) {
        int max = properties.maxInputChars();
        if (text.length() <= max) {
            return text;
        }
        log.warn("OCR text has {} characters, truncating to {} before sending to Gemini", text.length(), max);
        return text.substring(0, max);
    }

    private HttpRequest buildRequest(String input) {
        GenerateContentRequest body = new GenerateContentRequest(List.of(
                new Content(List.of(new Part(PROMPT.formatted(input))))));

        URI uri = URI.create(properties.baseUrl() + "/models/" + properties.model() + ":generateContent");

        return HttpRequest.newBuilder(uri)
                .timeout(properties.timeout())
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", properties.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(body)))
                .build();
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new GenAiException("Gemini request timed out after " + properties.timeout(), e, true);
        } catch (IOException e) {
            throw new GenAiException("Gemini request failed: " + e.getMessage(), e, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenAiException("Gemini request was interrupted", e);
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenAiException("Interrupted while waiting to retry the Gemini request", e);
        }
    }

    private String extractSummary(String responseBody) {
        GenerateContentResponse response;
        try {
            response = jsonMapper.readValue(responseBody, GenerateContentResponse.class);
        } catch (JacksonException e) {
            throw new GenAiException("Could not parse Gemini response: " + abbreviate(responseBody), e);
        }

        if (response.candidates() == null || response.candidates().isEmpty()) {
            String reason = response.promptFeedback() != null ? response.promptFeedback().blockReason() : null;
            throw new GenAiException("Gemini returned no answer"
                    + (reason != null ? " (blocked: " + reason + ")" : ""));
        }

        Candidate candidate = response.candidates().getFirst();
        String summary = candidate.content() == null || candidate.content().parts() == null
                ? ""
                : candidate.content().parts().stream()
                  .map(Part::text)
                  .filter(Objects::nonNull)
                  .collect(Collectors.joining())
                  .trim();

        if (summary.isBlank()) {
            throw new GenAiException("Gemini returned an empty summary (finishReason="
                    + candidate.finishReason() + ")");
        }
        return summary;
    }

    /** Liest aus Googles Fehler-JSON nur Status und Meldung, sonst den gekürzten Body. */
    private String describeError(String body) {
        try {
            ErrorResponse error = jsonMapper.readValue(body, ErrorResponse.class);
            if (error != null && error.error() != null && error.error().message() != null) {
                return error.error().status() + " - " + error.error().message();
            }
        } catch (JacksonException ignored) {
            // kein JSON im erwarteten Format -> Rohtext verwenden
        }
        return abbreviate(body);
    }

    private static String abbreviate(String s) {
        if (s == null || s.isEmpty()) {
            return "<empty>";
        }
        return s.length() <= MAX_ERROR_BODY_LENGTH ? s : s.substring(0, MAX_ERROR_BODY_LENGTH) + "...";
    }
}