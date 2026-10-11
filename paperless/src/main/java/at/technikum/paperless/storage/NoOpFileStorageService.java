package at.technikum.paperless.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// Sprint 2: Datei wird nur entgegengenommen und nicht gespeichert.
// Wird in Sprint 3 durch eine MinIO-Implementierung ersetzt.
@Slf4j
@Service
public class NoOpFileStorageService implements IFileStorageService {

    @Override
    public String store(MultipartFile file) {
        String key = UUID.randomUUID().toString();

        log.info("Datei empfangen (nicht gespeichert): name={}, size={} bytes, contentType={}, key={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType(), key);

        return key;
    }
}
