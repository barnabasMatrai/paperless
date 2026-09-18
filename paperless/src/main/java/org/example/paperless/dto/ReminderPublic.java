package org.example.paperless.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderPublic {
    private Long Id;
    private LocalDateTime dueDate;
    private Boolean notified;
    private String description;
}
