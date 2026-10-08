package com.hainam.worksphere.workshift.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateWorkShiftRequest {

    @NotBlank(message = "Work shift name is required")
    @Size(max = 100, message = "Work shift name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Work shift code is required")
    @Size(max = 20, message = "Work shift code must not exceed 20 characters")
    private String code;

    @NotNull(message = "Start time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    private Double breakDuration;

    private String description;

    private Boolean isNightShift;

    private Boolean isActive;

    private java.util.UUID storeId;
}
