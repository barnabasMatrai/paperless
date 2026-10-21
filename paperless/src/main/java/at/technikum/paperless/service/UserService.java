package at.technikum.paperless.service;

import at.technikum.paperless.dto.in.UserRegisterCreate;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.exception.UsernameAlreadyExistsException;
import at.technikum.paperless.repository.IUserRepository;
import at.technikum.paperless.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(UserRegisterCreate userRegister) {
        log.info("Registering new user with username '{}'",
                userRegister.getUsername());

        if (userRepository.existsByUsername(userRegister.getUsername())) {
            log.warn("Registration failed: username '{}' already exists",
                    userRegister.getUsername());
            throw new UsernameAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(userRegister.getUsername());
        user.setPasswordHash(
                passwordEncoder.encode(userRegister.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID {}", savedUser.getId());

        return savedUser;
    }

    public List<User> getAll() {
        log.debug("Fetching all users");

        List<User> users = userRepository.findAll().stream()
                .toList();

        log.debug("Retrieved {} users", users.size());

        return users;
    }

    public User get(long id) {
        log.debug("Fetching user with ID {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with ID {} was not found", id);
                    return new ResourceNotFoundException("User", id);
                });
    }

    public User update(long id, UserRegisterCreate userIn) {
        log.info("Updating user with ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update user: ID {} was not found", id);
                    return new ResourceNotFoundException("User", id);
                });

        if (!user.getUsername().equals(userIn.getUsername())
                && userRepository.existsByUsername(userIn.getUsername())) {
            log.warn("User update failed: username '{}' is already taken",
                    userIn.getUsername());
            throw new UsernameAlreadyExistsException();
        }

        user.setUsername(userIn.getUsername());
        user.setPasswordHash(
                passwordEncoder.encode(userIn.getPassword()));

        User updatedUser = userRepository.save(user);

        log.info("User with ID {} updated successfully", id);

        return updatedUser;
    }

    public void delete(long id) {
        log.info("Deleting user with ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete user: ID {} was not found", id);
                    return new ResourceNotFoundException("User", id);
                });

        userRepository.delete(user);

        log.info("User with ID {} deleted successfully", id);
    }
}
