package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.exception.ResourceNotFoundException;
import at.technikum.paperless.messaging.UploadDocumentEvent;
import at.technikum.paperless.messaging.UploadDocumentPublisher;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IUserRepository;
import at.technikum.paperless.search.DocumentSearchService;
import at.technikum.paperless.storage.FileUpload;
import at.technikum.paperless.storage.IFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private IDocumentRepository documentRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private IFileStorageService fileStorageService;
    @Mock
    private UploadDocumentPublisher uploadDocumentPublisher;
    @Mock
    private DocumentSearchService documentSearchService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DocumentService documentService;

    private Document document;
    private User user;
    private FileUpload file;

    @BeforeEach
    void setUp() {
        document = new Document();
        document.setId(7L);
        document.setFilename("test.pdf");

        user = new User();
        user.setId(3L);

        file = new FileUpload("test.pdf", "application/pdf", 12L,
                new ByteArrayInputStream("hello world!".getBytes()));
    }

    private static Document document(Long id, Long userId) {
        Document doc = new Document();
        doc.setId(id);
        if (userId != null) {
            User u = new User();
            u.setId(userId);
            doc.setUser(u);
        }
        return doc;
    }

    @Test
    void findAllReturnsAllDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(document));

        assertThat(documentService.findAll()).containsExactly(document);
    }

    @Test
    void findAllByUserIdReturnsDocuments() {
        when(documentRepository.findAllByUserIdOrderByUploadDateDesc(3L)).thenReturn(List.of(document));

        assertThat(documentService.findAllByUserId(3L)).containsExactly(document);
    }

    @Test
    void findByIdReturnsDocument() {
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        assertThat(documentService.findById(7L)).isSameAs(document);
    }

    @Test
    void findByIdThrowsIfNotFound() {
        when(documentRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.findById(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void uploadStoresFileSetsUserAndStorageKeyAndPublishesOcrEvent() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(fileStorageService.store(file)).thenReturn("storage-key");
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = documentService.upload(3L, document, file);

        assertThat(saved).isSameAs(document);
        verify(fileStorageService).store(file);
        assertThat(document.getUser()).isSameAs(user);
        assertThat(document.getStorageKey()).isEqualTo("storage-key");

        ArgumentCaptor<UploadDocumentEvent> captor = ArgumentCaptor.forClass(UploadDocumentEvent.class);
        verify(uploadDocumentPublisher).publish(captor.capture());
        assertThat(captor.getValue().getDocumentId()).isEqualTo("7");
        assertThat(captor.getValue().getStorageKey()).isEqualTo("storage-key");
    }

    @Test
    void uploadThrowsIfUserNotFound() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.upload(3L, document, file))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(fileStorageService, documentRepository);
    }

    @Test
    void updateCopiesTypeAndFilenameAndSaves() {
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document updated = new Document();
        updated.setDocumentType(at.technikum.paperless.model.DocumentType.INVOICE);
        updated.setFilename("new-name.pdf");

        Document result = documentService.update(7L, updated);

        assertThat(result).isSameAs(document);
        assertThat(document.getDocumentType()).isEqualTo(at.technikum.paperless.model.DocumentType.INVOICE);
        assertThat(document.getFilename()).isEqualTo("new-name.pdf");
    }

    @Test
    void deleteByIdDeletesAndPublishesDocumentDeletedEvent() {
        document.setStorageKey("storage-key");
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        documentService.deleteById(7L);

        verify(documentRepository).deleteById(7L);
        ArgumentCaptor<at.technikum.paperless.messaging.DocumentDeletedEvent> captor =
                ArgumentCaptor.forClass(at.technikum.paperless.messaging.DocumentDeletedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().documentId()).isEqualTo("7");
        assertThat(captor.getValue().storageKey()).isEqualTo("storage-key");
    }

    @Test
    void searchReturnsAllByUserWhenQueryBlank() {
        when(documentRepository.findAllByUserIdOrderByUploadDateDesc(3L)).thenReturn(List.of(document));

        assertThat(documentService.search(3L, "  ")).containsExactly(document);
        verify(documentSearchService, never()).searchDocumentIds(anyString());
    }

    @Test
    void searchReturnsEmptyListWhenSearchYieldsNoIds() {
        when(documentSearchService.searchDocumentIds("bayern")).thenReturn(List.of());

        assertThat(documentService.search(3L, "bayern")).isEmpty();
    }

    @Test
    void searchReturnsMatchingDocumentsOfUser() {
        Document docA = document(7L, 3L);
        Document docB = document(8L, 3L);
        Document otherUserDoc = document(9L, 4L);

        when(documentSearchService.searchDocumentIds("bayern")).thenReturn(List.of(7L, 8L, 9L));
        when(documentRepository.findAllById(List.of(7L, 8L, 9L)))
                .thenReturn(List.of(docA, docB, otherUserDoc));

        assertThat(documentService.search(3L, "bayern")).containsExactly(docA, docB);
    }

    @Test
    void saveSummaryStoresSummaryOnDocument() {
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document result = documentService.saveSummary(7L, "Eine kurze Zusammenfassung.");

        assertThat(result.getSummary()).isEqualTo("Eine kurze Zusammenfassung.");
        verify(documentRepository).save(document);
    }

    @Test
    void saveSummaryThrowsWhenDocumentDoesNotExist() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.saveSummary(99L, "Zusammenfassung"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(documentRepository, never()).save(document);
    }
}
