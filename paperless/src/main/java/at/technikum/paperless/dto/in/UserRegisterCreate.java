package at.technikum.paperless.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterCreate {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
