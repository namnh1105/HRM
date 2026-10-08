package com.hainam.worksphere.shared.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceStatsResponse {
    private long total;
    private long active;
    private long inactive;
    private long deleted;
}
