package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface ResultRepository extends JpaRepository<Result, Long> {
    String SCOPE = " (:admin = true or r.exam.createdBy.id = :uid or exists (select 1 from Subject sb join sb.faculty f where sb.id = r.exam.subject.id and f.id = :uid)) ";

    Optional<Result> findBySubmissionId(Long submissionId);

    @Query("select r from Result r join fetch r.exam e join fetch e.subject where r.student.id = :sid order by r.createdAt desc")
    List<Result> findForStudent(@Param("sid") Long studentId);

    @Query("select r from Result r where (:examId = 0 or r.exam.id = :examId) and (:status = '' or str(r.status) = :status) and" + SCOPE)
    Page<Result> search(@Param("examId") long examId, @Param("status") String status, @Param("admin") boolean admin,
                        @Param("uid") Long uid, Pageable pageable);

    @Query("select count(r) from Result r where r.status = :status and" + SCOPE)
    long countScoped(@Param("status") ResultStatus status, @Param("admin") boolean admin, @Param("uid") Long uid);
}
