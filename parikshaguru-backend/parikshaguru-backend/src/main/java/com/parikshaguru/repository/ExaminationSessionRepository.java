package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface ExaminationSessionRepository extends JpaRepository<ExaminationSession, Long> {
    Optional<ExaminationSession> findByExamIdAndStudentId(Long examId, Long studentId);
    List<ExaminationSession> findByStudentId(Long studentId);
    boolean existsByExamId(Long examId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ExaminationSession s where s.id = :id")
    Optional<ExaminationSession> lockById(@Param("id") Long id);

    @Query("select s.id from ExaminationSession s where s.status = :status and s.deadline <= :now")
    List<Long> findExpiredIds(@Param("status") SessionStatus status, @Param("now") Instant now);
}
