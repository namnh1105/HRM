package com.hainam.worksphere.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Given name is required")
    @Size(max = 50, message = "Given name must be less than 50 characters")
    private String givenName;

    @NotBlank(message = "Family name is required")
    @Size(max = 50, message = "Family name must be less than 50 characters")
    private String familyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 255, message = "Avatar URL must be less than 255 characters")
    private String avatarUrl;
}
