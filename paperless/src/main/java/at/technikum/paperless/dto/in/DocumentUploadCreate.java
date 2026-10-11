package at.technikum.paperless.dto.in;

import at.technikum.paperless.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentUploadCreate {
    @NotBlank
    private String filename;

    @NotNull
    private DocumentType type;
}
