package org.example.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderCreate {
    @NotNull
    private LocalDateTime dueDate;

    private boolean notified;

    @NotBlank
    private String description;
}