package at.technikum.paperless.controller;

import at.technikum.paperless.dto.in.AuthCreate;
import at.technikum.paperless.dto.out.AuthPublic;
import at.technikum.paperless.dto.out.UserLoginPublic;
import at.technikum.paperless.service.IAuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginPublic> login(
            @RequestBody @Valid AuthCreate authIn) {

        AuthPublic auth = authService.login(authIn);

        ResponseCookie cookie = ResponseCookie
                .from("accessToken", auth.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(auth.getUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
