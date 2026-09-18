package org.example.paperless.controller;

import lombok.AllArgsConstructor;
import org.example.paperless.dto.out.DocumentPublic;
import org.example.paperless.mapper.DocumentMapper;
import org.example.paperless.service.DocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
