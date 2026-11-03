package com.parikshaguru.repository;

import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByIdentifierIgnoreCase(String identifier);
    long countByRoleAndStatusIn(Role role, Collection<AccountStatus> statuses);
    long countByStatus(AccountStatus status);
    List<User> findByRoleAndStatusIn(Role role, Collection<AccountStatus> statuses);

    @Query("""
        select u from User u where (:role = '' or str(u.role) = :role) and (:status = '' or str(u.status) = :status)
        and (lower(u.fullName) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%'))
             or lower(u.identifier) like lower(concat('%', :q, '%')))
        """)
    Page<User> search(@Param("role") String role, @Param("status") String status, @Param("q") String q, Pageable pageable);
}
