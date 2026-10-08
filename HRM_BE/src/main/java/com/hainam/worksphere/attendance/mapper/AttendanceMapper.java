package com.hainam.worksphere.attendance.mapper;

import com.hainam.worksphere.attendance.domain.Attendance;
import com.hainam.worksphere.attendance.dto.response.AttendanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttendanceMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "workShiftId", source = "workShift.id")
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeName", source = "store.name")
    AttendanceResponse toAttendanceResponse(Attendance attendance);
}
