package at.technikum.paperless;

import at.technikum.paperless.config.SecurityConfig;
import at.technikum.paperless.dto.in.AuthCreate;
import at.technikum.paperless.dto.in.UserRegisterCreate;
import at.technikum.paperless.dto.out.AuthPublic;
import at.technikum.paperless.dto.out.UserLoginPublic;
import at.technikum.paperless.dto.out.UserPublic;
import at.technikum.paperless.search.OcrDocumentRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@Import(SecurityConfig.class)
public class UserControllerTests {

    @MockitoBean
    private OcrDocumentRepository ocrDocumentRepository;

    @MockitoBean
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Test
    void get_users_without_token_returns_unauthorized(
            @Autowired WebTestClient webTestClient) {

        webTestClient
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void post_register_valid_user_returns_created(
            @Autowired WebTestClient webTestClient) {

        UserRegisterCreate body = new UserRegisterCreate();
        body.setUsername("TestUser");
        body.setPassword("secret");

        UserPublic result = webTestClient
                .post()
                .uri("/api/users")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(UserPublic.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUsername()).isEqualTo("TestUser");
    }

    @Test
    void post_register_does_not_return_password_hash(
            @Autowired WebTestClient webTestClient) {

        UserRegisterCreate body = new UserRegisterCreate();
        body.setUsername("NoPasswordUser");
        body.setPassword("secret");

        webTestClient
                .post()
                .uri("/api/users")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.username")
                .isEqualTo("NoPasswordUser")
                .jsonPath("$.passwordHash")
                .doesNotExist();
    }

    @Test
    void post_login_invalid_credentials_returns_unauthorized(
            @Autowired WebTestClient webTestClient) {

        AuthCreate body = new AuthCreate();
        body.setUsername("NonExistingUser");
        body.setPassword("invalid");

        webTestClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void post_login_valid_credentials_returns_access_token(
            @Autowired WebTestClient webTestClient) {

        createUser(webTestClient, "LoginUser", "secret");

        AuthCreate body = new AuthCreate();
        body.setUsername("LoginUser");
        body.setPassword("secret");

        webTestClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.SET_COOKIE)
                .expectBody(UserLoginPublic.class)
                .value(user -> {
                    Assertions.assertThat(user).isNotNull();
                    Assertions.assertThat(user.getUsername())
                            .isEqualTo("LoginUser");
                });
    }

    @Test
    void get_users_with_valid_token_returns_users(
            @Autowired WebTestClient webTestClient) {

        String token = createUserAndLogin(
                webTestClient,
                "Alice",
                "secret"
        );

        WebTestClient.RequestHeadersUriSpec<?> request =
                webTestClient.get();

        WebTestClient.RequestHeadersSpec<?> requestWithUri =
                request.uri("/api/users");

        requestWithUri.header(
                "Authorization",
                "Bearer " + token
        );

        WebTestClient.ResponseSpec response =
                requestWithUri.exchange();

        response
                .expectStatus()
                .isOk();

        List<UserPublic> result = response
                .expectBodyList(UserPublic.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(result)
                .isNotNull();

        Assertions.assertThat(result)
                .extracting(UserPublic::getUsername)
                .contains("Alice");
    }

    @Test
    void get_users_with_invalid_token_returns_unauthorized(
            @Autowired WebTestClient webTestClient) {

        WebTestClient.RequestHeadersUriSpec<?> request =
                webTestClient.get();

        WebTestClient.RequestHeadersSpec<?> requestWithUri =
                request.uri("/api/users");

        requestWithUri.header(
                "Authorization",
                "Bearer invalid-token"
        );

        WebTestClient.ResponseSpec response =
                requestWithUri.exchange();

        response
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void get_users_with_malformed_authorization_header_returns_unauthorized(
            @Autowired WebTestClient webTestClient) {

        var request = webTestClient.get();

        var requestWithUri = request.uri("/api/users");

        requestWithUri.header(
                "Authorization",
                "Basic some-invalid-value"
        );

        var response = requestWithUri.exchange();

        response
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void post_register_duplicate_username_returns_error(
            @Autowired WebTestClient webTestClient) {

        createUser(webTestClient, "DuplicateUser", "secret");

        UserRegisterCreate body = new UserRegisterCreate();
        body.setUsername("DuplicateUser");
        body.setPassword("another-secret");

        webTestClient
                .post()
                .uri("/api/users")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void post_register_without_password_returns_bad_request(
            @Autowired WebTestClient webTestClient) {

        UserRegisterCreate body = new UserRegisterCreate();
        body.setUsername("MissingPasswordUser");

        webTestClient
                .post()
                .uri("/api/users")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void post_register_without_username_returns_bad_request(
            @Autowired WebTestClient webTestClient) {

        UserRegisterCreate body = new UserRegisterCreate();
        body.setPassword("secret");

        webTestClient
                .post()
                .uri("/api/users")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private void createUser(
            WebTestClient webTestClient,
            String username,
            String password) {

        UserRegisterCreate user = new UserRegisterCreate();
        user.setUsername(username);
        user.setPassword(password);

        webTestClient
                .post()
                .uri("/api/users")
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    private String createUserAndLogin(
            WebTestClient webTestClient,
            String username,
            String password) {

        createUser(webTestClient, username, password);

        AuthCreate body = new AuthCreate();
        body.setUsername(username);
        body.setPassword(password);

        var result = webTestClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(UserLoginPublic.class);

        ResponseCookie cookie = result.getResponseCookies()
                .getFirst("accessToken");

        Assertions.assertThat(cookie).isNotNull();

        return cookie.getValue();
    }
}
