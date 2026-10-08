package com.hainam.worksphere.relative.dto.request;

import com.hainam.worksphere.relative.domain.RelationshipType;
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
public class CreateRelativeRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Relationship type is required")
    private RelationshipType relationship;

    private LocalDate dateOfBirth;

    private String phone;

    private String idCardNumber;

    private String occupation;

    private String address;

    private Boolean isEmergencyContact;

    private Boolean isDependent;
}
