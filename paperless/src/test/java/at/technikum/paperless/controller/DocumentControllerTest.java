package at.technikum.paperless.controller;

import at.technikum.paperless.dto.in.DocumentUpdateCreate;
import at.technikum.paperless.dto.in.DocumentUploadCreate;
import at.technikum.paperless.dto.out.DocumentPublic;
import at.technikum.paperless.entity.Document;
import at.technikum.paperless.exception.InvalidFileException;
import at.technikum.paperless.mapper.IDocumentMapper;
import at.technikum.paperless.service.IDocumentService;
import at.technikum.paperless.storage.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private IDocumentService documentService;
    @Mock
    private IDocumentMapper documentMapper;

    private DocumentController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("5");
        controller = new DocumentController(documentService, documentMapper);
    }

    @Test
    void uploadStoresFileAndReturnsPublicDocument() throws IOException {
        byte[] content = "hello world!".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                "application/pdf", content);
        DocumentUploadCreate metadata = new DocumentUploadCreate();
        metadata.setFilename("test.pdf");

        Document document = new Document();
        document.setId(7L);
        DocumentPublic publicDoc = new DocumentPublic();
        publicDoc.setId(7L);

        when(documentMapper.toEntity(metadata)).thenReturn(document);
        when(documentService.upload(eq(5L), eq(document), any(FileUpload.class))).thenReturn(document);
        when(documentMapper.toObject(document)).thenReturn(publicDoc);

        DocumentPublic result = controller.upload(file, metadata, authentication);

        assertThat(result).isSameAs(publicDoc);
        ArgumentCaptor<FileUpload> captor = ArgumentCaptor.forClass(FileUpload.class);
        verify(documentService).upload(eq(5L), eq(document), captor.capture());
        assertThat(captor.getValue().filename()).isEqualTo("test.pdf");
        assertThat(captor.getValue().contentType()).isEqualTo("application/pdf");
        assertThat(captor.getValue().size()).isEqualTo(content.length);
        assertThat(captor.getValue().content()).isNotNull();
    }

    @Test
    void uploadRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                "application/pdf", new byte[0]);

        assertThatThrownBy(() -> controller.upload(file, new DocumentUploadCreate(), authentication))
                .isInstanceOf(InvalidFileException.class);
        verifyNoInteractions(documentService);
    }

    @Test
    void uploadRejectsInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "test.exe",
                "application/octet-stream", "data".getBytes());

        assertThatThrownBy(() -> controller.upload(file, new DocumentUploadCreate(), authentication))
                .isInstanceOf(InvalidFileException.class);
        verifyNoInteractions(documentService);
    }

    @Test
    void getAllReturnsPublicDocuments() {
        Document doc = new Document();
        DocumentPublic publicDoc = new DocumentPublic();
        when(documentService.findAllByUserId(5L)).thenReturn(List.of(doc));
        when(documentMapper.toObject(doc)).thenReturn(publicDoc);

        assertThat(controller.getAll(authentication)).containsExactly(publicDoc);
        verify(documentService).findAllByUserId(5L);
    }

    @Test
    void getByIdReturnsPublicDocument() {
        Document doc = new Document();
        when(documentService.findById(7L)).thenReturn(doc);
        when(documentMapper.toObject(doc)).thenReturn(new DocumentPublic());

        assertThat(controller.getById(7L)).isNotNull();
        verify(documentService).findById(7L);
    }

    @Test
    void updateReturnsPublicDocument() {
        DocumentUpdateCreate in = new DocumentUpdateCreate();
        Document doc = new Document();
        when(documentMapper.toEntity(in)).thenReturn(doc);
        when(documentService.update(7L, doc)).thenReturn(doc);
        when(documentMapper.toObject(doc)).thenReturn(new DocumentPublic());

        assertThat(controller.update(7L, in)).isNotNull();
        verify(documentService).update(7L, doc);
    }

    @Test
    void searchDelegatesToService() {
        Document doc = new Document();
        when(documentService.search(5L, "bayern")).thenReturn(List.of(doc));
        when(documentMapper.toObject(doc)).thenReturn(new DocumentPublic());

        assertThat(controller.search("bayern", authentication)).hasSize(1);
        verify(documentService).search(5L, "bayern");
    }

    @Test
    void deleteDelegatesToService() {
        controller.delete(7L);

        verify(documentService).deleteById(7L);
    }
}
