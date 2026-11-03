package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCodeIgnoreCase(String code);
    Optional<Department> findByNameIgnoreCase(String name);
}
