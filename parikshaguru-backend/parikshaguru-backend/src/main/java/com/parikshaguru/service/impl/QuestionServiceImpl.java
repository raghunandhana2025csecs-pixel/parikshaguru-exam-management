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
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questions;
    private final ExaminationRepository exams;
    private final AccessGuard guard;
    private final EntityMapper mapper;
    private final AuditService audit;

    @Override
    @Transactional
    public QuestionDto add(AuthPrincipal p, Long examId, QuestionRequest r) {
        Examination e = exams.findById(examId).orElseThrow(() -> ApiException.notFound("Examination not found"));
        guard.requireExamAccess(p, e);
        requireDraft(e);
        List<OptionDto> opts = normalizeOptions(r);
        String correct = normalizeCorrect(r, opts);
        Question q = new Question();
        q.setExam(e);
        q.setSubject(e.getSubject());
        fill(q, r, correct);
        q.setOrderNo(r.orderNo() != null ? r.orderNo() : questions.maxOrderNo(examId) + 1);
        applyOptions(q, opts);
        questions.save(q);
        audit.log(p.id(), "QUESTION_ADDED", "QUESTION", q.getId(), "exam=" + e.getCode());
        return mapper.toQuestionDto(q);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> list(AuthPrincipal p, Long examId) {
        Examination e = exams.findById(examId).orElseThrow(() -> ApiException.notFound("Examination not found"));
        guard.requireExamAccess(p, e);
        return questions.findByExamIdOrderByOrderNoAscIdAsc(examId).stream().map(mapper::toQuestionDto).toList();
    }

    @Override
    @Transactional
    public QuestionDto update(AuthPrincipal p, Long id, QuestionRequest r) {
        Question q = questions.findById(id).orElseThrow(() -> ApiException.notFound("Question not found"));
        guard.requireExamAccess(p, q.getExam());
        requireDraft(q.getExam());
        List<OptionDto> opts = normalizeOptions(r);
        String correct = normalizeCorrect(r, opts);
        fill(q, r, correct);
        if (r.orderNo() != null) q.setOrderNo(r.orderNo());
        applyOptions(q, opts);
        audit.log(p.id(), "QUESTION_UPDATED", "QUESTION", id, null);
        return mapper.toQuestionDto(q);
    }

    @Override
    @Transactional
    public void delete(AuthPrincipal p, Long id) {
        Question q = questions.findById(id).orElseThrow(() -> ApiException.notFound("Question not found"));
        guard.requireExamAccess(p, q.getExam());
        requireDraft(q.getExam());
        questions.delete(q);
        audit.log(p.id(), "QUESTION_DELETED", "QUESTION", id, null);
    }

    private void requireDraft(Examination e) {
        if (e.getStatus() != ExamStatus.DRAFT) throw ApiException.conflict("Questions can only be changed while the examination is DRAFT");
    }

    private void fill(Question q, QuestionRequest r, String correct) {
        q.setQuestionText(r.questionText().trim());
        q.setQuestionType(r.type());
        q.setCorrectAnswer(correct);
        q.setMarks(r.marks());
        q.setNegativeMarks(r.negativeMarks() == null ? 0 : r.negativeMarks());
        q.setTopic(r.topic());
    }

    private boolean isChoice(QuestionType t) { return t == QuestionType.MCQ || t == QuestionType.MULTI_SELECT; }

    private List<OptionDto> normalizeOptions(QuestionRequest r) {
        if (!isChoice(r.type())) return List.of();
        List<OptionDto> in = r.options() == null ? List.of() : r.options();
        if (in.size() < 2 || in.size() > 8) throw ApiException.badRequest("Choice questions need between 2 and 8 options");
        List<OptionDto> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < in.size(); i++) {
            OptionDto o = in.get(i);
            if (o == null || o.text() == null || o.text().isBlank()) throw ApiException.badRequest("Option text cannot be empty");
            String label = (o.label() == null || o.label().isBlank()) ? String.valueOf((char) ('A' + i)) : o.label().trim().toUpperCase();
            if (!seen.add(label)) throw ApiException.badRequest("Duplicate option label " + label);
            out.add(new OptionDto(label, o.text().trim()));
        }
        return out;
    }

    private String normalizeCorrect(QuestionRequest r, List<OptionDto> opts) {
        String raw = r.correctAnswer();
        switch (r.type()) {
            case MCQ -> {
                String c = raw == null ? "" : raw.trim().toUpperCase();
                if (opts.stream().noneMatch(o -> o.label().equals(c))) throw ApiException.badRequest("Correct answer must be one of the option labels");
                return c;
            }
            case MULTI_SELECT -> {
                String c = AnswerUtil.canonicalLabels(raw);
                Set<String> labels = opts.stream().map(OptionDto::label).collect(Collectors.toSet());
                if (c.isEmpty() || !labels.containsAll(Arrays.asList(c.split(",")))) throw ApiException.badRequest("Correct answers must be a non-empty subset of the option labels");
                return c;
            }
            case TRUE_FALSE -> {
                String c = raw == null ? "" : raw.trim().toUpperCase();
                if (!c.equals("TRUE") && !c.equals("FALSE")) throw ApiException.badRequest("Correct answer must be TRUE or FALSE");
                return c;
            }
            case SHORT_ANSWER -> {
                if (raw == null || raw.isBlank()) throw ApiException.badRequest("Short-answer questions need an expected answer (use | for alternatives)");
                return raw.trim();
            }
            default -> { return raw == null || raw.isBlank() ? null : raw.trim(); }   // DESCRIPTIVE: optional model answer
        }
    }

    /** Updates options in place (by label) to avoid unique-key clashes between delete and insert in one flush. */
    private void applyOptions(Question q, List<OptionDto> in) {
        Map<String, QuestionOption> existing = q.getOptions().stream().collect(Collectors.toMap(QuestionOption::getLabel, o -> o, (a, b) -> a));
        Set<String> keep = new HashSet<>();
        for (OptionDto d : in) {
            keep.add(d.label());
            QuestionOption o = existing.get(d.label());
            if (o == null) {
                o = new QuestionOption();
                o.setQuestion(q);
                o.setLabel(d.label());
                q.getOptions().add(o);
            }
            o.setOptionText(d.text());
        }
        q.getOptions().removeIf(o -> !keep.contains(o.getLabel()));
    }
}
