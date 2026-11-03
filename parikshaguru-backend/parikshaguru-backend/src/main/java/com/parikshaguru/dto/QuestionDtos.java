package com.parikshaguru.dto;

import com.parikshaguru.entity.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public final class QuestionDtos {
    private QuestionDtos() {}

    public record OptionDto(@Size(max = 2) String label, String text) {}

    public record QuestionRequest(
            @NotBlank String questionText,
            @NotNull QuestionType type,
            @Valid List<OptionDto> options,
            String correctAnswer,
            @NotNull @Positive Double marks,
            @PositiveOrZero Double negativeMarks,
            @Size(max = 120) String topic,
            Integer orderNo) {}

    /** Faculty/admin view - includes the correct answer. */
    public record QuestionDto(Long id, Long examId, Long subjectId, String topic, String questionText, QuestionType type,
                              List<OptionDto> options, String correctAnswer, double marks, double negativeMarks, int orderNo) {}

    /** Student view - NEVER contains the correct answer. */
    public record StudentQuestionDto(Long id, int orderNo, QuestionType type, String questionText, List<OptionDto> options,
                                     double marks, double negativeMarks, String topic, String selectedAnswer, boolean markedForReview) {}
}
