package com.hainam.worksphere.employee.dto.response;

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
public class EmployeeResponse {

    private UUID id;

    private String employeeCode;

    private UUID userId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    private String gender;

    private String idCardNumber;

    private String permanentAddress;

    private String currentAddress;

    private String avatarUrl;

    private UUID departmentId;

    private String departmentName;

    private UUID storeId;

    private String storeName;

    private String position;

    private LocalDate joinDate;

    private LocalDate leaveDate;

    private String employmentStatus;

    private String bankAccountNumber;

    private String bankName;

    private String taxCode;

    private String socialInsuranceNumber;

    private String healthInsuranceNumber;

    private Instant createdAt;

    private Instant updatedAt;
}
