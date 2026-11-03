package com.parikshaguru.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public final class SubjectDtos {
    private SubjectDtos() {}

    public record SubjectRequest(
            @NotBlank @Size(max = 30) String code,
            @NotBlank @Size(max = 150) String name,
            String department,                                  // code or name; defaults to caller's department
            @NotNull @Min(1) @Max(12) Integer semester,
            @NotNull @DecimalMin("0.5") @DecimalMax("30") Double creditPoints,
            String description,
            List<Long> facultyIds) {}

    public record FacultySummary(Long id, String fullName, String email, String identifier) {}

    public record SubjectDto(Long id, String code, String name, String department, String departmentCode,
                             int semester, double creditPoints, String description, List<FacultySummary> faculty) {}
}
