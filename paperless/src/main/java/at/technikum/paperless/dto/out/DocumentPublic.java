package at.technikum.paperless.dto.out;
import lombok.Data;
import at.technikum.paperless.model.DocumentType;

import java.time.LocalDateTime;

@Data
public class DocumentPublic {
    private Long id;
    private String filename;
    private DocumentType type;
    private LocalDateTime uploadDate;
    private ReminderPublic reminder;
}
