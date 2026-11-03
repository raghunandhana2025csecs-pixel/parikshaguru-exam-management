package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.Question;

public abstract class AbstractChoiceEvaluator implements QuestionEvaluator {
    @Override
    public final Double evaluate(Question q, String answer, boolean negEnabled) {
        if (answer == null || answer.isBlank()) return 0.0;               // unanswered: no marks, no penalty
        if (isCorrect(q, answer)) return q.getMarks();
        return negEnabled ? -q.getNegativeMarks() : 0.0;
    }

    protected abstract boolean isCorrect(Question q, String answer);
}
