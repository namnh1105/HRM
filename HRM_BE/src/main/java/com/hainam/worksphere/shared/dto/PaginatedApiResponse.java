package com.hainam.worksphere.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private List<T> data;
    private PageInfo pagination;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> PaginatedApiResponse<T> success(Page<T> page) {
        return PaginatedApiResponse.<T>builder()
                .success(true)
                .data(page.getContent())
                .pagination(PageInfo.of(page))
                .build();
    }

    public static <T> PaginatedApiResponse<T> success(String message, Page<T> page) {
        return PaginatedApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(page.getContent())
                .pagination(PageInfo.of(page))
                .build();
    }

    public static <T> PaginatedApiResponse<T> error(String message) {
        return PaginatedApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> PaginatedApiResponse<T> error(String code, String message) {
        return PaginatedApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
