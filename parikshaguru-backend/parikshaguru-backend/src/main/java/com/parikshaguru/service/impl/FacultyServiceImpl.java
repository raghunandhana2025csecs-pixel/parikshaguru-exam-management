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
import com.parikshaguru.service.evaluator.*;
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
public class FacultyServiceImpl implements FacultyService {
    private static final List<AccountStatus> LIVE = List.of(AccountStatus.ACTIVE, AccountStatus.APPROVED);
    private final UserRepository users;
    private final FacultyRepository faculties;
    private final SubjectRepository subjects;
    private final ExaminationRepository exams;
    private final SubmissionRepository submissions;
    private final ResultRepository results;
    private final DepartmentRepository departments;
    private final AccessGuard guard;
    private final EntityMapper mapper;
    private final NotificationService notifications;
    private final AuditService audit;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto dashboard(AuthPrincipal p) {
        boolean admin = guard.isAdmin(p);
        Long uid = p.id();
        Instant now = clock.instant();
        List<ExamStatus> live = List.of(ExamStatus.PUBLISHED, ExamStatus.ACTIVE);
        return new DashboardDto(
                users.countByRoleAndStatusIn(Role.STUDENT, LIVE),
                users.countByRoleAndStatusIn(Role.FACULTY, LIVE),
                subjects.countScoped(admin, uid),
                exams.countActiveScoped(live, now, admin, uid),
                exams.countScoped(live, admin, uid),
                exams.countScoped(List.of(ExamStatus.DRAFT), admin, uid),
                submissions.countNotGraded(GradingStatus.GRADED, admin, uid),
                results.countScoped(ResultStatus.PENDING_APPROVAL, admin, uid),
                admin ? users.countByStatus(AccountStatus.PENDING) : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getProfile(AuthPrincipal p) {
        return mapper.toUserDto(users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found")));
    }

    @Override
    @Transactional
    public UserDto updateProfile(AuthPrincipal p, UpdateStudentProfileRequest req) {
        User u = users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        if (req.fullName() != null && !req.fullName().isBlank()) u.setFullName(req.fullName().trim());
        if (req.phone() != null) faculties.findByUserId(p.id()).ifPresent(f -> f.setPhone(req.phone().trim()));
        audit.log(p.id(), "PROFILE_UPDATED", "USER", p.id(), null);
        return mapper.toUserDto(u);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> listUsers(AuthPrincipal p, String role, String status, String q, int page, int size) {
        String r = p.role() == Role.FACULTY ? "STUDENT" : PageUtil.upper(role);   // faculty can only browse students
        Page<User> pg = users.search(r, PageUtil.upper(status), PageUtil.q(q), PageUtil.of(page, size, Sort.by("fullName")));
        return PageResponse.of(pg.map(mapper::toUserDto));
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(AuthPrincipal p, Long userId, AccountStatus status) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        if (u.getId().equals(p.id())) throw ApiException.conflict("You cannot change your own account status");
        if (u.getRole() == Role.ADMIN) throw ApiException.conflict("Administrator accounts cannot be changed here");
        u.setStatus(status);
        if (status == AccountStatus.BLOCKED || status == AccountStatus.REJECTED) u.setTokenVersion(u.getTokenVersion() + 1);
        audit.log(p.id(), "USER_STATUS_" + status, "USER", userId, null);
        notifications.notifyUser(u, "Account status updated", "Your account status is now " + status, "ACCOUNT");
        return mapper.toUserDto(u);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> departments() {
        return departments.findAll(Sort.by("name")).stream().map(d -> new DepartmentDto(d.getId(), d.getCode(), d.getName())).toList();
    }
}
