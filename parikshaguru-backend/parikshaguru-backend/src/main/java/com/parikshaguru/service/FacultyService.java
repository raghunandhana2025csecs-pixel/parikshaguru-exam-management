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

public interface FacultyService {
    DashboardDto dashboard(AuthPrincipal p);
    UserDto getProfile(AuthPrincipal p);
    UserDto updateProfile(AuthPrincipal p, UpdateStudentProfileRequest req);
    PageResponse<UserDto> listUsers(AuthPrincipal p, String role, String status, String q, int page, int size);
    UserDto updateUserStatus(AuthPrincipal p, Long userId, AccountStatus status);
    List<DepartmentDto> departments();
}
