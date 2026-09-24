package org.example.paperless.service;

import org.example.paperless.entity.Document;
import org.example.paperless.repository.IDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DocumentService implements IDocumentService {

    private final IDocumentRepository documentRepository;

    public DocumentService(IDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Document not found: " + id));
    }

    public Document save(Document document) {
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
