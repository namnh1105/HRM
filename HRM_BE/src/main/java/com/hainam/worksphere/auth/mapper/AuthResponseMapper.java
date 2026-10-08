package com.hainam.worksphere.auth.mapper;

import com.hainam.worksphere.auth.dto.response.AuthenticationResponse;
import com.hainam.worksphere.auth.dto.response.TokenResponse;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthResponseMapper {

    @Mapping(target = "tokenType", constant = "Bearer")
    AuthenticationResponse toAuthenticationResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            UserWithAuthorizationResponse user
    );

    @Mapping(target = "tokenType", constant = "Bearer")
    TokenResponse toTokenResponse(String accessToken, Long expiresIn, UserWithAuthorizationResponse user);
}
