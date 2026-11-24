package at.technikum.paperless.storage;

public interface IFileStorageService {

    /**
     * Legt die Datei ab und gibt den Schlüssel zurück, unter dem sie wiedergefunden wird.
     *
     * @throws FileStorageException wenn die Datei nicht abgelegt werden kann
     */
    String store(FileUpload file);

    /**
     * Löscht das Objekt mit dem gegebenen Schlüssel aus dem Speicher.
     *
     * @throws FileStorageException wenn das Objekt nicht gelöscht werden kann
     */
    void delete(String storageKey);
}
