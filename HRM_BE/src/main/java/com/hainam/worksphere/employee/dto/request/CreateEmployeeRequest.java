package com.hainam.worksphere.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreateEmployeeRequest {

    @NotBlank(message = "Employee code is required")
    @Size(max = 20)
    private String employeeCode;

    private UUID userId;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    private LocalDate dateOfBirth;

    private String gender;

    @Size(max = 20)
    private String idCardNumber;

    private LocalDate idCardIssuedDate;

    @Size(max = 100)
    private String idCardIssuedPlace;

    private String permanentAddress;

    private String currentAddress;

    private UUID departmentId;

    private UUID storeId;

    @Size(max = 100)
    private String position;

    private LocalDate joinDate;

    @Size(max = 30)
    private String bankAccountNumber;

    @Size(max = 100)
    private String bankName;

    @Size(max = 20)
    private String taxCode;

    @Size(max = 20)
    private String socialInsuranceNumber;

    @Size(max = 20)
    private String healthInsuranceNumber;
}
