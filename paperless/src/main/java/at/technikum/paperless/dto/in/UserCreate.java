package at.technikum.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;

public class UserCreate {
    @NotBlank
    private String username;

    @NotBlank
    private String passwordHash;
}
