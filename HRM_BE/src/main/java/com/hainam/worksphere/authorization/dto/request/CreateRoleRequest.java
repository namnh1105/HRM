package com.hainam.worksphere.authorization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    @NotBlank(message = "Role code is required")
    @Size(max = 50, message = "Role code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
