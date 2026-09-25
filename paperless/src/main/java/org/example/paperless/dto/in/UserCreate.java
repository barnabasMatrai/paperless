package org.example.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;

public class UserCreate {
    @NotBlank
    private String username;

    @NotBlank
    //private String email;

    @NotBlank
    private String passwordHash;
}
