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

public interface SubjectService {
    PageResponse<SubjectDto> search(AuthPrincipal p, String q, int semester, boolean mine, int page, int size);
    SubjectDto get(Long id);
    SubjectDto create(AuthPrincipal p, SubjectRequest req);
    SubjectDto update(AuthPrincipal p, Long id, SubjectRequest req);
    void delete(AuthPrincipal p, Long id);
}
