package com.hainam.worksphere.workshift.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkShiftResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private UUID workShiftId;

    private String workShiftName;

    private String workShiftCode;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    private LocalDate date;

    private Instant createdAt;

    private Instant updatedAt;
}
