package at.technikum.paperless.dto.in;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthCreate {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
