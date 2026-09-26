package at.technikum.paperless.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secretKey;
    private int expirationDays = 1;
}
