package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "questions", indexes = @Index(name = "idx_question_exam", columnList = "exam_id,order_no"))
@Getter @Setter @NoArgsConstructor
public class Question extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exam_id", nullable = false) private Examination exam;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subject_id", nullable = false) private Subject subject;
    @Column(length = 120) private String topic;
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT") private String questionText;
    @Enumerated(EnumType.STRING) @Column(name = "question_type", nullable = false, length = 20) private QuestionType questionType;
    /** MCQ: "B"; MULTI_SELECT: "A,C"; TRUE_FALSE: "TRUE"/"FALSE"; SHORT_ANSWER: text ("a|b" = alternatives). Never sent to students. */
    @Column(name = "correct_answer", columnDefinition = "TEXT") private String correctAnswer;
    @Column(nullable = false) private double marks;
    @Column(name = "negative_marks", nullable = false) private double negativeMarks;
    @Column(name = "order_no", nullable = false) private int orderNo;
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("label ASC")
    private List<QuestionOption> options = new ArrayList<>();
}
