package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class SingleChoiceEvaluator extends AbstractChoiceEvaluator {
    @Override public boolean supports(QuestionType t) { return t == QuestionType.MCQ || t == QuestionType.TRUE_FALSE; }
    @Override protected boolean isCorrect(Question q, String a) {
        return q.getCorrectAnswer() != null && q.getCorrectAnswer().trim().equalsIgnoreCase(a.trim());
    }
}
