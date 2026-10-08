package com.hainam.worksphere.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Store code is required")
    @Size(max = 30, message = "Store code must not exceed 30 characters")
    private String code;

    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String email;
}
