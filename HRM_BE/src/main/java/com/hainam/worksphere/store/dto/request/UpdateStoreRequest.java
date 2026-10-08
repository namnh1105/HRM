package com.hainam.worksphere.store.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreRequest {

    @Size(max = 150, message = "Store name must not exceed 150 characters")
    private String name;

    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String email;

    private Boolean isActive;
}
