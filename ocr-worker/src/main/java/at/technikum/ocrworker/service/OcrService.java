package at.technikum.ocrworker.service;

import at.technikum.ocrworker.config.OcrProperties;
import at.technikum.ocrworker.exception.OcrProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * Führt die Texterkennung aus: PDFs werden Seite für Seite in Bilder umgewandelt (PDFBox)
 * und dann mit Tesseract gelesen. Bilder (PNG/JPG) gehen direkt an Tesseract.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");

    private final OcrProperties properties;

    public String extractText(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);

        try {
            if (name.endsWith(".pdf")) {
                return ocrPdf(file);
            }
            if (IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith)) {
                return ocrImage(file);
            }
        } catch (IOException | TesseractException e) {
            throw new OcrProcessingException("OCR fehlgeschlagen für " + name, e);
        } catch (UnsatisfiedLinkError e) {
            // Tritt auf, wenn Tesseract im System nicht installiert ist
            throw new OcrProcessingException("Tesseract-Library nicht gefunden – ist Tesseract installiert?", e);
        }

        throw new OcrProcessingException("Dateityp wird für OCR nicht unterstützt: " + name);
    }

    private String ocrPdf(Path file) throws IOException, TesseractException {
        Tesseract tesseract = createTesseract();
        StringBuilder text = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = document.getNumberOfPages();

            for (int page = 0; page < pages; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, properties.getDpi(), ImageType.GRAY);
                text.append(tesseract.doOCR(image)).append('\n');
                log.debug("OCR für Seite {}/{} abgeschlossen", page + 1, pages);
            }
        }
        return text.toString().trim();
    }

    private String ocrImage(Path file) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(file.toFile());
        if (image == null) {
            throw new OcrProcessingException("Bild konnte nicht gelesen werden: " + file.getFileName());
        }
        return createTesseract().doOCR(image).trim();
    }

    // Tesseract ist nicht thread-safe, deshalb pro Aufruf eine eigene Instanz
    private Tesseract createTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(properties.getDatapath());
        tesseract.setLanguage(properties.getLanguage());
        return tesseract;
    }
}