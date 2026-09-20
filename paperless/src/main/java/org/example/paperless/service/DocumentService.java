package org.example.paperless.service;

import org.example.paperless.entity.Document;
import org.example.paperless.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
    }

    public Document save(Document document) {
        // Rückreferenz setzen, sonst schlägt der NOT NULL-Constraint auf document_id fehl
        if (document.getReminder() != null) {
            document.getReminder().setDocument(document);
        }
        return documentRepository.save(document);
    }

    public void deleteById(Long id) {
        documentRepository.deleteById(id);
    }
}
