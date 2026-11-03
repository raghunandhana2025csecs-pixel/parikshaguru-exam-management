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
@RequestMapping("/api/exams")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService exams;
    private final QuestionService questions;

    @GetMapping
    public PageResponse<ExamDto> list(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(required = false) String q,
                                      @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) { return exams.list(p, q, status, page, size); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ExamDto create(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody ExamRequest r) { return exams.create(p, r); }

    @GetMapping("/{id}") public ExamDto get(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return exams.get(p, id); }

    @PutMapping("/{id}")
    public ExamDto update(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody ExamRequest r) { return exams.update(p, id, r); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { exams.delete(p, id); }

    @PostMapping("/{id}/publish") public ExamDto publish(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return exams.publish(p, id); }

    /** Body optional: empty = assign to every active student of the exam's department + semester. */
    @PostMapping("/{id}/assign")
    public MessageResponse assign(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @RequestBody(required = false) AssignRequest r) { return exams.assign(p, id, r); }

    @PostMapping("/{examId}/questions") @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto addQuestion(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long examId, @Valid @RequestBody QuestionRequest r) { return questions.add(p, examId, r); }

    @GetMapping("/{examId}/questions")
    public List<QuestionDto> listQuestions(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long examId) { return questions.list(p, examId); }
}
