package at.technikum.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class ReminderCreate {
    @NotNull
    private Long documentId;

    @NotNull
    private Instant dueDate;

    private boolean notified;

    @NotBlank
    private String description;
}