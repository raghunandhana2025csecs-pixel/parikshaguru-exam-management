package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
        @Index(name = "idx_audit_user", columnList = "user_id")})
@Getter @Setter @NoArgsConstructor
public class AuditLog extends BaseEntity {
    @Column(name = "user_id") private Long userId;
    @Column(nullable = false, length = 60) private String action;
    @Column(name = "entity_type", length = 40) private String entityType;
    @Column(name = "entity_id") private Long entityId;
    @Column(length = 1000) private String details;
    @Column(name = "ip_address", length = 64) private String ipAddress;
}
