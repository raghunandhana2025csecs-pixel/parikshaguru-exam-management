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

public interface ExamService {
    PageResponse<ExamDto> list(AuthPrincipal p, String q, String status, int page, int size);
    ExamDto get(AuthPrincipal p, Long id);
    ExamDto create(AuthPrincipal p, ExamRequest req);
    ExamDto update(AuthPrincipal p, Long id, ExamRequest req);
    void delete(AuthPrincipal p, Long id);
    ExamDto publish(AuthPrincipal p, Long id);
    MessageResponse assign(AuthPrincipal p, Long id, AssignRequest req);
    void refreshStatuses();
}
