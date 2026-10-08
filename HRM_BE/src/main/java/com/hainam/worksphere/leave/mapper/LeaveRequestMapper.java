package com.hainam.worksphere.leave.mapper;

import com.hainam.worksphere.leave.domain.LeaveRequest;
import com.hainam.worksphere.leave.dto.response.LeaveRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LeaveRequestMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "approverId", source = "approver.id")
    @Mapping(target = "approverName", source = "approver.fullName")
    @Mapping(target = "leaveType", expression = "java(leaveRequest.getLeaveType() != null ? leaveRequest.getLeaveType().name() : null)")
    @Mapping(target = "status", expression = "java(leaveRequest.getStatus() != null ? leaveRequest.getStatus().name() : null)")
    LeaveRequestResponse toLeaveRequestResponse(LeaveRequest leaveRequest);
}
