package at.technikum.paperless.service;

import at.technikum.paperless.dto.in.UserRegisterCreate;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.exception.UsernameAlreadyExistsException;
import at.technikum.paperless.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements  IUserService {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(UserRegisterCreate userRegister) {
        if (userRepository.existsByUsername(userRegister.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(userRegister.getUsername());
        user.setPasswordHash(passwordEncoder.encode(userRegister.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll().stream()
                .toList();
    }

    public User get(long id) {
        return userRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public User update(long id, UserRegisterCreate userIn) {
        User user = userRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        if (!user.getUsername().equals(userIn.getUsername())
                && userRepository.existsByUsername(userIn.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }

        user.setUsername(userIn.getUsername());
        user.setPasswordHash(
                passwordEncoder.encode(userIn.getPassword())
        );

        return userRepository.save(user);
    }

    public void delete(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        userRepository.delete(user);
    }
}
