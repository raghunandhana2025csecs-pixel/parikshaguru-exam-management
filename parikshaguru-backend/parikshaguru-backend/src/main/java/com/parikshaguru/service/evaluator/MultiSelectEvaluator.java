package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;
import com.parikshaguru.util.AnswerUtil;
import org.springframework.stereotype.Component;

/** All-or-nothing: the selected set must exactly equal the correct set. */
@Component
public class MultiSelectEvaluator extends AbstractChoiceEvaluator {
    @Override public boolean supports(QuestionType t) { return t == QuestionType.MULTI_SELECT; }
    @Override protected boolean isCorrect(Question q, String a) {
        return AnswerUtil.canonicalLabels(q.getCorrectAnswer()).equals(AnswerUtil.canonicalLabels(a));
    }
}
