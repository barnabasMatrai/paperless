package at.technikum.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import at.technikum.paperless.model.DocumentType;

import java.time.Instant;

@Data
public class DocumentCreate {
    @NotBlank
    private String filename;

    @NotNull
    private DocumentType type;

    @NotNull
    private Instant uploadDate;
}