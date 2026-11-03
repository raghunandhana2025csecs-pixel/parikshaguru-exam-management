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
public class ResultServiceImpl implements ResultService {
    private final ResultRepository results;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final GradingPolicy gradingPolicy;
    private final AccessGuard guard;
    private final EntityMapper mapper;
    private final AuditService audit;
    private final NotificationService notifications;
    private final Clock clock;

    @Override
    @Transactional
    public void initialize(Submission sub) {
        Result r = new Result();
        r.setSubmission(sub);
        r.setExam(sub.getExam());
        r.setStudent(sub.getStudent());
        r.setMaxMarks(sub.getExam().getTotalMarks());
        applyTotals(r, sub.getTotalMarks());
        r.setStatus(ResultStatus.PENDING_APPROVAL);
        results.save(r);
    }

    @Override
    @Transactional
    public void refresh(Submission sub) {
        Result r = results.findBySubmissionId(sub.getId()).orElse(null);
        if (r == null) return;
        if (r.getStatus() == ResultStatus.PUBLISHED) throw ApiException.conflict("The result is already published; modification is not allowed");
        applyTotals(r, sub.getTotalMarks());
        if (r.getStatus() == ResultStatus.APPROVED) {                    // marks changed after approval: needs re-approval
            r.setStatus(ResultStatus.PENDING_APPROVAL);
            r.setApprovedBy(null);
            r.setApprovedAt(null);
            audit.log(null, "RESULT_APPROVAL_REVOKED", "RESULT", r.getId(), "marks changed after approval");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResultDto> list(AuthPrincipal p, long examId, String status, int page, int size) {
        Page<Result> pg = results.search(examId, PageUtil.upper(status), guard.isAdmin(p), p.id(),
                PageUtil.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.of(pg.map(r -> mapper.toResultDto(r, false)));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultDto get(AuthPrincipal p, Long id) {
        Result r = load(id);
        if (p.role() == Role.STUDENT) {
            if (!r.getStudent().getId().equals(p.id()) || r.getStatus() != ResultStatus.PUBLISHED)
                throw ApiException.notFound("Result not found");
            return mapper.toResultDto(r, true);
        }
        guard.requireExamAccess(p, r.getExam());
        return mapper.toResultDto(r, false);
    }

    @Override
    @Transactional
    public ResultDto approve(AuthPrincipal p, Long id) {
        Result r = load(id);
        guard.requireExamAccess(p, r.getExam());
        if (r.getStatus() == ResultStatus.PUBLISHED) throw ApiException.conflict("Result is already published and locked");
        if (r.getStatus() == ResultStatus.APPROVED) throw ApiException.conflict("Result is already approved");
        if (r.getSubmission().getGradingStatus() != GradingStatus.GRADED)
            throw ApiException.conflict("Grading is incomplete: descriptive answers still need manual evaluation");
        r.setStatus(ResultStatus.APPROVED);
        r.setApprovedBy(users.getReferenceById(p.id()));
        r.setApprovedAt(clock.instant());
        audit.log(p.id(), "RESULT_APPROVED", "RESULT", id, "marks=" + r.getTotalMarks());
        return mapper.toResultDto(r, false);
    }

    @Override
    @Transactional
    public ResultDto publish(AuthPrincipal p, Long id) {
        Result r = load(id);
        guard.requireExamAccess(p, r.getExam());
        if (r.getStatus() == ResultStatus.PUBLISHED) throw ApiException.conflict("Result is already published");
        if (r.getStatus() != ResultStatus.APPROVED) throw ApiException.conflict("Only APPROVED results can be published");
        r.setStatus(ResultStatus.PUBLISHED);
        r.setPublishedBy(users.getReferenceById(p.id()));
        r.setPublishedAt(clock.instant());
        audit.log(p.id(), "RESULT_PUBLISHED", "RESULT", id, "marks=" + r.getTotalMarks() + " grade=" + r.getGrade());
        notifications.notifyUser(r.getStudent(), "Result published",
                "Your result for " + r.getExam().getTitle() + " is now available in My Results.", "RESULT");
        return mapper.toResultDto(r, false);
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public BulkResultDto bulkApprove(AuthPrincipal p, List<Long> ids) { return bulk(ids, id -> approve(p, id)); }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public BulkResultDto bulkPublish(AuthPrincipal p, List<Long> ids) { return bulk(ids, id -> publish(p, id)); }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryDto> history(AuthPrincipal p, Long id) {
        Result r = load(id);
        guard.requireExamAccess(p, r.getExam());
        return auditLogs.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("RESULT", id).stream()
                .map(a -> new HistoryDto(a.getAction(), a.getUserId(),
                        a.getUserId() == null ? null : users.findById(a.getUserId()).map(User::getFullName).orElse(null),
                        a.getCreatedAt(), a.getDetails())).toList();
    }

    private Result load(Long id) { return results.findById(id).orElseThrow(() -> ApiException.notFound("Result not found")); }

    private void applyTotals(Result r, double total) {
        r.setTotalMarks(total);
        double pct = r.getMaxMarks() > 0 ? AnswerUtil.round2(total / r.getMaxMarks() * 100.0) : 0;
        r.setPercentage(pct);
        r.setGrade(gradingPolicy.grade(pct));
    }

    private BulkResultDto bulk(List<Long> ids, java.util.function.Consumer<Long> action) {
        int ok = 0;
        List<String> errors = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(ids)) {
            try { action.accept(id); ok++; }
            catch (ApiException e) { errors.add("#" + id + ": " + e.getMessage()); }
        }
        return new BulkResultDto(ok, errors.size(), errors);
    }
}
