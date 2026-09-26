package at.technikum.paperless.service;

import at.technikum.paperless.dto.in.AuthCreate;
import at.technikum.paperless.dto.out.AuthPublic;

public interface IAuthService {
    AuthPublic login(AuthCreate authIn);
}
