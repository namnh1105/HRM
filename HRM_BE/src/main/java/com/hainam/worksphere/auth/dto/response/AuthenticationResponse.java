package com.hainam.worksphere.auth.dto.response;

import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private UserWithAuthorizationResponse user;
}
