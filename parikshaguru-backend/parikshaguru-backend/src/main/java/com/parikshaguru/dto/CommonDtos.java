package com.parikshaguru.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CommonDtos {
    private CommonDtos() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(Instant timestamp, int status, String error, String message, String path, Map<String, String> fieldErrors) {}

    public record MessageResponse(String message) {}

    public record DepartmentDto(Long id, String code, String name) {}

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> of(Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }
}
