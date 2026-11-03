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
public class ExamServiceImpl implements ExamService {
    private static final List<AccountStatus> LIVE = List.of(AccountStatus.ACTIVE, AccountStatus.APPROVED);
    private final ExaminationRepository exams;
    private final SubjectRepository subjects;
    private final UserRepository users;
    private final QuestionRepository questions;
    private final ExamAssignmentRepository assignments;
    private final ExaminationSessionRepository sessions;
    private final SubmissionRepository submissions;
    private final StudentRepository students;
    private final AccessGuard guard;
    private final EntityMapper mapper;
    private final AuditService audit;
    private final NotificationService notifications;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExamDto> list(AuthPrincipal p, String q, String status, int page, int size) {
        Page<Examination> pg = exams.search(PageUtil.q(q), PageUtil.upper(status), guard.isAdmin(p), p.id(),
                PageUtil.of(page, size, Sort.by(Sort.Direction.DESC, "startTime")));
        return PageResponse.of(pg.map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDto get(AuthPrincipal p, Long id) {
        Examination e = load(id);
        guard.requireExamAccess(p, e);
        return toDto(e);
    }

    @Override
    @Transactional
    public ExamDto create(AuthPrincipal p, ExamRequest r) {
        Subject s = subjects.findById(r.subjectId()).orElseThrow(() -> ApiException.notFound("Subject not found"));
        guard.requireSubjectAccess(p, s);
        validateWindow(r);
        String code = PageUtil.upper(r.code());
        if (exams.existsByCodeIgnoreCase(code)) throw ApiException.conflict("Examination code already exists");
        Examination e = new Examination();
        apply(e, r, s);
        e.setStatus(ExamStatus.DRAFT);
        e.setCreatedBy(users.getReferenceById(p.id()));
        exams.save(e);
        audit.log(p.id(), "EXAM_CREATED", "EXAM", e.getId(), e.getCode());
        return toDto(e);
    }

    @Override
    @Transactional
    public ExamDto update(AuthPrincipal p, Long id, ExamRequest r) {
        Examination e = load(id);
        guard.requireExamAccess(p, e);
        if (e.getStatus() != ExamStatus.DRAFT) throw ApiException.conflict("Only DRAFT examinations can be edited");
        Subject s = subjects.findById(r.subjectId()).orElseThrow(() -> ApiException.notFound("Subject not found"));
        guard.requireSubjectAccess(p, s);
        validateWindow(r);
        exams.findByCodeIgnoreCase(PageUtil.upper(r.code())).filter(o -> !o.getId().equals(id))
                .ifPresent(o -> { throw ApiException.conflict("Examination code already exists"); });
        apply(e, r, s);
        audit.log(p.id(), "EXAM_UPDATED", "EXAM", id, e.getCode());
        return toDto(e);
    }

    @Override
    @Transactional
    public void delete(AuthPrincipal p, Long id) {
        Examination e = load(id);
        guard.requireExamAccess(p, e);
        if (e.getStatus() != ExamStatus.DRAFT) throw ApiException.conflict("Only DRAFT examinations can be deleted");
        if (sessions.existsByExamId(id)) throw ApiException.conflict("Examination already has attempts");
        questions.deleteByExamId(id);
        assignments.deleteByExamId(id);
        exams.delete(e);
        audit.log(p.id(), "EXAM_DELETED", "EXAM", id, e.getCode());
    }

    @Override
    @Transactional
    public ExamDto publish(AuthPrincipal p, Long id) {
        Examination e = load(id);
        guard.requireExamAccess(p, e);
        if (e.getStatus() != ExamStatus.DRAFT) throw ApiException.conflict("Examination is already published");
        long qc = questions.countByExamId(id);
        if (qc == 0) throw ApiException.badRequest("Add at least one question before publishing");
        double sum = questions.sumMarks(id);
        if (Math.abs(sum - e.getTotalMarks()) > 0.001)
            throw ApiException.badRequest(String.format("Sum of question marks (%.2f) must equal the examination total marks (%.2f)", sum, e.getTotalMarks()));
        if (!e.getEndTime().isAfter(clock.instant())) throw ApiException.badRequest("End time has already passed; edit the schedule first");
        e.setStatus(ExamStatus.PUBLISHED);
        int n = assignEligible(e);
        audit.log(p.id(), "EXAM_PUBLISHED", "EXAM", id, "assigned=" + n);
        return toDto(e);
    }

    @Override
    @Transactional
    public MessageResponse assign(AuthPrincipal p, Long id, AssignRequest req) {
        Examination e = load(id);
        guard.requireExamAccess(p, e);
        if (e.getStatus() == ExamStatus.COMPLETED) throw ApiException.conflict("Examination is completed");
        int n = 0;
        if (req == null || req.studentIds() == null || req.studentIds().isEmpty()) {
            n = assignEligible(e);
        } else {
            for (Long sid : req.studentIds()) {
                User u = users.findById(sid).orElseThrow(() -> ApiException.badRequest("Student not found: " + sid));
                if (u.getRole() != Role.STUDENT) throw ApiException.badRequest("User " + sid + " is not a student");
                if (addAssignment(e, u)) n++;
            }
        }
        audit.log(p.id(), "EXAM_ASSIGNED", "EXAM", id, "newAssignments=" + n);
        return new MessageResponse("Examination assigned to " + n + " new student(s)");
    }

    @Override
    @Transactional
    public void refreshStatuses() {
        Instant now = clock.instant();
        exams.activate(ExamStatus.PUBLISHED, ExamStatus.ACTIVE, now);
        exams.complete(List.of(ExamStatus.PUBLISHED, ExamStatus.ACTIVE), ExamStatus.COMPLETED, now);
    }

    // ---------------------------------------------------------------- helpers
    private Examination load(Long id) { return exams.findById(id).orElseThrow(() -> ApiException.notFound("Examination not found")); }

    private ExamDto toDto(Examination e) {
        return mapper.toExamDto(e, questions.countByExamId(e.getId()), assignments.countByExamId(e.getId()), submissions.countByExamId(e.getId()));
    }

    private void validateWindow(ExamRequest r) {
        if (!r.endTime().isAfter(r.startTime())) throw ApiException.badRequest("End time must be after start time");
    }

    private void apply(Examination e, ExamRequest r, Subject s) {
        e.setTitle(r.title().trim());
        e.setCode(PageUtil.upper(r.code()));
        e.setSubject(s);
        e.setSemester(r.semester() != null ? r.semester() : s.getSemester());
        e.setExamType(r.examType() == null || r.examType().isBlank() ? "MID_TERM" : r.examType().trim().toUpperCase());
        e.setTotalMarks(r.totalMarks());
        e.setDurationMinutes(r.durationMinutes());
        e.setStartTime(r.startTime());
        e.setEndTime(r.endTime());
        e.setNegativeMarking(Boolean.TRUE.equals(r.negativeMarking()));
        e.setNumberOfQuestions(r.numberOfQuestions() == null ? 0 : r.numberOfQuestions());
        e.setInstructions(r.instructions());
    }

    private int assignEligible(Examination e) {
        int n = 0;
        Long deptId = e.getSubject().getDepartment().getId();
        for (User u : students.findEligible(e.getSemester(), deptId, LIVE)) if (addAssignment(e, u)) n++;
        return n;
    }

    private boolean addAssignment(Examination e, User student) {
        if (assignments.existsByExamIdAndStudentId(e.getId(), student.getId())) return false;
        ExamAssignment a = new ExamAssignment();
        a.setExam(e); a.setStudent(student);
        assignments.save(a);
        if (e.getStatus() != ExamStatus.DRAFT)
            notifications.notifyUser(student, "New examination assigned", e.getTitle() + " (" + e.getCode() + ") opens at " + e.getStartTime(), "EXAM");
        return true;
    }
}
