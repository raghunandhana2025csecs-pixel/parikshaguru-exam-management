package com.parikshaguru.service;

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
import com.parikshaguru.security.AuthPrincipal;
import java.util.List;

public interface ResultService {
    void initialize(Submission submission);
    void refresh(Submission submission);
    PageResponse<ResultDto> list(AuthPrincipal p, long examId, String status, int page, int size);
    ResultDto get(AuthPrincipal p, Long id);
    ResultDto approve(AuthPrincipal p, Long id);
    ResultDto publish(AuthPrincipal p, Long id);
    BulkResultDto bulkApprove(AuthPrincipal p, List<Long> ids);
    BulkResultDto bulkPublish(AuthPrincipal p, List<Long> ids);
    List<HistoryDto> history(AuthPrincipal p, Long id);
}
