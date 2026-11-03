package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;
import com.parikshaguru.util.AnswerUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/** Case/whitespace-insensitive match against any "|"-separated accepted answer. No negative marking. */
@Component
public class ShortAnswerEvaluator implements QuestionEvaluator {
    @Override public boolean supports(QuestionType t) { return t == QuestionType.SHORT_ANSWER; }
    @Override public Double evaluate(Question q, String answer, boolean neg) {
        if (answer == null || answer.isBlank() || q.getCorrectAnswer() == null) return 0.0;
        String given = AnswerUtil.normalizeText(answer);
        boolean ok = Arrays.stream(q.getCorrectAnswer().split("\\|")).map(AnswerUtil::normalizeText).anyMatch(given::equals);
        return ok ? q.getMarks() : 0.0;
    }
}
