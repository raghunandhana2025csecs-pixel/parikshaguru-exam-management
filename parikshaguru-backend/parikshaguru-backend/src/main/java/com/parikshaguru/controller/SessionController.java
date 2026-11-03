package com.parikshaguru.controller;

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
import com.parikshaguru.security.AuthPrincipal;
import com.parikshaguru.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SessionController {
    private final ExaminationSessionService sessions;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/api/exams/{examId}/start")
    public SessionDto start(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long examId) { return sessions.start(p, examId); }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/api/sessions/{id}")
    public SessionDto get(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return sessions.get(p, id); }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/api/sessions/{id}/answers")
    public ProgressDto save(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody SaveAnswerRequest r) { return sessions.saveAnswer(p, id, r); }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/api/sessions/{id}/answers")
    public List<AnswerDto> answers(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return sessions.listAnswers(p, id); }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/api/sessions/{id}/mark-review")
    public ProgressDto mark(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody MarkReviewRequest r) { return sessions.markReview(p, id, r); }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/api/sessions/{id}/submit")
    public SubmitResponse submit(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return sessions.submit(p, id); }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/api/sessions/{id}/proctoring-events") @ResponseStatus(HttpStatus.ACCEPTED)
    public void event(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody ProctorEventRequest r) { sessions.logEvent(p, id, r); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @GetMapping("/api/sessions/{id}/proctoring-events")
    public List<ProctorEventDto> events(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return sessions.listEvents(p, id); }
}
