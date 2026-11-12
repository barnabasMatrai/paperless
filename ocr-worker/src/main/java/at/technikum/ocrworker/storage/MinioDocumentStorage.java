package at.technikum.ocrworker.storage;

import at.technikum.ocrworker.config.MinioProperties;
import at.technikum.ocrworker.exception.OcrProcessingException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioDocumentStorage {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * Lädt das Originaldokument aus MinIO in eine temporäre Datei
     * (entspricht downloadObject(...) aus den Folien).
     * Der Aufrufer muss die Datei danach löschen.
     */
    public Path downloadToTempFile(String storageKey) {
        Path target = null;
        try {
            target = Files.createTempFile("ocr-", suffixOf(storageKey));

            try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(storageKey)
                    .build())) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Objekt '{}' aus Bucket '{}' nach {} geladen",
                    storageKey, properties.getBucket(), target);
            return target;

        } catch (Exception e) {
            deleteQuietly(target);
            throw new OcrProcessingException(
                    "Dokument '" + storageKey + "' konnte nicht aus MinIO geladen werden", e);
        }
    }

    public void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Temporäre Datei {} konnte nicht gelöscht werden", file, e);
        }
    }

    private static String suffixOf(String storageKey) {
        int dot = storageKey.lastIndexOf('.');
        return dot >= 0 ? storageKey.substring(dot) : ".tmp";
    }
}
