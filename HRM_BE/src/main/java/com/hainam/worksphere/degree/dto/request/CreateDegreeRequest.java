package com.hainam.worksphere.degree.dto.request;

import com.hainam.worksphere.degree.domain.DegreeLevel;
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
public class CreateDegreeRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotBlank(message = "Degree name is required")
    private String degreeName;

    private DegreeLevel degreeLevel;

    private String major;

    private String institution;

    private LocalDate graduationDate;

    private Double gpa;

    private String attachmentUrl;

    private String note;
}
