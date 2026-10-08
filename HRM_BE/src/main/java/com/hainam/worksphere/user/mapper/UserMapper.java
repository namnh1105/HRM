package com.hainam.worksphere.user.mapper;

import com.hainam.worksphere.employee.domain.Employee;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        com.hainam.worksphere.employee.mapper.EmployeeMapper.class })
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatarUrl", source = "employee.avatarUrl")
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "isActive", source = "user.isEnabled")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "updatedAt", source = "user.updatedAt")
    @Mapping(target = "createdBy", source = "user.createdBy")
    @Mapping(target = "updatedBy", source = "user.updatedBy")
    @Mapping(target = "isDeleted", source = "user.isDeleted")
    @Mapping(target = "deletedAt", source = "user.deletedAt")
    @Mapping(target = "deletedBy", source = "user.deletedBy")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    UserResponse toUserResponse(User user, Employee employee);
}
