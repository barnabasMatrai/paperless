package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.exception.InvalidFileException;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IUserRepository;
import at.technikum.paperless.storage.IFileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@AllArgsConstructor
public class DocumentService implements IDocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "jpg", "jpeg", "png");

    private final IDocumentRepository documentRepository;
    private final IUserRepository userRepository;
    private final IFileStorageService fileStorageService;

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public List<Document> findAllByUserId(Long userId) {
        return documentRepository.findAllByUserIdOrderByUploadDateDesc(userId);
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Document not found: " + id));
    }

    public Document upload(Long userId, Document document, MultipartFile file) {
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "User not found: " + userId));

        // TODO Sprint 3: zurückgegebenen Schlüssel (MinIO) am Dokument speichern
        fileStorageService.store(file);

        document.setUser(user);

        return documentRepository.save(document);
    }

    public Document update(Long id, Document updatedDocument) {
        Document existingDocument = findById(id);

        existingDocument.setDocumentType(updatedDocument.getDocumentType());
        existingDocument.setFilename(updatedDocument.getFilename());

        return documentRepository.save(existingDocument);
    }

    public void deleteById(Long id) {
        documentRepository.deleteById(id);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("Only PDF, DOCX, JPG or PNG files are allowed");
        }
    }
}
