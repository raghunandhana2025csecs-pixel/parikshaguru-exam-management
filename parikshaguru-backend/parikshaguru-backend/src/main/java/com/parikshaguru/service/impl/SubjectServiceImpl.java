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
public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjects;
    private final DepartmentRepository departments;
    private final UserRepository users;
    private final ExaminationRepository exams;
    private final AccessGuard guard;
    private final EntityMapper mapper;
    private final AuditService audit;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubjectDto> search(AuthPrincipal p, String q, int semester, boolean mine, int page, int size) {
        Page<Subject> pg = subjects.search(PageUtil.q(q), Math.max(semester, 0), mine, p.id(), PageUtil.of(page, size, Sort.by("code")));
        return PageResponse.of(pg.map(mapper::toSubjectDto));
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDto get(Long id) { return mapper.toSubjectDto(load(id)); }

    @Override
    @Transactional
    public SubjectDto create(AuthPrincipal p, SubjectRequest r) {
        String code = PageUtil.upper(r.code());
        if (subjects.existsByCodeIgnoreCase(code)) throw ApiException.conflict("Subject code already exists");
        Subject s = new Subject();
        apply(s, r, p);
        s.setCode(code);
        if (p.role() == Role.FACULTY) s.getFaculty().add(users.getReferenceById(p.id()));
        else assignFaculty(s, r.facultyIds());
        subjects.save(s);
        audit.log(p.id(), "SUBJECT_CREATED", "SUBJECT", s.getId(), code);
        return mapper.toSubjectDto(s);
    }

    @Override
    @Transactional
    public SubjectDto update(AuthPrincipal p, Long id, SubjectRequest r) {
        Subject s = load(id);
        guard.requireSubjectAccess(p, s);
        String code = PageUtil.upper(r.code());
        subjects.findByCodeIgnoreCase(code).filter(o -> !o.getId().equals(id)).ifPresent(o -> { throw ApiException.conflict("Subject code already exists"); });
        apply(s, r, p);
        s.setCode(code);
        if (p.role() == Role.ADMIN && r.facultyIds() != null) assignFaculty(s, r.facultyIds());
        audit.log(p.id(), "SUBJECT_UPDATED", "SUBJECT", id, code);
        return mapper.toSubjectDto(s);
    }

    @Override
    @Transactional
    public void delete(AuthPrincipal p, Long id) {
        Subject s = load(id);
        guard.requireSubjectAccess(p, s);
        if (exams.existsBySubjectId(id)) throw ApiException.conflict("Cannot delete a subject that has examinations");
        subjects.delete(s);
        audit.log(p.id(), "SUBJECT_DELETED", "SUBJECT", id, s.getCode());
    }

    private Subject load(Long id) { return subjects.findById(id).orElseThrow(() -> ApiException.notFound("Subject not found")); }

    private void apply(Subject s, SubjectRequest r, AuthPrincipal p) {
        s.setName(r.name().trim());
        s.setSemester(r.semester());
        s.setCreditPoints(r.creditPoints());
        s.setDescription(r.description());
        Department d;
        if (r.department() != null && !r.department().isBlank()) {
            String v = r.department().trim();
            d = departments.findByCodeIgnoreCase(v).or(() -> departments.findByNameIgnoreCase(v))
                    .orElseThrow(() -> ApiException.badRequest("Unknown department: " + v));
        } else if (s.getDepartment() != null) {
            d = s.getDepartment();
        } else {
            d = users.findById(p.id()).map(User::getDepartment).orElseThrow(() -> ApiException.badRequest("Department is required"));
            if (d == null) throw ApiException.badRequest("Department is required");
        }
        s.setDepartment(d);
    }

    private void assignFaculty(Subject s, List<Long> ids) {
        Set<User> set = new HashSet<>();
        if (ids != null) for (Long fid : ids) {
            User u = users.findById(fid).orElseThrow(() -> ApiException.badRequest("Faculty not found: " + fid));
            if (u.getRole() != Role.FACULTY) throw ApiException.badRequest("User " + fid + " is not a faculty member");
            set.add(u);
        }
        s.getFaculty().clear();
        s.getFaculty().addAll(set);
    }
}
