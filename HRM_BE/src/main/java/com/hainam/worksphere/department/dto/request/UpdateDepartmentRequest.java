package com.hainam.worksphere.department.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    private String description;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String email;

    private UUID managerId;

    private UUID parentDepartmentId;

    private UUID storeId;

    private Boolean isActive;
}
