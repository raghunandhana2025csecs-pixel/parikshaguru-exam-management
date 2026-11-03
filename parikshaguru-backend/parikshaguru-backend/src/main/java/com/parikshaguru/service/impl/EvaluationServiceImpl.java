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
public class EvaluationServiceImpl implements EvaluationService {
    private final EvaluatorRegistry registry;
    private final QuestionRepository questions;
    private final StudentAnswerRepository answers;
    private final EvaluationRepository evaluations;
    private final SubmissionRepository submissions;
    private final ResultRepository resultRepo;
    private final ResultService resultService;
    private final SubmissionService submissionService;
    private final UserRepository users;
    private final AccessGuard guard;
    private final AuditService audit;
    private final Clock clock;

    @Override
    @Transactional
    public void autoEvaluate(Submission sub, ExaminationSession session) {
        Examination exam = sub.getExam();
        Map<Long, StudentAnswer> am = answers.findBySessionId(session.getId()).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (x, y) -> x));
        double objective = 0;
        boolean needsManual = false;
        List<Evaluation> rows = new ArrayList<>();
        for (Question q : questions.findByExamIdOrderByOrderNoAscIdAsc(exam.getId())) {
            StudentAnswer a = am.get(q.getId());
            Double score = registry.forType(q.getQuestionType())
                    .evaluate(q, a == null ? null : a.getSelectedAnswer(), exam.isNegativeMarking());
            Evaluation ev = new Evaluation();
            ev.setSubmission(sub);
            ev.setQuestion(q);
            ev.setMarksAwarded(score);
            ev.setAutoGraded(score != null);
            if (score == null) needsManual = true; else objective += score;
            rows.add(ev);
        }
        evaluations.saveAll(rows);
        sub.setObjectiveMarks(AnswerUtil.round2(objective));
        sub.setTotalMarks(Math.max(0, AnswerUtil.round2(objective)));
        sub.setGradingStatus(needsManual ? GradingStatus.PENDING : GradingStatus.GRADED);
        if (!needsManual) sub.setEvaluatedAt(clock.instant());
        submissions.save(sub);
    }

    @Override
    @Transactional
    public SubmissionDetailDto evaluate(AuthPrincipal p, Long submissionId, EvaluateRequest req) {
        Submission sub = submissions.findById(submissionId).orElseThrow(() -> ApiException.notFound("Submission not found"));
        guard.requireExamAccess(p, sub.getExam());
        Result existing = resultRepo.findBySubmissionId(submissionId).orElse(null);
        if (existing != null && existing.getStatus() == ResultStatus.PUBLISHED)
            throw ApiException.conflict("The result is already published; modification is not allowed");

        List<Evaluation> rows = evaluations.findBySubmissionId(submissionId);
        Map<Long, Evaluation> byQ = rows.stream().collect(Collectors.toMap(e -> e.getQuestion().getId(), e -> e));
        User grader = users.getReferenceById(p.id());
        for (EvaluationItem it : req.items()) {
            Evaluation ev = byQ.get(it.questionId());
            if (ev == null) throw ApiException.badRequest("Question " + it.questionId() + " is not part of this submission");
            double max = ev.getQuestion().getMarks();
            if (it.marks() > max) throw ApiException.badRequest("Marks for question " + it.questionId() + " exceed the maximum of " + max);
            ev.setMarksAwarded(AnswerUtil.round2(it.marks()));
            ev.setFeedback(it.feedback());
            ev.setAutoGraded(false);
            ev.setEvaluator(grader);
        }
        double sum = 0;
        boolean anyPending = false, anyManualDone = false;
        for (Evaluation ev : rows) {
            if (ev.getMarksAwarded() == null) anyPending = true; else sum += ev.getMarksAwarded();
            if (!ev.isAutoGraded() && ev.getMarksAwarded() != null) anyManualDone = true;
        }
        sub.setTotalMarks(Math.max(0, AnswerUtil.round2(sum)));
        sub.setGradingStatus(anyPending ? (anyManualDone ? GradingStatus.PARTIALLY_GRADED : GradingStatus.PENDING) : GradingStatus.GRADED);
        if (req.feedback() != null) sub.setFeedback(req.feedback());
        sub.setEvaluatedBy(grader);
        sub.setEvaluatedAt(clock.instant());
        resultService.refresh(sub);
        audit.log(p.id(), "SUBMISSION_EVALUATED", "SUBMISSION", submissionId, "total=" + sub.getTotalMarks() + " status=" + sub.getGradingStatus());
        return submissionService.get(p, submissionId);
    }
}
