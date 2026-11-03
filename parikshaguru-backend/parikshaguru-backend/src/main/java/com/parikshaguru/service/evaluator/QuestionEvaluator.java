package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;

/** Strategy: one implementation per question type. */
public interface QuestionEvaluator {
    boolean supports(QuestionType type);

    /** @return marks awarded (may be negative), or null when manual evaluation is required. */
    Double evaluate(Question question, String studentAnswer, boolean negativeMarkingEnabled);
}
