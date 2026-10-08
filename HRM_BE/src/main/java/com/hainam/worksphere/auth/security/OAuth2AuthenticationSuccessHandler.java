package com.hainam.worksphere.auth.security;

import com.hainam.worksphere.auth.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.oauth.success-redirect-path:/oauth/success}")
    private String successRedirectPath;

    public OAuth2AuthenticationSuccessHandler(@Lazy AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // Extract user information from Google OAuth2 response
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String googleId = oAuth2User.getAttribute("sub");
            String givenName = oAuth2User.getAttribute("given_name");
            String familyName = oAuth2User.getAttribute("family_name");

            log.info("Google OAuth2 login successful for email: {}", email);

            // Process OAuth2 login through AuthenticationService
            var authResponse = authenticationService.processGoogleOAuth2Login(email, name, googleId, givenName, familyName);

            // Build redirect URL to frontend with token parameters
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + successRedirectPath)
                    .queryParam("access_token", authResponse.getAccessToken())
                    .queryParam("refresh_token", authResponse.getRefreshToken())
                    .queryParam("token_type", "Bearer")
                    .queryParam("expires_in", authResponse.getExpiresIn())
                    .queryParam("user", URLEncoder.encode(authResponse.getUser().getEmail(), StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            log.info("Redirecting to frontend: {}", frontendUrl + successRedirectPath);

            // Redirect to frontend with token
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error processing Google OAuth2 login", e);

            // Redirect to frontend with error
            String errorRedirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + successRedirectPath)
                    .queryParam("error", "oauth_failed")
                    .queryParam("message", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            response.sendRedirect(errorRedirectUrl);
        }
    }
}

