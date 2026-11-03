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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notifications;

    @GetMapping
    public PageResponse<NotificationDto> list(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) { return notifications.list(p, page, size); }

    @GetMapping("/unread-count")
    public java.util.Map<String, Long> unread(@AuthenticationPrincipal AuthPrincipal p) { return java.util.Map.of("unread", notifications.unreadCount(p)); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody NotificationRequest r) { return notifications.create(p, r); }

    @PutMapping("/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { notifications.markRead(p, id); }

    @PutMapping("/read-all") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll(@AuthenticationPrincipal AuthPrincipal p) { notifications.markAllRead(p); }
}
