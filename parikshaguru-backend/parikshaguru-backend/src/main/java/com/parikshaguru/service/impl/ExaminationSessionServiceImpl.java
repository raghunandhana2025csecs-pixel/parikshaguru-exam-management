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

/**
 * Online examination engine. Server time (Clock) is the only authority for deadlines.
 * Concurrency: a pessimistic row lock on the session serialises answer saves / submit / auto-submit,
 * and unique constraints (session per exam+student, submission per session) guarantee no duplicates.
 */
@Service
@RequiredArgsConstructor
public class ExaminationSessionServiceImpl implements ExaminationSessionService {
    private static final Logger log = LoggerFactory.getLogger(ExaminationSessionServiceImpl.class);
    private static final int MAX_EVENTS_PER_SESSION = 5000;

    private final ExaminationRepository exams;
    private final ExamAssignmentRepository assignments;
    private final ExaminationSessionRepository sessions;
    private final QuestionRepository questions;
    private final StudentAnswerRepository answers;
    private final SubmissionRepository submissions;
    private final ProctoringEventRepository events;
    private final UserRepository users;
    private final EvaluationService evaluation;
    private final ResultService results;
    private final NotificationService notifications;
    private final AuditService audit;
    private final AccessGuard guard;
    private final EntityMapper mapper;
    private final Clock clock;

    // ------------------------------------------------------------------ start / resume
    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public SessionDto start(AuthPrincipal p, Long examId) {
        Examination exam = exams.findById(examId).orElseThrow(() -> ApiException.notFound("Examination not found"));
        // Locking the assignment row serialises concurrent "start" calls of the same student.
        assignments.lockByExamAndStudent(examId, p.id())
                .orElseThrow(() -> ApiException.forbidden("This examination is not assigned to you"));
        Instant now = clock.instant();

        Optional<ExaminationSession> existing = sessions.findByExamIdAndStudentId(examId, p.id());
        if (existing.isPresent()) {
            ExaminationSession s = existing.get();
            if (s.getStatus() == SessionStatus.IN_PROGRESS && !now.isBefore(s.getDeadline())) s = finalizeLocked(s.getId(), true);
            if (s.getStatus() != SessionStatus.IN_PROGRESS) throw ApiException.conflict("You have already attempted this examination; it was submitted");
            s.setLastActivityAt(now);
            return build(s, now);                                            // resume the authorised active session
        }
        if (exam.getStatus() == ExamStatus.DRAFT) throw ApiException.forbidden("This examination is not published");
        if (now.isBefore(exam.getStartTime())) throw ApiException.forbidden("The examination has not started yet (opens " + exam.getStartTime() + ")");
        if (!now.isBefore(exam.getEndTime())) throw ApiException.forbidden("The examination window has closed");
        if (questions.countByExamId(examId) == 0) throw ApiException.conflict("This examination has no questions");

        Instant byDuration = now.plus(Duration.ofMinutes(exam.getDurationMinutes()));
        ExaminationSession s = new ExaminationSession();
        s.setExam(exam);
        s.setStudent(users.getReferenceById(p.id()));
        s.setStartedAt(now);
        s.setDeadline(byDuration.isBefore(exam.getEndTime()) ? byDuration : exam.getEndTime());
        s.setStatus(SessionStatus.IN_PROGRESS);
        s.setIpAddress(RequestUtil.clientIp());
        s.setLastActivityAt(now);
        sessions.save(s);
        saveEvent(s, ProctorEventType.EXAM_START, "Examination started");
        audit.log(p.id(), "EXAM_STARTED", "SESSION", s.getId(), "exam=" + exam.getCode());
        return build(s, now);
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public SessionDto get(AuthPrincipal p, Long sessionId) {
        ExaminationSession s = loadOwned(p, sessionId);
        Instant now = clock.instant();
        if (s.getStatus() == SessionStatus.IN_PROGRESS) {
            if (!now.isBefore(s.getDeadline())) {
                s = finalizeLocked(sessionId, true);
            } else {
                if (s.getLastActivityAt() != null) {
                    long gap = Duration.between(s.getLastActivityAt(), now).getSeconds();
                    if (gap > 90) saveEvent(s, ProctorEventType.CONNECTION_RESTORED, "Reconnected after " + gap + "s without activity");
                }
                s.setLastActivityAt(now);
            }
        }
        return build(s, now);
    }

    // ------------------------------------------------------------------ answers
    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public ProgressDto saveAnswer(AuthPrincipal p, Long sessionId, SaveAnswerRequest req) {
        ExaminationSession s = loadOwnedLocked(p, sessionId);
        Instant now = clock.instant();
        ensureOpen(s, now);
        Question q = questions.findById(req.questionId())
                .filter(x -> x.getExam().getId().equals(s.getExam().getId()))
                .orElseThrow(() -> ApiException.badRequest("Question does not belong to this examination"));
        StudentAnswer a = answers.findBySessionIdAndQuestionId(sessionId, q.getId()).orElseGet(() -> {
            StudentAnswer n = new StudentAnswer();
            n.setSession(s);
            n.setQuestion(q);
            return n;
        });
        if (req.answer() != null) {
            String norm = normalize(q, req.answer());
            a.setSelectedAnswer(norm.isEmpty() ? null : norm);
            a.setAnsweredAt(now);
        }
        if (req.markedForReview() != null) a.setMarkedForReview(req.markedForReview());
        answers.save(a);
        s.setLastActivityAt(now);
        return progress(s, now);
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public ProgressDto markReview(AuthPrincipal p, Long sessionId, MarkReviewRequest req) {
        return saveAnswer(p, sessionId, new SaveAnswerRequest(req.questionId(), null, req.marked()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerDto> listAnswers(AuthPrincipal p, Long sessionId) {
        ExaminationSession s = loadOwned(p, sessionId);
        return answers.findBySessionId(s.getId()).stream()
                .map(a -> new AnswerDto(a.getQuestion().getId(), a.getSelectedAnswer(), a.isMarkedForReview())).toList();
    }

    // ------------------------------------------------------------------ submit
    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public SubmitResponse submit(AuthPrincipal p, Long sessionId) {
        ExaminationSession s = loadOwnedLocked(p, sessionId);
        if (s.getStatus() != SessionStatus.IN_PROGRESS) throw ApiException.conflict("This examination has already been submitted");
        boolean late = !clock.instant().isBefore(s.getDeadline());
        s = finalizeLocked(sessionId, late);
        String msg = late ? "Time was over; your saved answers were submitted automatically"
                : "Examination submitted successfully. Results appear after faculty approval.";
        return new SubmitResponse(sessionId, s.getStatus().name(), s.getSubmittedAt(), msg);
    }

    @Override
    @Transactional
    public void autoSubmit(Long sessionId) {
        ExaminationSession s = sessions.lockById(sessionId).orElse(null);
        if (s == null || s.getStatus() != SessionStatus.IN_PROGRESS) return;
        if (!clock.instant().isBefore(s.getDeadline())) finalizeLocked(sessionId, true);
    }

    /** Single place where a session is closed: records submission, auto-grades, creates the result - all in one transaction. */
    private ExaminationSession finalizeLocked(Long sessionId, boolean auto) {
        ExaminationSession s = sessions.lockById(sessionId).orElseThrow(() -> ApiException.notFound("Session not found"));
        if (s.getStatus() != SessionStatus.IN_PROGRESS) return s;                 // idempotent: never submits twice
        Instant now = clock.instant();
        s.setStatus(auto ? SessionStatus.AUTO_SUBMITTED : SessionStatus.SUBMITTED);
        s.setSubmittedAt(now.isAfter(s.getDeadline()) ? s.getDeadline() : now);
        sessions.save(s);
        if (!submissions.existsBySessionId(sessionId)) {
            Submission sub = new Submission();
            sub.setSession(s);
            sub.setExam(s.getExam());
            sub.setStudent(s.getStudent());
            sub.setSubmittedAt(s.getSubmittedAt());
            sub.setAutoSubmitted(auto);
            sub.setGradingStatus(GradingStatus.PENDING);
            submissions.save(sub);
            evaluation.autoEvaluate(sub, s);
            results.initialize(sub);
        }
        saveEvent(s, ProctorEventType.EXAM_END, auto ? "Auto-submitted at server deadline" : "Submitted by student");
        audit.log(s.getStudent().getId(), auto ? "EXAM_AUTO_SUBMITTED" : "EXAM_SUBMITTED", "SESSION", sessionId, "exam=" + s.getExam().getCode());
        notifications.notifyUser(s.getStudent(), "Examination submitted",
                s.getExam().getTitle() + " was " + (auto ? "auto-submitted" : "submitted") + ". Results will be published after faculty approval.", "EXAM");
        log.info("Session {} finalized (auto={})", sessionId, auto);
        return s;
    }

    // ------------------------------------------------------------------ proctoring
    @Override
    @Transactional
    public void logEvent(AuthPrincipal p, Long sessionId, ProctorEventRequest req) {
        if (req.eventType() == ProctorEventType.EXAM_START || req.eventType() == ProctorEventType.EXAM_END)
            throw ApiException.badRequest("EXAM_START and EXAM_END are recorded by the server only");
        ExaminationSession s = loadOwned(p, sessionId);
        if (s.getStatus() != SessionStatus.IN_PROGRESS) return;
        if (events.countBySessionId(sessionId) >= MAX_EVENTS_PER_SESSION) return;
        saveEvent(s, req.eventType(), req.details());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProctorEventDto> listEvents(AuthPrincipal p, Long sessionId) {
        ExaminationSession s = sessions.findById(sessionId).orElseThrow(() -> ApiException.notFound("Session not found"));
        guard.requireExamAccess(p, s.getExam());
        return events.findBySessionIdOrderByOccurredAtAsc(sessionId).stream()
                .map(e -> new ProctorEventDto(e.getId(), e.getEventType(), e.getDetails(), e.getOccurredAt())).toList();
    }

    // ------------------------------------------------------------------ helpers
    private ExaminationSession loadOwned(AuthPrincipal p, Long id) {
        ExaminationSession s = sessions.findById(id).orElseThrow(() -> ApiException.notFound("Session not found"));
        if (!s.getStudent().getId().equals(p.id())) throw ApiException.forbidden("You cannot access another student's examination session");
        return s;
    }

    private ExaminationSession loadOwnedLocked(AuthPrincipal p, Long id) {
        ExaminationSession s = sessions.lockById(id).orElseThrow(() -> ApiException.notFound("Session not found"));
        if (!s.getStudent().getId().equals(p.id())) throw ApiException.forbidden("You cannot access another student's examination session");
        return s;
    }

    private void ensureOpen(ExaminationSession s, Instant now) {
        if (s.getStatus() != SessionStatus.IN_PROGRESS) throw ApiException.conflict("Examination already submitted; answers can no longer be changed");
        if (!now.isBefore(s.getDeadline())) {
            finalizeLocked(s.getId(), true);
            throw ApiException.conflict("Time is over. Your saved answers were submitted automatically");
        }
    }

    private void saveEvent(ExaminationSession s, ProctorEventType type, String details) {
        ProctoringEvent e = new ProctoringEvent();
        e.setSession(s);
        e.setEventType(type);
        e.setDetails(details);
        e.setOccurredAt(clock.instant());
        events.save(e);
    }

    private String normalize(Question q, String raw) {
        Set<String> labels = q.getOptions().stream().map(QuestionOption::getLabel).collect(Collectors.toSet());
        switch (q.getQuestionType()) {
            case MCQ -> {
                String l = raw.trim().toUpperCase();
                if (!l.isEmpty() && !labels.contains(l)) throw ApiException.badRequest("Invalid option '" + l + "'");
                return l;
            }
            case MULTI_SELECT -> {
                String c = AnswerUtil.canonicalLabels(raw);
                if (!c.isEmpty() && !labels.containsAll(Arrays.asList(c.split(",")))) throw ApiException.badRequest("Invalid option selected");
                return c;
            }
            case TRUE_FALSE -> {
                String t = raw.trim().toUpperCase();
                if (!t.isEmpty() && !t.equals("TRUE") && !t.equals("FALSE")) throw ApiException.badRequest("Answer must be TRUE or FALSE");
                return t;
            }
            default -> { return raw.strip(); }
        }
    }

    private ProgressDto progress(ExaminationSession s, Instant now) {
        int total = (int) questions.countByExamId(s.getExam().getId());
        int answered = (int) answers.countAnswered(s.getId());
        boolean open = s.getStatus() == SessionStatus.IN_PROGRESS;
        long remaining = open ? Math.max(0, Duration.between(now, s.getDeadline()).getSeconds()) : 0;
        return new ProgressDto(s.getStatus().name(), total, answered, Math.max(0, total - answered),
                (int) answers.countMarked(s.getId()), remaining, now);
    }

    private SessionDto build(ExaminationSession s, Instant now) {
        Examination e = s.getExam();
        boolean open = s.getStatus() == SessionStatus.IN_PROGRESS;
        ProgressDto pr = progress(s, now);
        List<StudentQuestionDto> qs = List.of();
        if (open) {
            Map<Long, StudentAnswer> am = answers.findBySessionId(s.getId()).stream()
                    .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (x, y) -> x));
            qs = questions.findByExamIdOrderByOrderNoAscIdAsc(e.getId()).stream()
                    .map(q -> mapper.toStudentQuestionDto(q, am.get(q.getId()))).toList();
        }
        return new SessionDto(s.getId(), e.getId(), e.getCode(), e.getTitle(), e.getSubject().getName(), s.getStatus().name(),
                s.getStartedAt(), s.getDeadline(), now, pr.remainingSeconds(), e.isNegativeMarking(), e.getTotalMarks(),
                pr.totalQuestions(), pr.answered(), pr.unanswered(), pr.markedForReview(), qs,
                open ? null : "This examination has been submitted");
    }
}
