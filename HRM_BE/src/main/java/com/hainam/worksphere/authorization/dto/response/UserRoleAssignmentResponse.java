package com.hainam.worksphere.authorization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentResponse {

    private UUID id;

    private UUID userId;

    private RoleResponse role;

    private Boolean isActive;
}
