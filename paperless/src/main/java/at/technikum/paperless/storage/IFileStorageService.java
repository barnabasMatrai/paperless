package at.technikum.paperless.storage;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    /**
     * Legt die Datei ab und gibt den Schlüssel zurück, unter dem sie wiedergefunden wird.
     *
     * @throws FileStorageException wenn die Datei nicht abgelegt werden kann
     */
    String store(MultipartFile file);
}
