package com.hainam.worksphere.leave.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveLeaveRequestDto {

    @NotNull(message = "Approved status is required")
    private Boolean approved;

    private String comment;
}
