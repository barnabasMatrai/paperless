package org.example.paperless.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.paperless.dto.in.DocumentCreate;
import org.example.paperless.dto.out.DocumentPublic;
import org.example.paperless.entity.Document;
import org.example.paperless.mapper.DocumentMapper;
import org.example.paperless.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final DocumentMapper documentMapper;

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
    public ResponseEntity<DocumentPublic> create(@Valid @RequestBody DocumentCreate documentIn) {
        Document saved = documentService.save(documentMapper.toEntity(documentIn));
        DocumentPublic result = documentMapper.toObject(saved);
        return ResponseEntity.created(URI.create("/api/documents/" + result.getId())).body(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}