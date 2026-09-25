package org.example.paperless.dto.out;

import lombok.Data;
import org.example.paperless.dto.in.DocumentCreate;

import java.util.List;

@Data
public class UserPublic {
    private int id;
    private String username;
    //private String email;
    private String passwordHash;
    private List<DocumentCreate> documents;
}
