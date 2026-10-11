package at.technikum.paperless.controller;

import at.technikum.paperless.dto.in.DocumentCreate;
import at.technikum.paperless.dto.in.DocumentUploadCreate;
import at.technikum.paperless.dto.out.DocumentPublic;
import at.technikum.paperless.entity.Document;
import at.technikum.paperless.mapper.IDocumentMapper;
import at.technikum.paperless.service.IDocumentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
@RequestMapping("/api/documents")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin
public class DocumentController {
    private final IDocumentService documentService;
    private final IDocumentMapper documentMapper;

    @GetMapping
    public List<DocumentPublic> getAll(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        return documentService.findAllByUserId(userId).stream()
                .map(documentMapper::toObject)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@documentSecurity.isOwner(#id, authentication)")
    public DocumentPublic getById(@PathVariable Long id) {
        return documentMapper.toObject(documentService.findById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentPublic upload(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("metadata") DocumentUploadCreate metadata,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        Document document = documentMapper.toEntity(metadata);
        Document saved = documentService.upload(userId, document, file);
        return documentMapper.toObject(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@documentSecurity.isOwner(#id, authentication)")
    public DocumentPublic update(@PathVariable Long id, @Valid @RequestBody DocumentCreate documentIn) {
        Document updated = documentService.update(id, documentMapper.toEntity(documentIn));
        return documentMapper.toObject(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@documentSecurity.isOwner(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        documentService.deleteById(id);
    }
}
