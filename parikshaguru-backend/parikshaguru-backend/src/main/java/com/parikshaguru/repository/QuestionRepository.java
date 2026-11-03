package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamIdOrderByOrderNoAscIdAsc(Long examId);
    long countByExamId(Long examId);
    void deleteByExamId(Long examId);

    @Query("select coalesce(sum(q.marks), 0) from Question q where q.exam.id = :examId")
    double sumMarks(@Param("examId") Long examId);

    @Query("select coalesce(max(q.orderNo), 0) from Question q where q.exam.id = :examId")
    int maxOrderNo(@Param("examId") Long examId);
}
