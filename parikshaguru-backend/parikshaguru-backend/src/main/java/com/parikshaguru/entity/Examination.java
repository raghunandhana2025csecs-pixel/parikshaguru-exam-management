package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "examinations", indexes = {
        @Index(name = "idx_exam_status", columnList = "status"),
        @Index(name = "idx_exam_window", columnList = "start_time,end_time")})
@Getter @Setter @NoArgsConstructor
public class Examination extends BaseEntity {
    @Column(nullable = false, unique = true, length = 40) private String code;
    @Column(nullable = false, length = 200) private String title;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subject_id", nullable = false) private Subject subject;
    @Column(nullable = false) private int semester;
    @Column(name = "exam_type", nullable = false, length = 40) private String examType;
    @Column(name = "total_marks", nullable = false) private double totalMarks;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(name = "start_time", nullable = false) private Instant startTime;
    @Column(name = "end_time", nullable = false) private Instant endTime;
    @Column(name = "negative_marking", nullable = false) private boolean negativeMarking;
    @Column(name = "number_of_questions", nullable = false) private int numberOfQuestions;
    @Column(columnDefinition = "TEXT") private String instructions;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ExamStatus status = ExamStatus.DRAFT;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private User createdBy;
}
