package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name = "departments")
@Getter @Setter @NoArgsConstructor
public class Department extends BaseEntity {
    @Column(nullable = false, unique = true, length = 20) private String code;
    @Column(nullable = false, unique = true, length = 120) private String name;
    public Department(String code, String name) { this.code = code; this.name = name; }
}
