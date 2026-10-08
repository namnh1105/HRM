package com.hainam.worksphere.insurance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRegistrationResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private UUID insuranceId;

    private String insuranceName;

    private String registrationNumber;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String note;

    private Instant createdAt;

    private Instant updatedAt;
}
