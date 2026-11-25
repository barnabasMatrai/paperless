package at.technikum.paperless.controller;

import at.technikum.paperless.dto.in.DocumentCreate;
import at.technikum.paperless.dto.in.DocumentUpdateCreate;
import at.technikum.paperless.dto.in.DocumentUploadCreate;
import at.technikum.paperless.dto.out.DocumentPublic;
import at.technikum.paperless.entity.Document;
import at.technikum.paperless.exception.InvalidFileException;
import at.technikum.paperless.mapper.IDocumentMapper;
import at.technikum.paperless.service.IDocumentService;
import at.technikum.paperless.storage.FileUpload;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@PreAuthorize("isAuthenticated()")
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/documents")
@SecurityRequirement(name = "bearerAuth")
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
            Authentication authentication) throws IOException {
        validateFile(file);

        Long userId = Long.valueOf(authentication.getName());

        Document document = documentMapper.toEntity(metadata);
        Document saved = documentService.upload(userId, document,
                new FileUpload(file.getOriginalFilename(), file.getContentType(),
                        file.getSize(), file.getInputStream()));
        return documentMapper.toObject(saved);
    }

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "docx", "jpg", "jpeg", "png");

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Document upload rejected: file is null or empty");
            throw new InvalidFileException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1)
                  .toLowerCase(Locale.ROOT)
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Document upload rejected: unsupported file extension '{}'",
                    extension);
            throw new InvalidFileException(
                    "Only PDF, DOCX, JPG or PNG files are allowed");
        }

        log.debug("File validation successful for extension '{}'", extension);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@documentSecurity.isOwner(#id, authentication)")
    public DocumentPublic update(@PathVariable Long id, @Valid @RequestBody DocumentUpdateCreate documentIn) {
        Document updated = documentService.update(id, documentMapper.toEntity(documentIn));
        return documentMapper.toObject(updated);
    }

    @GetMapping("/search")
    public List<DocumentPublic> search(@RequestParam("q") String query, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        return documentService.search(userId, query).stream()
                .map(documentMapper::toObject)
                .toList();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@documentSecurity.isOwner(#id, authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        documentService.deleteById(id);
    }
}
