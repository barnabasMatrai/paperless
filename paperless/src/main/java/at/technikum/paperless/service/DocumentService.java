package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.exception.InvalidFileException;
import at.technikum.paperless.messaging.OcrMessagePublisher;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IUserRepository;
import at.technikum.paperless.storage.IFileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import at.technikum.paperless.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class DocumentService implements IDocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "docx", "jpg", "jpeg", "png");

    private final IDocumentRepository documentRepository;
    private final IUserRepository userRepository;
    private final IFileStorageService fileStorageService;
    private final OcrMessagePublisher ocrMessagePublisher;

    public List<Document> findAll() {
        log.debug("Fetching all documents");

        List<Document> documents = documentRepository.findAll();

        log.debug("Retrieved {} documents", documents.size());
        return documents;
    }

    public List<Document> findAllByUserId(Long userId) {
        log.debug("Fetching documents for user with ID {}", userId);

        List<Document> documents =
                documentRepository.findAllByUserIdOrderByUploadDateDesc(userId);

        log.debug("Retrieved {} documents for user with ID {}",
                documents.size(), userId);
        return documents;
    }

    public Document findById(Long id) {
        log.debug("Fetching document with ID {}", id);

        return documentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Document with ID {} was not found", id);
                    return new ResourceNotFoundException("Document", id);
                });
    }

    @Transactional
    public Document upload(Long userId, Document document, MultipartFile file) {
        log.info("Starting document upload for user with ID {}", userId);

        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot upload document: user with ID {} was not found",
                            userId);
                    return new ResourceNotFoundException("User", userId);
                });

        log.debug("Storing uploaded file");
        fileStorageService.store(file);

        document.setUser(user);

        Document savedDocument = documentRepository.save(document);
        log.info("Document with ID {} saved successfully for user with ID {}",
                savedDocument.getId(), userId);

        log.debug("Sending document with ID {} for OCR processing",
                savedDocument.getId());
        ocrMessagePublisher.sendDocumentForOcr(savedDocument.getId());

        log.info("Document with ID {} submitted for OCR processing",
                savedDocument.getId());

        return savedDocument;
    }

    public Document update(Long id, Document updatedDocument) {
        log.info("Updating document with ID {}", id);

        Document existingDocument = findById(id);

        existingDocument.setDocumentType(updatedDocument.getDocumentType());
        existingDocument.setFilename(updatedDocument.getFilename());

        Document savedDocument = documentRepository.save(existingDocument);

        log.info("Document with ID {} updated successfully", id);
        return savedDocument;
    }

    public void deleteById(Long id) {
        log.info("Deleting document with ID {}", id);

        documentRepository.deleteById(id);

        log.info("Document with ID {} deleted successfully", id);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Document upload rejected: file is null or empty");
            throw new InvalidFileException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT)
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Document upload rejected: unsupported file extension '{}'",
                    extension);
            throw new InvalidFileException(
                    "Only PDF, DOCX, JPG or PNG files are allowed");
        }

        log.debug("File validation successful for extension '{}'", extension);
    }
}
