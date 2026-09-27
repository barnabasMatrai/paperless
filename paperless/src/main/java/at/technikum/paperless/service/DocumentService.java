package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.repository.IDocumentRepository;
import at.technikum.paperless.repository.IUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class DocumentService implements IDocumentService {

    private final IDocumentRepository documentRepository;
    private final IUserRepository userRepository;

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Document not found: " + id));
    }

    public Document save(Long userId, Document document) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "User not found: " + userId));

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
}
