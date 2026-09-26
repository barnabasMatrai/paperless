package at.technikum.paperless.service;

import at.technikum.paperless.dto.in.UserRegisterCreate;
import at.technikum.paperless.entity.User;

import java.util.List;

public interface IUserService {
    User register(UserRegisterCreate userRegister);
    List<User> getAll();
    User get(long id);
    User update(long id, UserRegisterCreate userIn);
    void delete(long id);
}
