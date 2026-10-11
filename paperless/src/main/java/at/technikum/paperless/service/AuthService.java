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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthService implements IAuthService {

    private final IUserMapper userMapper;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    public AuthPublic login(AuthCreate authIn) {
        User user = userRepository.findByUsername(authIn.getUsername())
                .orElseThrow(() -> {
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(authIn.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtIssuer.issue(user.getId(), List.of("USER"));
        UserLoginPublic userLogin = userMapper.toLoginObject(user);
        AuthPublic auth = new AuthPublic(token, userLogin);

        return auth;
    }

    @Override
    public UserLoginPublic getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return userMapper.toLoginObject(user);
    }
}
