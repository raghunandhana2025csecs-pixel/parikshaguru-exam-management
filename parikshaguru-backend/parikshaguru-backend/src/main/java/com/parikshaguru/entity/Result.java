package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "results", uniqueConstraints = {
        @UniqueConstraint(name = "uk_result_submission", columnNames = "submission_id"),
        @UniqueConstraint(name = "uk_result_exam_student", columnNames = {"exam_id", "student_id"})},
        indexes = @Index(name = "idx_result_status", columnList = "status"))
@Getter @Setter @NoArgsConstructor
public class Result extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "submission_id", nullable = false) private Submission submission;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exam_id", nullable = false) private Examination exam;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private User student;
    @Column(name = "total_marks", nullable = false) private double totalMarks;
    @Column(name = "max_marks", nullable = false) private double maxMarks;
    @Column(nullable = false) private double percentage;
    @Column(nullable = false, length = 5) private String grade;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ResultStatus status = ResultStatus.PENDING_APPROVAL;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by") private User approvedBy;
    @Column(name = "approved_at") private Instant approvedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "published_by") private User publishedBy;
    @Column(name = "published_at") private Instant publishedAt;
}
