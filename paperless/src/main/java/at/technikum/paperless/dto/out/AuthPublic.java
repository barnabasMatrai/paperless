package at.technikum.paperless.dto.out;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthPublic {
    private String accessToken;
    private UserLoginPublic user;
}
