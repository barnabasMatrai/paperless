package at.technikum.paperless.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioFileStorageServiceTest {

    @Mock
    private MinioClient minioClient;
    @Mock
    private MinioProperties properties;

    private MinioFileStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new MinioFileStorageService(minioClient, properties);
        lenient().when(properties.getBucket()).thenReturn("documents");
    }

    @Test
    void storePutsObjectToMinioAndReturnsKey() throws Exception {
        FileUpload file = new FileUpload("test.pdf", "application/pdf", 5L,
                new ByteArrayInputStream("hello".getBytes()));

        String key = storage.store(file);

        assertThat(key).isNotBlank();

        ArgumentCaptor<io.minio.PutObjectArgs> captor = ArgumentCaptor.forClass(io.minio.PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("documents");
        assertThat(captor.getValue().object()).isEqualTo(key);
        assertThat(captor.getValue().object()).endsWith(".pdf");
        assertThat(captor.getValue().contentType()).isEqualTo("application/pdf");
    }

    @Test
    void storeUsesDefaultContentTypeWhenNoneGiven() throws Exception {
        FileUpload file = new FileUpload("test.pdf", null, 5L,
                new ByteArrayInputStream("hello".getBytes()));

        storage.store(file);

        ArgumentCaptor<io.minio.PutObjectArgs> captor = ArgumentCaptor.forClass(io.minio.PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertThat(captor.getValue().contentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void deleteRemovesObjectFromMinio() throws Exception {
        storage.delete("some-key");

        ArgumentCaptor<io.minio.RemoveObjectArgs> captor = ArgumentCaptor.forClass(io.minio.RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());
        assertThat(captor.getValue().object()).isEqualTo("some-key");
        assertThat(captor.getValue().bucket()).isEqualTo("documents");
    }

    @Test
    void deleteIgnoresBlankStorageKey() throws Exception {
        storage.delete("");

        verify(minioClient, org.mockito.Mockito.never()).removeObject(any(io.minio.RemoveObjectArgs.class));
    }
}
