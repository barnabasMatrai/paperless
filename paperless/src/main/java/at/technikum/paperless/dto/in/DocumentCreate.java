package at.technikum.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import at.technikum.paperless.model.DocumentType;

import java.time.Instant;

@Data
public class DocumentCreate {
    @NotBlank
    @Size(max = 255, message = "Filename must not exceed 255 characters")
    private String filename;

    @NotNull
    private DocumentType type;

    @NotNull
    private Instant uploadDate;
}