package com.parikshaguru.entity;

import com.parikshaguru.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "question_options", uniqueConstraints = @UniqueConstraint(name = "uk_option_label", columnNames = {"question_id", "label"}))
@Getter @Setter @NoArgsConstructor
public class QuestionOption extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id", nullable = false) private Question question;
    @Column(nullable = false, length = 2) private String label;
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT") private String optionText;
}
