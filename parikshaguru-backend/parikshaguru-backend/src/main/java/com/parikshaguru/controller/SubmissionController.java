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
@RequestMapping("/api/submissions")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissions;
    private final EvaluationService evaluation;

    @GetMapping
    public PageResponse<SubmissionSummaryDto> list(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(defaultValue = "0") long examId,
                                                   @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) { return submissions.list(p, examId, status, page, size); }

    @GetMapping("/{id}") public SubmissionDetailDto get(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return submissions.get(p, id); }

    @PostMapping("/{id}/evaluate")
    public SubmissionDetailDto evaluate(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody EvaluateRequest r) { return evaluation.evaluate(p, id, r); }
}
