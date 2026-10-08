package com.hainam.worksphere.department.mapper;

import com.hainam.worksphere.department.domain.Department;
import com.hainam.worksphere.department.dto.request.CreateDepartmentRequest;
import com.hainam.worksphere.department.dto.response.DepartmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {

    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", source = "manager.fullName")
    @Mapping(target = "parentDepartmentId", source = "parentDepartment.id")
    @Mapping(target = "parentDepartmentName", source = "parentDepartment.name")
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.name")
    DepartmentResponse toDepartmentResponse(Department department);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Department toEntity(CreateDepartmentRequest request);
}
