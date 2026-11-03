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

public interface ExaminationSessionService {
    SessionDto start(AuthPrincipal p, Long examId);
    SessionDto get(AuthPrincipal p, Long sessionId);
    ProgressDto saveAnswer(AuthPrincipal p, Long sessionId, SaveAnswerRequest req);
    ProgressDto markReview(AuthPrincipal p, Long sessionId, MarkReviewRequest req);
    List<AnswerDto> listAnswers(AuthPrincipal p, Long sessionId);
    SubmitResponse submit(AuthPrincipal p, Long sessionId);
    void autoSubmit(Long sessionId);
    void logEvent(AuthPrincipal p, Long sessionId, ProctorEventRequest req);
    List<ProctorEventDto> listEvents(AuthPrincipal p, Long sessionId);
}
