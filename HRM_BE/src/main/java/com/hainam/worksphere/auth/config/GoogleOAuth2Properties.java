package com.hainam.worksphere.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class GoogleOAuth2Properties {
    @Value("${GOOGLE_OAUTH2_CLIENT_ID:}")
    private String clientId;

    @Value("${GOOGLE_OAUTH2_CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${GOOGLE_OAUTH2_REDIRECT_URI:http://localhost:8080/auth/google/callback}")
    private String redirectUri;
}
