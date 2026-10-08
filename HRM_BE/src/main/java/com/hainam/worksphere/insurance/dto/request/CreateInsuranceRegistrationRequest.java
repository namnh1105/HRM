package com.hainam.worksphere.insurance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInsuranceRegistrationRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Insurance ID is required")
    private UUID insuranceId;

    private String registrationNumber;

    private LocalDate startDate;

    private LocalDate endDate;

    private String note;
}
