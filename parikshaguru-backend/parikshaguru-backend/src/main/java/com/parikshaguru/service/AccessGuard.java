package com.parikshaguru.service;

import com.parikshaguru.entity.Examination;
import com.parikshaguru.entity.Subject;
import com.parikshaguru.entity.enums.Role;
import com.parikshaguru.exception.ApiException;
import com.parikshaguru.security.AuthPrincipal;
import org.springframework.stereotype.Component;

/** Ownership / assignment checks for faculty. Call inside a transaction (touches lazy collections). */
@Component
public class AccessGuard {
    public boolean isAdmin(AuthPrincipal p) { return p.role() == Role.ADMIN; }

    public boolean isAssigned(Subject s, Long userId) {
        return s.getFaculty().stream().anyMatch(f -> f.getId().equals(userId));
    }

    public void requireSubjectAccess(AuthPrincipal p, Subject s) {
        if (isAdmin(p) || isAssigned(s, p.id())) return;
        throw ApiException.forbidden("You are not assigned to subject " + s.getCode());
    }

    public void requireExamAccess(AuthPrincipal p, Examination e) {
        if (isAdmin(p) || e.getCreatedBy().getId().equals(p.id()) || isAssigned(e.getSubject(), p.id())) return;
        throw ApiException.forbidden("You do not have access to examination " + e.getCode());
    }
}
