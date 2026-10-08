package com.hainam.worksphere.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class JwtProperties {
    @Value("${JWT_SECRET:mySecretKey123456789012345678901234567890123456789012345678901234567890}")
    private String secret;

    @Value("${JWT_ACCESS_TOKEN_EXPIRATION:900000}")
    private long accessTokenExpiration;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION:604800000}")
    private long refreshTokenExpiration;
}
