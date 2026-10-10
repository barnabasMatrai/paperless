package at.technikum.paperless.storage;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    /**
     * Legt die Datei ab und gibt den Schlüssel zurück, unter dem sie wiedergefunden wird.
     */
    String store(MultipartFile file);
}
