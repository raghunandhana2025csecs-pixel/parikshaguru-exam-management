package com.parikshaguru.service.evaluator;

import com.parikshaguru.entity.enums.QuestionType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EvaluatorRegistry {
    private final List<QuestionEvaluator> evaluators;

    public EvaluatorRegistry(List<QuestionEvaluator> evaluators) { this.evaluators = evaluators; }

    public QuestionEvaluator forType(QuestionType t) {
        return evaluators.stream().filter(e -> e.supports(t)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No evaluator for " + t));
    }
}
