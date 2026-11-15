package at.technikum.paperless.dto.out;
import lombok.Data;
import at.technikum.paperless.model.DocumentType;

import java.time.Instant;

@Data
public class DocumentPublic {
    private Long id;
    private String filename;
    private DocumentType type;
    private Instant uploadDate;
    private ReminderPublic reminder;
}
