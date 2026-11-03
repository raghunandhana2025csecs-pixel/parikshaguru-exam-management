package com.parikshaguru.dto;

import com.parikshaguru.dto.QuestionDtos.StudentQuestionDto;
import com.parikshaguru.entity.enums.ProctorEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class SessionDtos {
    private SessionDtos() {}

    public record SessionDto(Long sessionId, Long examId, String examCode, String examTitle, String subjectName, String status,
                             Instant startedAt, Instant deadline, Instant serverTime, long remainingSeconds,
                             boolean negativeMarking, double totalMarks, int totalQuestions, int answered, int unanswered,
                             int markedForReview, List<StudentQuestionDto> questions, String message) {}

    /** answer == null: leave unchanged; answer == "": clear the answer. */
    public record SaveAnswerRequest(@NotNull Long questionId, @Size(max = 20000) String answer, Boolean markedForReview) {}

    public record MarkReviewRequest(@NotNull Long questionId, @NotNull Boolean marked) {}

    public record AnswerDto(Long questionId, String answer, boolean markedForReview) {}

    public record ProgressDto(String status, int totalQuestions, int answered, int unanswered, int markedForReview,
                              long remainingSeconds, Instant serverTime) {}

    public record ProctorEventRequest(@NotNull ProctorEventType eventType, @Size(max = 500) String details) {}

    public record ProctorEventDto(Long id, ProctorEventType eventType, String details, Instant occurredAt) {}

    public record SubmitResponse(Long sessionId, String status, Instant submittedAt, String message) {}
}
