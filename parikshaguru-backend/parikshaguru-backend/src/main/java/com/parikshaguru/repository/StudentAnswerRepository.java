package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findBySessionId(Long sessionId);
    Optional<StudentAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    @Query("select count(a) from StudentAnswer a where a.session.id = :sid and a.selectedAnswer is not null and a.selectedAnswer <> ''")
    long countAnswered(@Param("sid") Long sessionId);

    @Query("select count(a) from StudentAnswer a where a.session.id = :sid and a.markedForReview = true")
    long countMarked(@Param("sid") Long sessionId);
}
