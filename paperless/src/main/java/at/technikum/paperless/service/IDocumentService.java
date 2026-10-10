package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {

    List<Document> findAll();

    List<Document> findAllByUserId(Long userId);

    Document findById(Long id);

    Document upload(Long userId, Document document, MultipartFile file);

    Document update(Long id, Document updatedDocument);

    void deleteById(Long id);
}
