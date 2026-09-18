package org.example.paperless.dto;
import lombok.Data;
import org.example.paperless.model.DocumentType;

import java.time.LocalDateTime;

@Data
public class DocumentPublic {
    private Long id;
    private String filename;
    private DocumentType type;
    private LocalDateTime uploadDate;
    private ReminderPublic reminder;
}
