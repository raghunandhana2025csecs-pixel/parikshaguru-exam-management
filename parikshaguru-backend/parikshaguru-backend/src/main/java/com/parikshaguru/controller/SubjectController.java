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
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjects;

    @GetMapping
    public PageResponse<SubjectDto> list(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(required = false) String q,
                                         @RequestParam(defaultValue = "0") int semester, @RequestParam(defaultValue = "false") boolean mine,
                                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return subjects.search(p, q, semester, mine, page, size);
    }

    @GetMapping("/{id}") public SubjectDto get(@PathVariable Long id) { return subjects.get(id); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public SubjectDto create(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody SubjectRequest r) { return subjects.create(p, r); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PutMapping("/{id}")
    public SubjectDto update(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody SubjectRequest r) { return subjects.update(p, id, r); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { subjects.delete(p, id); }
}
