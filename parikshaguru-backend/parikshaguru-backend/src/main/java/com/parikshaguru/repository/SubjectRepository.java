package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Subject> findByCodeIgnoreCase(String code);

    @Query("""
        select s from Subject s where
        (lower(s.name) like lower(concat('%', :q, '%')) or lower(s.code) like lower(concat('%', :q, '%')))
        and (:semester = 0 or s.semester = :semester)
        and (:mine = false or exists (select 1 from Subject s2 join s2.faculty f where s2.id = s.id and f.id = :uid))
        """)
    Page<Subject> search(@Param("q") String q, @Param("semester") int semester, @Param("mine") boolean mine,
                         @Param("uid") Long uid, Pageable pageable);

    @Query("select count(s) from Subject s where :admin = true or exists (select 1 from Subject s2 join s2.faculty f where s2.id = s.id and f.id = :uid)")
    long countScoped(@Param("admin") boolean admin, @Param("uid") Long uid);
}
