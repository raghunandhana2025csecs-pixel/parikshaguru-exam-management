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
public class StudentServiceImpl implements StudentService {
    private final UserRepository users;
    private final StudentRepository students;
    private final ExamAssignmentRepository assignments;
    private final ExaminationSessionRepository sessions;
    private final StudentAnswerRepository answers;
    private final QuestionRepository questions;
    private final ResultRepository results;
    private final NotificationRepository notifications;
    private final EntityMapper mapper;
    private final AuditService audit;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardDto dashboard(AuthPrincipal p) {
        User u = users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        Student s = students.findByUserId(p.id()).orElseThrow(() -> ApiException.notFound("Student profile not found"));
        List<StudentExamDto> exams = buildExams(p.id());
        long available = exams.stream().filter(e -> e.state().equals("AVAILABLE") || e.state().equals("UPCOMING")).count();
        long active = exams.stream().filter(e -> e.state().equals("IN_PROGRESS")).count();
        long completed = exams.stream().filter(e -> e.state().equals("SUBMITTED")).count();
        long pending = results.findForStudent(p.id()).stream().filter(r -> r.getStatus() != ResultStatus.PUBLISHED).count();
        List<NotificationDtos.NotificationDto> notes = notifications
                .findByUserIdOrderByCreatedAtDesc(p.id(), PageRequest.of(0, 5)).map(mapper::toNotificationDto).getContent();
        return new StudentDashboardDto(u.getFullName(), u.getIdentifier(), u.getDepartment() == null ? null : u.getDepartment().getName(),
                s.getCourse(), s.getSemester(), available, active, completed, pending,
                notifications.countByUserIdAndSeenFalse(p.id()), exams, notes);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileDto getProfile(AuthPrincipal p) { return profile(p.id()); }

    @Override
    @Transactional
    public StudentProfileDto updateProfile(AuthPrincipal p, UpdateStudentProfileRequest req) {
        User u = users.findById(p.id()).orElseThrow(() -> ApiException.notFound("User not found"));
        Student s = students.findByUserId(p.id()).orElseThrow(() -> ApiException.notFound("Student profile not found"));
        if (req.fullName() != null && !req.fullName().isBlank()) u.setFullName(req.fullName().trim());
        if (req.phone() != null) s.setPhone(req.phone().trim());
        audit.log(p.id(), "PROFILE_UPDATED", "USER", p.id(), null);
        return profile(p.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentExamDto> exams(AuthPrincipal p, String state) {
        List<StudentExamDto> all = buildExams(p.id());
        if (state == null || state.isBlank()) return all;
        return all.stream().filter(e -> e.state().equalsIgnoreCase(state.trim())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResultsDto results(AuthPrincipal p) {
        List<Result> rs = results.findForStudent(p.id());
        List<ResultDto> dtos = rs.stream().map(r -> mapper.toResultDto(r, true)).toList();
        long pub = rs.stream().filter(r -> r.getStatus() == ResultStatus.PUBLISHED).count();
        Double avg = rs.stream().filter(r -> r.getStatus() == ResultStatus.PUBLISHED)
                .mapToDouble(Result::getPercentage).average().stream().boxed().findFirst().map(AnswerUtil::round2).orElse(null);
        return new StudentResultsDto(dtos, pub, rs.size() - pub, avg);
    }

    private StudentProfileDto profile(Long uid) {
        User u = users.findById(uid).orElseThrow(() -> ApiException.notFound("User not found"));
        Student s = students.findByUserId(uid).orElseThrow(() -> ApiException.notFound("Student profile not found"));
        return new StudentProfileDto(u.getId(), u.getFullName(), u.getEmail(), u.getIdentifier(),
                u.getDepartment() == null ? null : u.getDepartment().getName(), s.getCourse(), s.getSemester(), s.getPhone());
    }

    private List<StudentExamDto> buildExams(Long uid) {
        Instant now = clock.instant();
        Map<Long, ExaminationSession> byExam = sessions.findByStudentId(uid).stream()
                .collect(Collectors.toMap(s -> s.getExam().getId(), s -> s, (a, b) -> a));
        List<StudentExamDto> out = new ArrayList<>();
        for (ExamAssignment a : assignments.findForStudent(uid)) {
            Examination e = a.getExam();
            if (e.getStatus() == ExamStatus.DRAFT) continue;
            ExaminationSession s = byExam.get(e.getId());
            String state;
            long remaining = 0;
            int answered = 0;
            if (s != null) {
                if (s.getStatus() == SessionStatus.IN_PROGRESS && now.isBefore(s.getDeadline())) {
                    state = "IN_PROGRESS";
                    remaining = Math.max(0, Duration.between(now, s.getDeadline()).getSeconds());
                    answered = (int) answers.countAnswered(s.getId());
                } else state = "SUBMITTED";
            } else if (now.isBefore(e.getStartTime())) state = "UPCOMING";
            else if (now.isAfter(e.getEndTime())) state = "MISSED";
            else state = "AVAILABLE";
            long qc = questions.countByExamId(e.getId());
            out.add(new StudentExamDto(e.getId(), e.getCode(), e.getTitle(), e.getSubject().getCode(), e.getSubject().getName(),
                    e.getStartTime(), e.getEndTime(), e.getDurationMinutes(), e.getTotalMarks(),
                    qc > 0 ? (int) qc : e.getNumberOfQuestions(), e.isNegativeMarking(), e.getInstructions(), state,
                    s == null ? null : s.getId(), state.equals("AVAILABLE"), state.equals("IN_PROGRESS"), remaining, answered));
        }
        return out;
    }
}
