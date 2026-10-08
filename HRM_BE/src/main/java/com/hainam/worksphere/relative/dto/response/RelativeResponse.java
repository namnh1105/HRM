package com.hainam.worksphere.relative.dto.response;

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
public class RelativeResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String fullName;

    private String relationship;

    private LocalDate dateOfBirth;

    private String phone;

    private String idCardNumber;

    private String occupation;

    private String address;

    private Boolean isEmergencyContact;

    private Boolean isDependent;

    private Instant createdAt;

    private Instant updatedAt;
}
