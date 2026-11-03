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
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository submissions;
    private final ResultRepository results;
    private final EvaluationRepository evaluations;
    private final StudentAnswerRepository answers;
    private final QuestionRepository questions;
    private final ProctoringEventRepository events;
    private final AccessGuard guard;
    private final EntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubmissionSummaryDto> list(AuthPrincipal p, long examId, String status, int page, int size) {
        Page<Submission> pg = submissions.search(examId, PageUtil.upper(status), guard.isAdmin(p), p.id(),
                PageUtil.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")));
        return PageResponse.of(pg.map(s -> mapper.toSubmissionSummary(s, results.findBySubmissionId(s.getId()).orElse(null))));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDetailDto get(AuthPrincipal p, Long id) {
        Submission s = submissions.findById(id).orElseThrow(() -> ApiException.notFound("Submission not found"));
        guard.requireExamAccess(p, s.getExam());
        Result r = results.findBySubmissionId(id).orElse(null);
        Map<Long, Evaluation> ev = evaluations.findBySubmissionId(id).stream()
                .collect(Collectors.toMap(e -> e.getQuestion().getId(), e -> e, (a, b) -> a));
        Map<Long, StudentAnswer> am = answers.findBySessionId(s.getSession().getId()).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a, b) -> a));
        List<AnswerReviewDto> rows = new ArrayList<>();
        for (Question q : questions.findByExamIdOrderByOrderNoAscIdAsc(s.getExam().getId())) {
            Evaluation e = ev.get(q.getId());
            StudentAnswer a = am.get(q.getId());
            Double awarded = e == null ? null : e.getMarksAwarded();
            rows.add(new AnswerReviewDto(q.getId(), q.getOrderNo(), q.getQuestionType(), q.getQuestionText(), mapper.toOptions(q),
                    q.getCorrectAnswer(), a == null ? null : a.getSelectedAnswer(), q.getMarks(), awarded,
                    e != null && e.isAutoGraded(), awarded == null, e == null ? null : e.getFeedback()));
        }
        Map<String, Long> proctor = new LinkedHashMap<>();
        for (Object[] row : events.countByType(s.getSession().getId())) proctor.put(String.valueOf(row[0]), (Long) row[1]);
        return new SubmissionDetailDto(mapper.toSubmissionSummary(s, r), s.getFeedback(), s.getSession().getId(), rows, proctor);
    }
}
