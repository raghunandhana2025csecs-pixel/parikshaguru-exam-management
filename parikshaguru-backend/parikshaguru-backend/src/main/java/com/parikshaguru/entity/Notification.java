package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "notifications", indexes = @Index(name = "idx_notification_user", columnList = "user_id,is_read"))
@Getter @Setter @NoArgsConstructor
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String message;
    @Column(nullable = false, length = 30) private String type = "INFO";
    @Column(name = "is_read", nullable = false) private boolean seen;
}
