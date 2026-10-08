package com.hainam.worksphere.authorization.mapper;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.dto.request.CreatePermissionRequest;
import com.hainam.worksphere.authorization.dto.request.UpdatePermissionRequest;
import com.hainam.worksphere.authorization.dto.response.PermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", constant = "false")
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Permission toEntity(CreatePermissionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isSystem", ignore = true)
    @Mapping(target = "rolePermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(@MappingTarget Permission entity, UpdatePermissionRequest request);

    PermissionResponse toResponse(Permission entity);
}
