package com.hainam.worksphere.insurance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceResponse {

    private UUID id;

    private String code;

    private String name;

    private String insuranceType;

    private String provider;

    private Double employeeRate;

    private Double employerRate;

    private String description;

    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
