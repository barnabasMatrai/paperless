package at.technikum.paperless.dto.out;

import lombok.Data;
import at.technikum.paperless.dto.in.DocumentCreate;

import java.util.List;

@Data
public class UserPublic {
    private int id;
    private String username;
    private List<DocumentCreate> documents;
}
