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
public class AdminController {
    private final FacultyService faculty;

    @GetMapping("/api/departments")
    public List<DepartmentDto> departments() { return faculty.departments(); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @GetMapping("/api/admin/dashboard")
    public DashboardDto dashboard(@AuthenticationPrincipal AuthPrincipal p) { return faculty.dashboard(p); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @GetMapping("/api/admin/users")
    public PageResponse<UserDto> users(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(required = false) String role,
                                       @RequestParam(required = false) String status, @RequestParam(required = false) String q,
                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return faculty.listUsers(p, role, status, q, page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/admin/users/{id}/status")
    public UserDto status(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id, @Valid @RequestBody UpdateStatusRequest r) {
        return faculty.updateUserStatus(p, id, r.status());
    }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @GetMapping("/api/faculty/profile")
    public UserDto profile(@AuthenticationPrincipal AuthPrincipal p) { return faculty.getProfile(p); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PutMapping("/api/faculty/profile")
    public UserDto updateProfile(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody UpdateStudentProfileRequest r) { return faculty.updateProfile(p, r); }
}
