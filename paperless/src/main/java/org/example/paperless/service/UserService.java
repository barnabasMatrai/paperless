package org.example.paperless.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.paperless.dto.in.UserRegisterCreate;
import org.example.paperless.dto.out.UserPublic;
import org.example.paperless.entity.User;
import org.example.paperless.mapper.IUserMapper;
import org.example.paperless.repository.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final IUserMapper userMapper;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserPublic register(UserRegisterCreate userRegister) {
        /*if (userRepository.existsByUsername(userRegister.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }*/

        User user = new User();
        user.setUsername(userRegister.getUsername());
        //user.setEmail(userRegister.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userRegister.getPassword()));
        User saved = userRepository.save(user);

        return userMapper.toObject(saved);
    }

    public List<UserPublic> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toObject)
                .toList();
    }

    public UserPublic get(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        return userMapper.toObject(user);
    }
}
