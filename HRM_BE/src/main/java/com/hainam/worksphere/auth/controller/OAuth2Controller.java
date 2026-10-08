package com.hainam.worksphere.auth.controller;

import com.hainam.worksphere.auth.service.AuthenticationService;
import com.hainam.worksphere.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final AuthenticationService authenticationService;

    @GetMapping("/google")
    public RedirectView googleLogin() {
        return new RedirectView("/oauth2/authorization/google");
    }

    @GetMapping("/validate-token")
    public ApiResponse<Object> validateToken(@RequestParam("token") String token) {
        try {
            var userPrincipal = authenticationService.validateAccessToken(token);
            return ApiResponse.builder()
                    .success(true)
                    .message("Token is valid")
                    .data(userPrincipal)
                    .build();
        } catch (Exception e) {
            return ApiResponse.builder()
                    .success(false)
                    .message("Invalid token: " + e.getMessage())
                    .build();
        }
    }
}
