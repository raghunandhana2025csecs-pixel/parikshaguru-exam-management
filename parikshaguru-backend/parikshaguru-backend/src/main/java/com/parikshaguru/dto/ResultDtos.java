package com.parikshaguru.dto;

import com.parikshaguru.dto.QuestionDtos.OptionDto;
import com.parikshaguru.entity.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ResultDtos {
    private ResultDtos() {}

    public record SubmissionSummaryDto(Long id, Long examId, String examCode, String examTitle, Long studentId, String studentName,
                                       String rollNumber, Instant submittedAt, boolean autoSubmitted, double objectiveMarks,
                                       double totalMarks, double maxMarks, GradingStatus gradingStatus,
                                       Long resultId, ResultStatus resultStatus) {}

    public record AnswerReviewDto(Long questionId, int orderNo, QuestionType type, String questionText, List<OptionDto> options,
                                  String correctAnswer, String studentAnswer, double maxMarks, Double marksAwarded,
                                  boolean autoGraded, boolean needsManualGrading, String feedback) {}

    public record SubmissionDetailDto(SubmissionSummaryDto summary, String feedback, Long sessionId,
                                      List<AnswerReviewDto> answers, Map<String, Long> proctoringSummary) {}

    public record EvaluationItem(@NotNull Long questionId, @NotNull @PositiveOrZero Double marks, String feedback) {}

    public record EvaluateRequest(@NotEmpty @Valid List<EvaluationItem> items, String feedback) {}

    /** marks/percentage/grade are null for students until the result is PUBLISHED. */
    public record ResultDto(Long id, Long submissionId, Long examId, String examCode, String examTitle, String subjectCode,
                            String subjectName, Long studentId, String studentName, String rollNumber,
                            Double marksObtained, Double maxMarks, Double percentage, String grade, ResultStatus status,
                            boolean published, Instant submittedAt, Instant approvedAt, Instant publishedAt, String feedback) {}

    public record StudentResultsDto(List<ResultDto> results, long publishedCount, long pendingCount, Double averagePercentage) {}

    public record BulkIdsRequest(@NotEmpty List<Long> ids) {}

    public record BulkResultDto(int processed, int skipped, List<String> errors) {}

    public record HistoryDto(String action, Long userId, String userName, Instant at, String details) {}
}
