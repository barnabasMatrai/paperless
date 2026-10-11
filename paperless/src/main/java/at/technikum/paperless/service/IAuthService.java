package at.technikum.paperless.service;

import at.technikum.paperless.dto.in.AuthCreate;
import at.technikum.paperless.dto.out.AuthPublic;
import at.technikum.paperless.dto.out.UserLoginPublic;

public interface IAuthService {
    AuthPublic login(AuthCreate authIn);
    UserLoginPublic getCurrentUser(Long userId);
}
