package org.example.paperless.service;

import org.example.paperless.entity.Document;

import java.util.List;

public interface IDocumentService {

    List<Document> findAll();

    Document findById(Long id);

    Document save(Document document);

    Document update(Long id, Document updatedDocument);

    void deleteById(Long id);
}
