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
@RequestMapping("/api/students")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService students;

    @GetMapping("/dashboard") public StudentDashboardDto dashboard(@AuthenticationPrincipal AuthPrincipal p) { return students.dashboard(p); }
    @GetMapping("/profile") public StudentProfileDto profile(@AuthenticationPrincipal AuthPrincipal p) { return students.getProfile(p); }
    @PutMapping("/profile")
    public StudentProfileDto update(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody UpdateStudentProfileRequest r) { return students.updateProfile(p, r); }
    /** state = UPCOMING | AVAILABLE | IN_PROGRESS | SUBMITTED | MISSED (optional) */
    @GetMapping("/exams")
    public List<StudentExamDto> exams(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(required = false) String state) { return students.exams(p, state); }
    @GetMapping("/results") public StudentResultsDto results(@AuthenticationPrincipal AuthPrincipal p) { return students.results(p); }
}
