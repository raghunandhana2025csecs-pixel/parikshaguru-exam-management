package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "examination_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_session_exam_student", columnNames = {"exam_id", "student_id"}),
        indexes = @Index(name = "idx_session_status_deadline", columnList = "status,deadline"))
@Getter @Setter @NoArgsConstructor
public class ExaminationSession extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exam_id", nullable = false) private Examination exam;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private User student;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    /** Authoritative server-side deadline. */
    @Column(nullable = false) private Instant deadline;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private SessionStatus status = SessionStatus.IN_PROGRESS;
    @Column(name = "ip_address", length = 64) private String ipAddress;
    @Column(name = "last_activity_at") private Instant lastActivityAt;
}
