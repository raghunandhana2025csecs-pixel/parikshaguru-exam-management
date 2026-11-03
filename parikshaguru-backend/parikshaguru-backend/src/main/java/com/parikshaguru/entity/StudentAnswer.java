package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "student_answers", uniqueConstraints = @UniqueConstraint(name = "uk_answer_session_question", columnNames = {"session_id", "question_id"}))
@Getter @Setter @NoArgsConstructor
public class StudentAnswer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false) private ExaminationSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id", nullable = false) private Question question;
    @Column(name = "selected_answer", columnDefinition = "TEXT") private String selectedAnswer;
    @Column(name = "marked_for_review", nullable = false) private boolean markedForReview;
    @Column(name = "answered_at") private Instant answeredAt;
}
