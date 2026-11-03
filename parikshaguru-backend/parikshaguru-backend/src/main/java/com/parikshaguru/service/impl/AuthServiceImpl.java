package com.parikshaguru.service.impl;

import com.parikshaguru.dto.*;
import com.parikshaguru.dto.AuthDtos.*;
import com.parikshaguru.dto.CommonDtos.*;
import com.parikshaguru.dto.ExamDtos.*;
import com.parikshaguru.dto.NotificationDtos.*;
import com.parikshaguru.dto.QuestionDtos.*;
import com.parikshaguru.dto.ResultDtos.*;
import com.parikshaguru.dto.SessionDtos.*;
import com.parikshaguru.dto.StudentDtos.*;
import com.parikshaguru.dto.SubjectDtos.*;
import com.parikshaguru.dto.AdminDtos.*;
import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import com.parikshaguru.exception.ApiException;
import com.parikshaguru.mapper.EntityMapper;
import com.parikshaguru.repository.*;
import com.parikshaguru.security.*;
import com.parikshaguru.service.*;
import com.parikshaguru.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final StudentRepository students;
    private final FacultyRepository faculties;
    private final PasswordResetTokenRepository resetTokens;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;
    private final JwtService jwt;
    private final com.parikshaguru.config.AppProperties props;
    private final EntityMapper mapper;
    private final AuditService audit;
    private final Clock clock;

    @Override
    @Transactional
    public UserDto register(RegisterRequest r) {
        if (!r.password().equals(r.confirmPassword())) throw ApiException.badRequest("Passwords do not match");
        if (r.role() == Role.ADMIN) throw ApiException.forbidden("Administrator accounts cannot be self-registered");
        String email = r.email().trim().toLowerCase();
        validateDomain(email);
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("Email is already registered");
        String ident = r.identifier().trim().toUpperCase();
        if (users.existsByIdentifierIgnoreCase(ident)) throw ApiException.conflict("Roll number / Employee ID is already registered");
        Department dept = resolveDepartment(r.department());

        AccountStatus status;
        if (r.role() == Role.STUDENT) {
            if (r.course() == null || r.course().isBlank() || r.semester() == null)
                throw ApiException.badRequest("Course and semester are required for students");
            status = props.getAuth().isStudentAutoActivate() ? AccountStatus.ACTIVE : AccountStatus.PENDING;
        } else {
            boolean hasCode = r.inviteCode() != null && !r.inviteCode().isBlank();
            if (hasCode && !constantTimeEquals(r.inviteCode().trim(), props.getAuth().getFacultyInviteCode()))
                throw ApiException.badRequest("Invalid faculty invitation code");
            status = hasCode ? AccountStatus.ACTIVE : AccountStatus.PENDING;   // no code => needs admin approval
        }

        User u = new User();
        u.setFullName(r.fullName().trim()); u.setEmail(email); u.setIdentifier(ident);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(r.role()); u.setStatus(status); u.setDepartment(dept);
        users.save(u);

        if (r.role() == Role.STUDENT) {
            Student s = new Student();
            s.setUser(u); s.setCourse(r.course().trim()); s.setSemester(r.semester());
            students.save(s);
        } else {
            Faculty f = new Faculty();
            f.setUser(u); f.setDesignation("Faculty");
            faculties.save(f);
        }
        audit.log(u.getId(), "USER_REGISTERED", "USER", u.getId(), "role=" + u.getRole() + " status=" + status);
        return mapper.toUserDto(u);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmailIgnoreCase(r.email().trim()).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPasswordHash())) {
            if (u != null) audit.log(u.getId(), "LOGIN_FAILED", "USER", u.getId(), null);
            throw ApiException.unauthorized("Invalid email or password");
        }
        switch (u.getStatus()) {
            case PENDING -> throw ApiException.forbidden("Your account is awaiting administrator approval");
            case REJECTED -> throw ApiException.forbidden("Your registration was rejected. Contact the examination office");
            case BLOCKED -> throw ApiException.forbidden("Your account is blocked. Contact the examination office");
            default -> { }
        }
        u.setLastLoginAt(clock.instant());
        audit.log(u.getId(), "LOGIN", "USER", u.getId(), null);
        return new AuthResponse(jwt.generate(u), "Bearer", jwt.expiresInSeconds(), mapper.toUserDto(u));
    }

    @Override
    @Transactional
    public void logout(AuthPrincipal p) {
        User u = users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        u.setTokenVersion(u.getTokenVersion() + 1);          // invalidates every JWT issued before now
        audit.log(u.getId(), "LOGOUT", "USER", u.getId(), null);
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest r) {
        String generic = "If the email is registered, password reset instructions have been issued";
        User u = users.findByEmailIgnoreCase(r.email().trim()).orElse(null);
        if (u == null || !u.getStatus().canLogin()) return new ForgotPasswordResponse(generic, null);
        resetTokens.findByUserIdAndUsedFalse(u.getId()).forEach(t -> t.setUsed(true));
        byte[] raw = new byte[32];
        new java.security.SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        PasswordResetToken t = new PasswordResetToken();
        t.setUser(u); t.setTokenHash(sha256(token));
        t.setExpiresAt(clock.instant().plus(Duration.ofMinutes(props.getAuth().getResetTokenMinutes())));
        resetTokens.save(t);
        audit.log(u.getId(), "PASSWORD_RESET_REQUESTED", "USER", u.getId(), null);
        // No SMTP configured: plug a mail sender in here. In dev the token is returned in the response.
        log.info("Password reset token issued for user {}", u.getId());
        return new ForgotPasswordResponse(generic, props.getAuth().isExposeResetToken() ? token : null);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest r) {
        if (!r.newPassword().equals(r.confirmPassword())) throw ApiException.badRequest("Passwords do not match");
        PasswordResetToken t = resetTokens.findByTokenHash(sha256(r.token().trim()))
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset token"));
        if (t.isUsed() || t.getExpiresAt().isBefore(clock.instant())) throw ApiException.badRequest("Invalid or expired reset token");
        User u = t.getUser();
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setTokenVersion(u.getTokenVersion() + 1);
        t.setUsed(true);
        audit.log(u.getId(), "PASSWORD_RESET", "USER", u.getId(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto me(AuthPrincipal p) {
        return mapper.toUserDto(users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found")));
    }

    @Override
    @Transactional
    public AuthResponse changePassword(AuthPrincipal p, ChangePasswordRequest r) {
        if (!r.newPassword().equals(r.confirmPassword())) throw ApiException.badRequest("Passwords do not match");
        User u = users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) throw ApiException.badRequest("Current password is incorrect");
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setTokenVersion(u.getTokenVersion() + 1);
        audit.log(u.getId(), "PASSWORD_CHANGED", "USER", u.getId(), null);
        return new AuthResponse(jwt.generate(u), "Bearer", jwt.expiresInSeconds(), mapper.toUserDto(u));
    }

    private void validateDomain(String email) {
        List<String> allowed = props.getAuth().getAllowedEmailDomains();
        if (allowed == null || allowed.isEmpty()) return;
        String domain = email.substring(email.indexOf('@') + 1);
        boolean ok = allowed.stream().map(String::trim).filter(s -> !s.isEmpty()).anyMatch(d -> d.equalsIgnoreCase(domain));
        if (!ok) throw ApiException.badRequest("Please use your institutional email address (" + String.join(", ", allowed) + ")");
    }

    private Department resolveDepartment(String value) {
        String v = value.trim();
        return departments.findByCodeIgnoreCase(v).or(() -> departments.findByNameIgnoreCase(v))
                .orElseThrow(() -> ApiException.badRequest("Unknown department: " + v + " (see GET /api/departments)"));
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (b == null) return false;
        return java.security.MessageDigest.isEqual(a.getBytes(java.nio.charset.StandardCharsets.UTF_8), b.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static String sha256(String s) {
        try {
            byte[] d = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
