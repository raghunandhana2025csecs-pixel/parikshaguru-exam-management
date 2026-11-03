package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

/** Student-specific data (table student_profiles) - one-to-one with User. */
@Entity @Table(name = "student_profiles")
@Getter @Setter @NoArgsConstructor
public class Student extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(nullable = false, length = 100) private String course;
    @Column(nullable = false) private int semester;
    @Column(length = 20) private String phone;
}
