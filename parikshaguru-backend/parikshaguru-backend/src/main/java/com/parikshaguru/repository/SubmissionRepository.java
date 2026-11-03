package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    String SCOPE = " (:admin = true or s.exam.createdBy.id = :uid or exists (select 1 from Subject sb join sb.faculty f where sb.id = s.exam.subject.id and f.id = :uid)) ";

    boolean existsBySessionId(Long sessionId);
    long countByExamId(Long examId);

    @Query("select s from Submission s where (:examId = 0 or s.exam.id = :examId) and (:status = '' or str(s.gradingStatus) = :status) and" + SCOPE)
    Page<Submission> search(@Param("examId") long examId, @Param("status") String status, @Param("admin") boolean admin,
                            @Param("uid") Long uid, Pageable pageable);

    @Query("select count(s) from Submission s where s.gradingStatus <> :done and" + SCOPE)
    long countNotGraded(@Param("done") GradingStatus done, @Param("admin") boolean admin, @Param("uid") Long uid);
}
