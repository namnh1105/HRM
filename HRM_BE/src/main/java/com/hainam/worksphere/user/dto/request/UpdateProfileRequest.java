package com.hainam.worksphere.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "Given name must be less than 50 characters")
    private String givenName;

    @Size(max = 50, message = "Family name must be less than 50 characters")
    private String familyName;

    @Size(max = 255, message = "Avatar URL must be less than 255 characters")
    private String avatarUrl;
}
