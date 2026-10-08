package com.hainam.worksphere.workshift.mapper;

import com.hainam.worksphere.workshift.domain.EmployeeWorkShift;
import com.hainam.worksphere.workshift.dto.request.AssignWorkShiftRequest;
import com.hainam.worksphere.workshift.dto.response.EmployeeWorkShiftResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeWorkShiftMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "employee.employeeCode", target = "employeeCode")
    @Mapping(source = "workShift.id", target = "workShiftId")
    @Mapping(source = "workShift.name", target = "workShiftName")
    @Mapping(source = "workShift.code", target = "workShiftCode")
    @Mapping(source = "workShift.startTime", target = "shiftStartTime")
    @Mapping(source = "workShift.endTime", target = "shiftEndTime")
    EmployeeWorkShiftResponse toResponse(EmployeeWorkShift employeeWorkShift);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "workShift", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    EmployeeWorkShift toEntity(AssignWorkShiftRequest request);
}
