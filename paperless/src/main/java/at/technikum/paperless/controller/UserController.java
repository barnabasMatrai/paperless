package at.technikum.paperless.controller;

import at.technikum.paperless.entity.User;
import at.technikum.paperless.mapper.IUserMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import at.technikum.paperless.dto.in.UserRegisterCreate;
import at.technikum.paperless.dto.out.UserPublic;
import at.technikum.paperless.service.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    private final IUserService userService;
    private final IUserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserPublic create(@RequestBody @Valid UserRegisterCreate userRegister) {
        User registeredUser = userService.register(userRegister);
        return userMapper.toObject(registeredUser);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UserPublic read(@PathVariable int id) {
        User user = userService.get(id);
        return userMapper.toObject(user);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<UserPublic> readAll() {
        List<User> users = userService.getAll();
        return users.stream()
                .map(userMapper::toObject)
                .toList();
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UserPublic update(
            @PathVariable Long id,
            @Valid @RequestBody UserRegisterCreate userIn) {

        return userMapper.toObject(
                userService.update(id, userIn)
        );
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
