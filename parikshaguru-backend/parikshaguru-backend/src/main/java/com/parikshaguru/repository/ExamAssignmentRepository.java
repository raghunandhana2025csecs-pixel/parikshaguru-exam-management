package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface ExamAssignmentRepository extends JpaRepository<ExamAssignment, Long> {
    boolean existsByExamIdAndStudentId(Long examId, Long studentId);
    long countByExamId(Long examId);
    void deleteByExamId(Long examId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ExamAssignment a where a.exam.id = :examId and a.student.id = :sid")
    Optional<ExamAssignment> lockByExamAndStudent(@Param("examId") Long examId, @Param("sid") Long studentId);

    @Query("select a from ExamAssignment a join fetch a.exam e join fetch e.subject where a.student.id = :sid order by e.startTime desc")
    List<ExamAssignment> findForStudent(@Param("sid") Long studentId);
}
