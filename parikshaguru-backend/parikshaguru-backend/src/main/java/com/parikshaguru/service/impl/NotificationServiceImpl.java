package com.parikshaguru.service.impl;

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
import com.parikshaguru.exception.ApiException;
import com.parikshaguru.mapper.EntityMapper;
import com.parikshaguru.repository.*;
import com.parikshaguru.security.*;
import com.parikshaguru.service.*;
import com.parikshaguru.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final List<AccountStatus> LIVE = List.of(AccountStatus.ACTIVE, AccountStatus.APPROVED);
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final StudentRepository students;
    private final EntityMapper mapper;
    private final AuditService audit;

    @Override
    @Transactional
    public void notifyUser(User user, String title, String message, String type) {
        Notification n = new Notification();
        n.setUser(user); n.setTitle(title); n.setMessage(message); n.setType(type == null ? "INFO" : type);
        notifications.save(n);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> list(AuthPrincipal p, int page, int size) {
        return PageResponse.of(notifications.findByUserIdOrderByCreatedAtDesc(p.id(), PageUtil.of(page, size, Sort.unsorted()))
                .map(mapper::toNotificationDto));
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(AuthPrincipal p) { return notifications.countByUserIdAndSeenFalse(p.id()); }

    @Override
    @Transactional
    public void markRead(AuthPrincipal p, Long id) {
        Notification n = notifications.findById(id).orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getUser().getId().equals(p.id())) throw ApiException.notFound("Notification not found");
        n.setSeen(true);
    }

    @Override
    @Transactional
    public void markAllRead(AuthPrincipal p) { notifications.markAllRead(p.id()); }

    @Override
    @Transactional
    public MessageResponse create(AuthPrincipal p, NotificationRequest req) {
        List<User> targets;
        if (req.userId() != null) {
            User u = users.findById(req.userId()).orElseThrow(() -> ApiException.notFound("Target user not found"));
            if (p.role() == Role.FACULTY && u.getRole() != Role.STUDENT) throw ApiException.forbidden("Faculty can notify students only");
            targets = List.of(u);
        } else if (req.role() != null) {
            if (p.role() == Role.FACULTY && req.role() != Role.STUDENT) throw ApiException.forbidden("Faculty can notify students only");
            targets = (req.role() == Role.STUDENT && req.semester() != null)
                    ? students.findBySemester(req.semester(), LIVE)
                    : users.findByRoleAndStatusIn(req.role(), LIVE);
        } else {
            throw ApiException.badRequest("Provide userId or role");
        }
        if (targets.size() > 5000) throw ApiException.badRequest("Too many recipients (max 5000)");
        targets.forEach(u -> notifyUser(u, req.title().trim(), req.message().trim(), req.type()));
        audit.log(p.id(), "NOTIFICATION_SENT", "NOTIFICATION", null, "recipients=" + targets.size() + " title=" + req.title());
        return new MessageResponse("Notification sent to " + targets.size() + " user(s)");
    }
}
