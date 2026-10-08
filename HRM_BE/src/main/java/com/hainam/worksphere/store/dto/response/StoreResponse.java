package com.hainam.worksphere.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private UUID id;

    private String name;

    private String code;

    private String address;

    private String phone;

    private String email;

    private Boolean isActive;

    private Instant createdAt;

    private Instant updatedAt;
}
