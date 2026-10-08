package com.hainam.worksphere.contract.dto.request;

import com.hainam.worksphere.contract.domain.ContractType;
import jakarta.validation.constraints.NotBlank;
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
public class CreateContractRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate signingDate;

    @NotNull(message = "Base salary is required")
    private Double baseSalary;

    private Double salaryCoefficient;

    private Double allowance;

    private String note;

    private String attachmentUrl;
}
