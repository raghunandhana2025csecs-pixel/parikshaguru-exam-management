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
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {
    private final ResultService results;

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @GetMapping
    public PageResponse<ResultDto> list(@AuthenticationPrincipal AuthPrincipal p, @RequestParam(defaultValue = "0") long examId,
                                        @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) { return results.list(p, examId, status, page, size); }

    /** Students: own PUBLISHED results only. Staff: any result they have access to. */
    @GetMapping("/{id}") public ResultDto get(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return results.get(p, id); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PostMapping("/{id}/approve") public ResultDto approve(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return results.approve(p, id); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PostMapping("/{id}/publish") public ResultDto publish(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return results.publish(p, id); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PostMapping("/bulk-approve")
    public BulkResultDto bulkApprove(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody BulkIdsRequest r) { return results.bulkApprove(p, r.ids()); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @PostMapping("/bulk-publish")
    public BulkResultDto bulkPublish(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody BulkIdsRequest r) { return results.bulkPublish(p, r.ids()); }

    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    @GetMapping("/{id}/history")
    public List<HistoryDto> history(@AuthenticationPrincipal AuthPrincipal p, @PathVariable Long id) { return results.history(p, id); }
}
