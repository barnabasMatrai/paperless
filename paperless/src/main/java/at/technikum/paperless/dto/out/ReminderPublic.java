package at.technikum.paperless.dto.out;
import lombok.Data;

import java.time.Instant;

@Data
public class ReminderPublic {
    private Long id;
    private Instant dueDate;
    private Boolean notified;
    private String description;
}
