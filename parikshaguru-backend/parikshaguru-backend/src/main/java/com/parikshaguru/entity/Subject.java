package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "subjects", indexes = @Index(name = "idx_subjects_semester", columnList = "semester"))
@Getter @Setter @NoArgsConstructor
public class Subject extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30) private String code;
    @Column(nullable = false, length = 150) private String name;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "department_id", nullable = false) private Department department;
    @Column(nullable = false) private int semester;
    @Column(name = "credit_points", nullable = false) private double creditPoints;
    @Column(columnDefinition = "TEXT") private String description;
    /** Many-to-many: table subject_faculty(subject_id, faculty_id). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "subject_faculty", joinColumns = @JoinColumn(name = "subject_id"), inverseJoinColumns = @JoinColumn(name = "faculty_id"))
    private Set<User> faculty = new HashSet<>();
}
