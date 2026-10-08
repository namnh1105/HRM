package com.hainam.worksphere.contract.dto.request;

import com.hainam.worksphere.contract.domain.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContractRequest {

    private LocalDate endDate;

    private Double baseSalary;

    private Double salaryCoefficient;

    private Double allowance;

    private ContractStatus status;

    private String note;

    private String attachmentUrl;
}
