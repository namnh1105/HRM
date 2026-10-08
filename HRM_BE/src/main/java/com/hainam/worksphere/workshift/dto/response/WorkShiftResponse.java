package com.hainam.worksphere.workshift.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftResponse {

    private UUID id;

    private String name;

    private String code;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    private Double breakDuration;

    private Double totalHours;

    private String description;

    private Boolean isActive;

    private Boolean isNightShift;

    private UUID storeId;

    private String storeName;

    private Instant createdAt;

    private Instant updatedAt;
}
