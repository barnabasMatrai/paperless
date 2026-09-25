package org.example.paperless.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.paperless.dto.in.UserRegisterCreate;
import org.example.paperless.dto.out.UserPublic;
import org.example.paperless.entity.User;

import java.util.List;

public interface IUserService {
    public UserPublic register(UserRegisterCreate userRegister);

    public List<UserPublic> getAll();

    public UserPublic get(long id);
}
