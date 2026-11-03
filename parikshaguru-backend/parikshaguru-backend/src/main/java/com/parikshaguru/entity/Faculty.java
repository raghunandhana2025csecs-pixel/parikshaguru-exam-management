package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

/** Faculty-specific data (table faculty_profiles) - one-to-one with User. */
@Entity @Table(name = "faculty_profiles")
@Getter @Setter @NoArgsConstructor
public class Faculty extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(nullable = false, length = 100) private String designation;
    @Column(length = 20) private String phone;
}
