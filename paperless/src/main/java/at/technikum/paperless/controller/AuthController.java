package at.technikum.paperless.controller;

import at.technikum.paperless.dto.in.AuthCreate;
import at.technikum.paperless.dto.out.AuthPublic;
import at.technikum.paperless.service.IAuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public AuthPublic login(@RequestBody @Valid AuthCreate authIn) {
        return authService.login(authIn);
    }
}
