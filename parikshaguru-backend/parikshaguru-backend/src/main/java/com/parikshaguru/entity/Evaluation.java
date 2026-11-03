package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "evaluations", uniqueConstraints = @UniqueConstraint(name = "uk_eval_submission_question", columnNames = {"submission_id", "question_id"}))
@Getter @Setter @NoArgsConstructor
public class Evaluation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "submission_id", nullable = false) private Submission submission;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id", nullable = false) private Question question;
    /** null = awaiting manual evaluation. */
    @Column(name = "marks_awarded") private Double marksAwarded;
    @Column(columnDefinition = "TEXT") private String feedback;
    @Column(name = "auto_graded", nullable = false) private boolean autoGraded;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "evaluator_id") private User evaluator;
}
