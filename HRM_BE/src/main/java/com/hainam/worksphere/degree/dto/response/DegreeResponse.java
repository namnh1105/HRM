package com.hainam.worksphere.degree.dto.response;

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
public class DegreeResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName;

    private String degreeName;

    private String degreeLevel;

    private String major;

    private String institution;

    private LocalDate graduationDate;

    private Double gpa;

    private String attachmentUrl;

    private String note;

    private Instant createdAt;

    private Instant updatedAt;
}
