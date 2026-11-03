package com.parikshaguru.scheduler;

import com.parikshaguru.entity.enums.SessionStatus;
import com.parikshaguru.repository.ExaminationSessionRepository;
import com.parikshaguru.service.ExamService;
import com.parikshaguru.service.ExaminationSessionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/** Server-side enforcement: expired sessions are submitted even if the browser is closed. */
@Component
@RequiredArgsConstructor
public class ExamScheduler {
    private static final Logger log = LoggerFactory.getLogger(ExamScheduler.class);
    private final ExaminationSessionRepository sessions;
    private final ExaminationSessionService sessionService;
    private final ExamService examService;
    private final Clock clock;

    @Scheduled(fixedDelay = 15000, initialDelay = 20000)
    public void autoSubmitExpired() {
        for (Long id : sessions.findExpiredIds(SessionStatus.IN_PROGRESS, clock.instant())) {
            try { sessionService.autoSubmit(id); }
            catch (Exception e) { log.error("Auto-submit failed for session {}", id, e); }
        }
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 20000)
    public void refreshExamStatuses() {
        try { examService.refreshStatuses(); }
        catch (Exception e) { log.error("Exam status refresh failed", e); }
    }
}
