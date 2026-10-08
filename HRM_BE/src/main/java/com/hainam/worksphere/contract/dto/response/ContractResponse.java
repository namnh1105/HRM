package com.hainam.worksphere.contract.dto.response;

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
public class ContractResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String employeeCode;

    private String contractType;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate signingDate;

    private Double baseSalary;

    private Double salaryCoefficient;

    private Double allowance;

    private String status;

    private String note;

    private String attachmentUrl;

    private Instant createdAt;

    private Instant updatedAt;
}
