package org.example.paperless.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.paperless.dto.in.UserRegisterCreate;
import org.example.paperless.dto.out.UserPublic;
import org.example.paperless.service.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserPublic create(@RequestBody @Valid UserRegisterCreate userRegister) {
        return userService.register(userRegister);
    }

    @GetMapping("/{id}")
    public UserPublic read(@PathVariable int id) {
        return userService.get(id);
    }

    @GetMapping
    public List<UserPublic> readAll() {
        return userService.getAll();
    }
}
