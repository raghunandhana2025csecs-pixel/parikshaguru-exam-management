package com.parikshaguru;

import com.parikshaguru.entity.Question;
import com.parikshaguru.entity.enums.QuestionType;
import com.parikshaguru.service.evaluator.*;
import com.parikshaguru.service.impl.PercentageGradingPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {
    private static final double D = 1e-9;

    private Question q(QuestionType t, String correct, double marks, double neg) {
        Question q = new Question();
        q.setQuestionType(t); q.setCorrectAnswer(correct); q.setMarks(marks); q.setNegativeMarks(neg);
        return q;
    }

    @Test void mcqCorrectWrongBlank() {
        var ev = new SingleChoiceEvaluator();
        Question q = q(QuestionType.MCQ, "B", 2, 0.5);
        assertEquals(2.0, ev.evaluate(q, "b", true).doubleValue(), D);
        assertEquals(-0.5, ev.evaluate(q, "A", true).doubleValue(), D);
        assertEquals(0.0, ev.evaluate(q, "A", false).doubleValue(), D);
        assertEquals(0.0, ev.evaluate(q, "", true).doubleValue(), D);
    }

    @Test void multiSelectExactSetOnly() {
        var ev = new MultiSelectEvaluator();
        Question q = q(QuestionType.MULTI_SELECT, "A,C", 3, 1);
        assertEquals(3.0, ev.evaluate(q, "c, a", true).doubleValue(), D);
        assertEquals(-1.0, ev.evaluate(q, "A", true).doubleValue(), D);
    }

    @Test void shortAnswerAlternatives() {
        var ev = new ShortAnswerEvaluator();
        Question q = q(QuestionType.SHORT_ANSWER, "3NF|third normal form", 2, 0);
        assertEquals(2.0, ev.evaluate(q, "  Third   Normal Form ", false).doubleValue(), D);
        assertEquals(0.0, ev.evaluate(q, "2NF", false).doubleValue(), D);
    }

    @Test void descriptiveNeedsManualUnlessBlank() {
        var ev = new DescriptiveEvaluator();
        Question q = q(QuestionType.DESCRIPTIVE, null, 5, 0);
        assertNull(ev.evaluate(q, "my essay", false));
        assertEquals(0.0, ev.evaluate(q, " ", false).doubleValue(), D);
    }

    @Test void grades() {
        var g = new PercentageGradingPolicy();
        assertEquals("A+", g.grade(94));
        assertEquals("B", g.grade(60));
        assertEquals("F", g.grade(39.9));
    }
}
