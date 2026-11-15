package at.technikum.ocrworker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Liest die OCR-Einstellungen (ocr.*) aus der application.properties:
 * Pfad zu den Tesseract-Sprachdaten, Sprache(n) und Auflösung beim Umwandeln der PDF-Seiten.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ocr")
public class OcrProperties {
    private String datapath = "/usr/share/tesseract-ocr/5/tessdata";
    private String language = "deu+eng";
    private int dpi = 300;
}