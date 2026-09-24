package org.example.paperless.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.paperless.dto.in.DocumentCreate;
import org.example.paperless.dto.out.DocumentPublic;
import org.example.paperless.entity.Document;
import org.example.paperless.mapper.IDocumentMapper;
import org.example.paperless.service.IDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {
    private final IDocumentService documentService;
    private final IDocumentMapper documentMapper;

    @GetMapping
    public List<DocumentPublic> getAll() {
        return documentService.findAll().stream()
                .map(documentMapper::toObject)
                .toList();
    }

    @GetMapping("/{id}")
    public DocumentPublic getById(@PathVariable Long id) {
        return documentMapper.toObject(documentService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentPublic create(@Valid @RequestBody DocumentCreate documentIn) {
        Document saved = documentService.save(documentMapper.toEntity(documentIn));
        return documentMapper.toObject(saved);
    }

    @PutMapping("/{id}")
    public DocumentPublic update(@PathVariable Long id, @Valid @RequestBody DocumentCreate documentIn) {
        Document updated = documentService.update(id, documentMapper.toEntity(documentIn));
        return documentMapper.toObject(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        documentService.deleteById(id);
    }
}