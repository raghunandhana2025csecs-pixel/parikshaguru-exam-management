package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_role_status", columnList = "role,status"))
@Getter @Setter @NoArgsConstructor
public class User extends BaseEntity {
    @Column(name = "full_name", nullable = false, length = 120) private String fullName;
    @Column(nullable = false, unique = true, length = 190) private String email;
    /** Roll number (students) or Employee ID (faculty/admin). */
    @Column(nullable = false, unique = true, length = 60) private String identifier;
    @Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AccountStatus status;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "department_id") private Department department;
    /** Incremented on logout / password change so previously issued JWTs become invalid. */
    @Column(name = "token_version", nullable = false) private int tokenVersion = 0;
    @Column(name = "last_login_at") private Instant lastLoginAt;
}
