package at.technikum.paperless.service;

import at.technikum.paperless.entity.Document;
import at.technikum.paperless.storage.FileUpload;

import java.util.List;

public interface IDocumentService {

    List<Document> findAll();

    List<Document> findAllByUserId(Long userId);

    Document findById(Long id);

    Document upload(Long userId, Document document, FileUpload file);

    Document update(Long id, Document updatedDocument);

    Document saveSummary(Long id, String summary);

    List<Document> search(Long userId, String query);

    void deleteById(Long id);


}
