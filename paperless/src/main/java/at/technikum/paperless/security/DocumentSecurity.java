package at.technikum.paperless.security;

import at.technikum.paperless.service.IDocumentService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("documentSecurity")
@AllArgsConstructor
public class DocumentSecurity {

    private final IDocumentService documentService;

    public boolean isOwner(Long documentId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        return documentService.findById(documentId)
                .getUser()
                .getId()
                .equals(userId);
    }
}
