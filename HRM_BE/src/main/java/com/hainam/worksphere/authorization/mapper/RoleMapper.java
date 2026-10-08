package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.authorization.dto.request.CreateRoleRequest;
import com.hainam.worksphere.authorization.dto.request.UpdateRoleRequest;
import com.hainam.worksphere.authorization.dto.response.RoleResponse;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PermissionMapper.class)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", constant = "false")
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Role toEntity(CreateRoleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(@MappingTarget Role entity, UpdateRoleRequest request);

    @Mapping(target = "permissions", ignore = true)
    RoleResponse toResponse(Role entity);

    @AfterMapping
    default void mapPermissions(@MappingTarget RoleResponse response, Role entity) {
        if (entity.getRolePermissions() != null) {
            Set<com.hainam.worksphere.authorization.dto.response.PermissionResponse> permissions = entity.getRolePermissions()
                .stream()
                .filter(rp -> rp.getPermission() != null && rp.getIsActive() && !rp.getPermission().getIsDeleted())
                .map(rp -> {
                    com.hainam.worksphere.authorization.domain.Permission p = rp.getPermission();
                    return com.hainam.worksphere.authorization.dto.response.PermissionResponse.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .displayName(p.getDisplayName())
                        .description(p.getDescription())
                        .resource(p.getResource())
                        .action(p.getAction())
                        .isSystem(p.getIsSystem())
                        .isActive(p.getIsActive())
                        .isDeleted(p.getIsDeleted())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build();
                })
                .collect(Collectors.toSet());
            response.setPermissions(permissions);
        } else {
            response.setPermissions(new java.util.HashSet<>());
        }
    }

    @Named("toSimpleResponse")
    @Mapping(target = "permissions", ignore = true)
    RoleResponse toSimpleResponse(Role entity);
}
