package at.technikum.genaiworker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Einstellungen für den Zugriff auf die Google Gemini API (Präfix "gemini.").
 * Der API-Key kommt über die Umgebungsvariable GEMINI_API_KEY (docker-compose / .env).
 */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        Duration timeout,
        int maxInputChars
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}