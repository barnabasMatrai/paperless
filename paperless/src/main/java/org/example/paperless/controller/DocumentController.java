package org.example.paperless.controller;

import org.example.paperless.dto.out.DocumentPublic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    @GetMapping
    public List<DocumentPublic> getAll() {
        return
    }
}
