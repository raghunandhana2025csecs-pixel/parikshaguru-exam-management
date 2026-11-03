package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    String SCOPE = " (:admin = true or e.createdBy.id = :uid or exists (select 1 from Subject sb join sb.faculty f where sb.id = e.subject.id and f.id = :uid)) ";

    boolean existsByCodeIgnoreCase(String code);
    Optional<Examination> findByCodeIgnoreCase(String code);
    boolean existsBySubjectId(Long subjectId);

    @Query("select e from Examination e where (lower(e.title) like lower(concat('%', :q, '%')) or lower(e.code) like lower(concat('%', :q, '%'))) "
            + "and (:status = '' or str(e.status) = :status) and" + SCOPE)
    Page<Examination> search(@Param("q") String q, @Param("status") String status, @Param("admin") boolean admin,
                             @Param("uid") Long uid, Pageable pageable);

    @Query("select count(e) from Examination e where e.status in :statuses and" + SCOPE)
    long countScoped(@Param("statuses") Collection<ExamStatus> statuses, @Param("admin") boolean admin, @Param("uid") Long uid);

    @Query("select count(e) from Examination e where e.status in :statuses and e.startTime <= :now and e.endTime >= :now and" + SCOPE)
    long countActiveScoped(@Param("statuses") Collection<ExamStatus> statuses, @Param("now") Instant now,
                           @Param("admin") boolean admin, @Param("uid") Long uid);

    @Modifying
    @Query("update Examination e set e.status = :to where e.status = :from and e.startTime <= :now and e.endTime > :now")
    int activate(@Param("from") ExamStatus from, @Param("to") ExamStatus to, @Param("now") Instant now);

    @Modifying
    @Query("update Examination e set e.status = :to where e.status in :from and e.endTime <= :now")
    int complete(@Param("from") Collection<ExamStatus> from, @Param("to") ExamStatus to, @Param("now") Instant now);
}
