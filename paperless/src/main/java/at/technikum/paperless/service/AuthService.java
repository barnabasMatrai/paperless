package at.technikum.paperless.service;

import at.technikum.paperless.dto.in.AuthCreate;
import at.technikum.paperless.dto.out.AuthPublic;
import at.technikum.paperless.dto.out.UserLoginPublic;
import at.technikum.paperless.entity.User;
import at.technikum.paperless.exception.InvalidCredentialsException;
import at.technikum.paperless.mapper.IUserMapper;
import at.technikum.paperless.repository.IUserRepository;
import at.technikum.paperless.security.JwtIssuer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService implements IAuthService {

    private final IUserMapper userMapper;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    public AuthPublic login(AuthCreate authIn) {
        log.info("Login attempt for username '{}'", authIn.getUsername());

        User user = userRepository.findByUsername(authIn.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: username '{}' not found",
                            authIn.getUsername());
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(
                authIn.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: invalid credentials for username '{}'",
                    authIn.getUsername());
            throw new InvalidCredentialsException();
        }

        log.debug("Credentials validated for user ID {}", user.getId());

        String token = jwtIssuer.issue(user.getId(), List.of("USER"));
        UserLoginPublic userLogin = userMapper.toLoginObject(user);
        AuthPublic auth = new AuthPublic(token, userLogin);

        log.info("Login successful for user ID {}", user.getId());

        return auth;
    }

    @Override
    public UserLoginPublic getCurrentUser(Long userId) {
        log.debug("Fetching current user with ID {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Current user not found for ID {}", userId);
                    return new UsernameNotFoundException("User not found");
                });

        UserLoginPublic currentUser = userMapper.toLoginObject(user);

        log.debug("Successfully retrieved current user with ID {}", userId);

        return currentUser;
    }
}
