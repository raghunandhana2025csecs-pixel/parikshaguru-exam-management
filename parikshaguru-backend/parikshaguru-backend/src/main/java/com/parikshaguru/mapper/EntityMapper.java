package com.parikshaguru.mapper;

import com.parikshaguru.dto.AuthDtos.UserDto;
import com.parikshaguru.dto.ExamDtos.ExamDto;
import com.parikshaguru.dto.NotificationDtos.NotificationDto;
import com.parikshaguru.dto.QuestionDtos.*;
import com.parikshaguru.dto.ResultDtos.*;
import com.parikshaguru.dto.SubjectDtos.*;
import com.parikshaguru.entity.*;
import com.parikshaguru.entity.enums.*;
import com.parikshaguru.repository.FacultyRepository;
import com.parikshaguru.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** Converts JPA entities to DTOs so entities are never exposed through the API. Call inside a transaction (lazy relations). */
@Component
@RequiredArgsConstructor
public class EntityMapper {
    private final StudentRepository students;
    private final FacultyRepository faculties;
    private final Clock clock;

    public UserDto toUserDto(User u) {
        Student s = u.getRole() == Role.STUDENT ? students.findByUserId(u.getId()).orElse(null) : null;
        Faculty f = u.getRole() == Role.FACULTY ? faculties.findByUserId(u.getId()).orElse(null) : null;
        Department d = u.getDepartment();
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getIdentifier(), u.getRole(), u.getStatus(),
                d == null ? null : d.getName(), d == null ? null : d.getCode(),
                s == null ? null : s.getCourse(), s == null ? null : s.getSemester(),
                s != null ? s.getPhone() : (f != null ? f.getPhone() : null),
                f == null ? null : f.getDesignation());
    }

    public SubjectDto toSubjectDto(Subject s) {
        List<FacultySummary> fac = s.getFaculty().stream()
                .map(u -> new FacultySummary(u.getId(), u.getFullName(), u.getEmail(), u.getIdentifier())).toList();
        return new SubjectDto(s.getId(), s.getCode(), s.getName(), s.getDepartment().getName(), s.getDepartment().getCode(),
                s.getSemester(), s.getCreditPoints(), s.getDescription(), fac);
    }

    public ExamStatus effectiveStatus(Examination e) {
        ExamStatus st = e.getStatus();
        if (st == ExamStatus.PUBLISHED || st == ExamStatus.ACTIVE) {
            Instant now = clock.instant();
            if (!now.isBefore(e.getEndTime())) return ExamStatus.COMPLETED;
            if (!now.isBefore(e.getStartTime())) return ExamStatus.ACTIVE;
        }
        return st;
    }

    public ExamDto toExamDto(Examination e, long questionCount, long assigned, long submitted) {
        Subject s = e.getSubject();
        return new ExamDto(e.getId(), e.getCode(), e.getTitle(), s.getId(), s.getCode(), s.getName(), e.getSemester(),
                e.getExamType(), e.getTotalMarks(), e.getDurationMinutes(), e.getStartTime(), e.getEndTime(),
                e.isNegativeMarking(), e.getNumberOfQuestions(), questionCount, e.getInstructions(), e.getStatus(),
                effectiveStatus(e), assigned, submitted, e.getCreatedBy().getFullName());
    }

    public List<OptionDto> toOptions(Question q) {
        return q.getOptions().stream().map(o -> new OptionDto(o.getLabel(), o.getOptionText())).toList();
    }

    public QuestionDto toQuestionDto(Question q) {
        return new QuestionDto(q.getId(), q.getExam().getId(), q.getSubject().getId(), q.getTopic(), q.getQuestionText(),
                q.getQuestionType(), toOptions(q), q.getCorrectAnswer(), q.getMarks(), q.getNegativeMarks(), q.getOrderNo());
    }

    /** Deliberately omits correctAnswer. */
    public StudentQuestionDto toStudentQuestionDto(Question q, StudentAnswer a) {
        return new StudentQuestionDto(q.getId(), q.getOrderNo(), q.getQuestionType(), q.getQuestionText(), toOptions(q),
                q.getMarks(), q.getNegativeMarks(), q.getTopic(),
                a == null ? null : a.getSelectedAnswer(), a != null && a.isMarkedForReview());
    }

    public NotificationDto toNotificationDto(Notification n) {
        return new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isSeen(), n.getCreatedAt());
    }

    public ResultDto toResultDto(Result r, boolean hideUnlessPublished) {
        boolean pub = r.getStatus() == ResultStatus.PUBLISHED;
        boolean show = pub || !hideUnlessPublished;
        Examination e = r.getExam();
        User st = r.getStudent();
        return new ResultDto(r.getId(), r.getSubmission().getId(), e.getId(), e.getCode(), e.getTitle(),
                e.getSubject().getCode(), e.getSubject().getName(), st.getId(), st.getFullName(), st.getIdentifier(),
                show ? r.getTotalMarks() : null, show ? r.getMaxMarks() : null, show ? r.getPercentage() : null,
                show ? r.getGrade() : null, r.getStatus(), pub, r.getSubmission().getSubmittedAt(),
                r.getApprovedAt(), r.getPublishedAt(), show ? r.getSubmission().getFeedback() : null);
    }

    public SubmissionSummaryDto toSubmissionSummary(Submission s, Result r) {
        Examination e = s.getExam();
        return new SubmissionSummaryDto(s.getId(), e.getId(), e.getCode(), e.getTitle(), s.getStudent().getId(),
                s.getStudent().getFullName(), s.getStudent().getIdentifier(), s.getSubmittedAt(), s.isAutoSubmitted(),
                s.getObjectiveMarks(), s.getTotalMarks(), e.getTotalMarks(), s.getGradingStatus(),
                r == null ? null : r.getId(), r == null ? null : r.getStatus());
    }
}
