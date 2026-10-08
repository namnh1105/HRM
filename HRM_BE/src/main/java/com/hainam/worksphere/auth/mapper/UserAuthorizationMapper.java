package com.hainam.worksphere.auth.mapper;

import com.hainam.worksphere.authorization.domain.Permission;
import com.hainam.worksphere.authorization.domain.Role;
import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {com.hainam.worksphere.employee.mapper.EmployeeMapper.class})
public interface UserAuthorizationMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "updatedAt", source = "user.updatedAt")
    @Mapping(target = "createdBy", source = "user.createdBy")
    @Mapping(target = "updatedBy", source = "user.updatedBy")
    @Mapping(target = "isDeleted", source = "user.isDeleted")
    @Mapping(target = "deletedAt", source = "user.deletedAt")
    @Mapping(target = "deletedBy", source = "user.deletedBy")
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "avatarUrl", source = "employee.avatarUrl")
    @Mapping(target = "roles", expression = "java(rolesToStringList(roles))")
    @Mapping(target = "permissions", expression = "java(permissionsToStringList(permissions))")
    @Mapping(target = "isActive", source = "user.isEnabled")
    UserWithAuthorizationResponse toUserWithAuthorizationResponse(User user, Employee employee, List<Role> roles, List<Permission> permissions);

    default List<String> rolesToStringList(List<Role> roles) {
        return roles.stream().map(Role::getCode).collect(Collectors.toList());
    }

    default List<String> permissionsToStringList(List<Permission> permissions) {
        return permissions.stream().map(Permission::getCode).collect(Collectors.toList());
    }
}
