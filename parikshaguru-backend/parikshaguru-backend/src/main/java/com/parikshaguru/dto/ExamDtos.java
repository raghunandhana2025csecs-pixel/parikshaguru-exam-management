package com.parikshaguru.dto;

import com.parikshaguru.entity.enums.ExamStatus;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public final class ExamDtos {
    private ExamDtos() {}

    public record ExamRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 40) String code,
            @NotNull Long subjectId,
            @Min(1) @Max(12) Integer semester,
            @Size(max = 40) String examType,
            @NotNull @Positive Double totalMarks,
            @NotNull @Min(1) @Max(600) Integer durationMinutes,
            @NotNull Instant startTime,
            @NotNull Instant endTime,
            Boolean negativeMarking,
            @Min(0) Integer numberOfQuestions,
            String instructions) {}

    public record ExamDto(Long id, String code, String title, Long subjectId, String subjectCode, String subjectName,
                          int semester, String examType, double totalMarks, int durationMinutes,
                          Instant startTime, Instant endTime, boolean negativeMarking, int numberOfQuestions,
                          long questionCount, String instructions, ExamStatus status, ExamStatus effectiveStatus,
                          long assignedCount, long submissionCount, String createdBy) {}

    public record AssignRequest(List<Long> studentIds) {}
}
