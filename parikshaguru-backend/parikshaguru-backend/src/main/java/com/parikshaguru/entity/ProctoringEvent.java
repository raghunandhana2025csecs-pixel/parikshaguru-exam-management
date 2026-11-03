package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "proctoring_events", indexes = @Index(name = "idx_proctor_session", columnList = "session_id,occurred_at"))
@Getter @Setter @NoArgsConstructor
public class ProctoringEvent extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false) private ExaminationSession session;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 30) private ProctorEventType eventType;
    @Column(length = 500) private String details;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
}
