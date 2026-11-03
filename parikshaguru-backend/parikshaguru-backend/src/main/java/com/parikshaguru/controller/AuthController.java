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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegisterRequest r) { return auth.register(r); }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }

    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal AuthPrincipal p) { auth.logout(p); return new MessageResponse("Logged out"); }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgot(@Valid @RequestBody ForgotPasswordRequest r) { return auth.forgotPassword(r); }

    @PostMapping("/reset-password")
    public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest r) { auth.resetPassword(r); return new MessageResponse("Password has been reset"); }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AuthPrincipal p) { return auth.me(p); }

    @PutMapping("/change-password")
    public AuthResponse change(@AuthenticationPrincipal AuthPrincipal p, @Valid @RequestBody ChangePasswordRequest r) { return auth.changePassword(p, r); }
}
