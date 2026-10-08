package com.hainam.worksphere.authorization.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "At least one role ID is required")
    private List<UUID> roleIds;
}
