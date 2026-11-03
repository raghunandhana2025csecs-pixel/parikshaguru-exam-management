package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "exam_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_assignment", columnNames = {"exam_id", "student_id"}))
@Getter @Setter @NoArgsConstructor
public class ExamAssignment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exam_id", nullable = false) private Examination exam;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private User student;
}
