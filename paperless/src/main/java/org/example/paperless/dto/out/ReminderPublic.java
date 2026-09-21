package org.example.paperless.dto.out;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderPublic {
    private Long id;
    private LocalDateTime dueDate;
    private Boolean notified;
    private String description;
}
