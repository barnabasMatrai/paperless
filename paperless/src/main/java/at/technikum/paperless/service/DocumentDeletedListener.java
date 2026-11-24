package at.technikum.paperless.service;

import at.technikum.paperless.messaging.DocumentDeletedEvent;
import at.technikum.paperless.messaging.DocumentDeletedPublisher;
import at.technikum.paperless.storage.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Konsumiert {@link DocumentDeletedEvent} nach dem DB-Commit des Deletes
 * (AFTER_COMMIT), damit der Commit nicht scheitern muss, nachdem die Datei
 * bereits weg ist. Löscht die Datei in MinIO und leitet die Löschung des
 * Indexierungs-Eintrags weiter (vgl. messaging.OcrResultsIndexListener).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentDeletedListener {

    private final IFileStorageService fileStorageService;
    private final DocumentDeletedPublisher documentDeletedPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentDeleted(DocumentDeletedEvent event) {
        log.info("Deleting file for document ID {} from MinIO", event.documentId());
        fileStorageService.delete(event.storageKey());

        // Meldung an den Indexing-Consumer: der Elasticsearch-Eintrag wird entfernt.
        documentDeletedPublisher.publish(event);
    }
}
