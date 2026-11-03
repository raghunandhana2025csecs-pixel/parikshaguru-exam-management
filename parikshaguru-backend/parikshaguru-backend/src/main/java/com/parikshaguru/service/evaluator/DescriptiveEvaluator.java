package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;
import org.springframework.stereotype.Component;

/** Descriptive answers always need a human; an empty answer is simply 0. */
@Component
public class DescriptiveEvaluator implements QuestionEvaluator {
    @Override public boolean supports(QuestionType t) { return t == QuestionType.DESCRIPTIVE; }
    @Override public Double evaluate(Question q, String answer, boolean neg) {
        return (answer == null || answer.isBlank()) ? Double.valueOf(0.0) : null;
    }
}
