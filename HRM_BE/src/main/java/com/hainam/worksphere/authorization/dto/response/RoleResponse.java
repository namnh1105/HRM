package com.hainam.worksphere.authorization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private UUID id;
    private String code;
    private String displayName;
    private String description;
    private Boolean isSystem;
    private Boolean isActive;
    private Boolean isDeleted;
    private Set<PermissionResponse> permissions;

    private Instant createdAt;

    private Instant updatedAt;
}
