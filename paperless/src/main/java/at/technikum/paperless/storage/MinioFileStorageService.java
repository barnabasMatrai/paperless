package at.technikum.paperless.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements IFileStorageService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MinioClient minioClient;
    private final MinioProperties properties;

    private volatile boolean bucketReady = false;

    @Override
    public String store(MultipartFile file) {
        String key = UUID.randomUUID() + extensionOf(file.getOriginalFilename());
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : DEFAULT_CONTENT_TYPE;

        try (InputStream in = file.getInputStream()) {
            ensureBucketExists();

            // Entspricht uploadObject(...) aus den Folien, nur mit Stream statt lokalem Pfad,
            // weil der Upload als MultipartFile im Speicher ankommt.
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(key)
                    .stream(in, file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            log.info("Datei in MinIO gespeichert: bucket={}, key={}, name={}, size={} bytes",
                    properties.getBucket(), key, file.getOriginalFilename(), file.getSize());
            return key;

        } catch (Exception e) {
            throw new FileStorageException(
                    "Datei '" + file.getOriginalFilename() + "' konnte nicht in MinIO gespeichert werden", e);
        }
    }

    // Bucket wird beim ersten Upload angelegt, nicht beim Start,
    // damit die App nicht abstürzt, wenn MinIO noch hochfährt.
    private void ensureBucketExists() throws Exception {
        if (bucketReady) {
            return;
        }
        String bucket = properties.getBucket();
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("MinIO-Bucket '{}' angelegt", bucket);
        }
        bucketReady = true;
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
}
