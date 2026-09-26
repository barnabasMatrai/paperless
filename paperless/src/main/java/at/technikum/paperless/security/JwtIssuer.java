package at.technikum.paperless.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class JwtIssuer {

    private final JwtProperties properties;

    public JwtIssuer(JwtProperties properties) {
        this.properties = properties;
    }

    public String issue(long userId, List<String> roles) {
        Instant expiresAt = Instant.now().plus(Duration.ofDays(properties.getExpirationDays()));
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withExpiresAt(expiresAt)
                .withClaim("a", roles)
                .sign(algorithm());
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return JWT.require(algorithm())
                .build()
                .verify(token);
    }

    private Algorithm algorithm() {
        return Algorithm.HMAC256(properties.getSecretKey());
    }
}
