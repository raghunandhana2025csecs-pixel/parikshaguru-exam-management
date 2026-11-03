package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    @Query("select s.user from Student s where s.semester = :sem and s.user.department.id = :dept and s.user.status in :statuses")
    List<User> findEligible(@Param("sem") int semester, @Param("dept") Long departmentId, @Param("statuses") Collection<AccountStatus> statuses);

    @Query("select s.user from Student s where s.semester = :sem and s.user.status in :statuses")
    List<User> findBySemester(@Param("sem") int semester, @Param("statuses") Collection<AccountStatus> statuses);
}
