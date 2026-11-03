package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface ProctoringEventRepository extends JpaRepository<ProctoringEvent, Long> {
    List<ProctoringEvent> findBySessionIdOrderByOccurredAtAsc(Long sessionId);
    long countBySessionId(Long sessionId);

    @Query("select e.eventType, count(e) from ProctoringEvent e where e.session.id = :sid group by e.eventType")
    List<Object[]> countByType(@Param("sid") Long sessionId);
}
