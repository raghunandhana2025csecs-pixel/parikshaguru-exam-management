package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "submissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_submission_session", columnNames = "session_id"),
        @UniqueConstraint(name = "uk_submission_exam_student", columnNames = {"exam_id", "student_id"})},
        indexes = @Index(name = "idx_submission_grading", columnList = "grading_status"))
@Getter @Setter @NoArgsConstructor
public class Submission extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false) private ExaminationSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exam_id", nullable = false) private Examination exam;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private User student;
    @Column(name = "submitted_at", nullable = false) private Instant submittedAt;
    @Column(name = "auto_submitted", nullable = false) private boolean autoSubmitted;
    @Column(name = "objective_marks", nullable = false) private double objectiveMarks;
    @Column(name = "total_marks", nullable = false) private double totalMarks;
    @Enumerated(EnumType.STRING) @Column(name = "grading_status", nullable = false, length = 20) private GradingStatus gradingStatus = GradingStatus.PENDING;
    @Column(columnDefinition = "TEXT") private String feedback;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "evaluated_by") private User evaluatedBy;
    @Column(name = "evaluated_at") private Instant evaluatedAt;
}
