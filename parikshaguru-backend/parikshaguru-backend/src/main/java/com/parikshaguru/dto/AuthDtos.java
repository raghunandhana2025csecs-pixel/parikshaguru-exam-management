package com.parikshaguru.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.parikshaguru.entity.enums.AccountStatus;
import com.parikshaguru.entity.enums.Role;
import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}

    public static final String PASSWORD_RULE = "^(?=.*[A-Za-z])(?=.*\\d).{8,100}$";
    public static final String PASSWORD_MSG = "Password must be 8+ characters and contain letters and digits";

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(max = 60) String identifier,       // roll number / employee ID
            @NotBlank String department,                        // department code or name
            @Size(max = 100) String course,
            @Min(1) @Max(12) Integer semester,
            @NotBlank @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MSG) String password,
            @NotBlank String confirmPassword,
            @NotNull Role role,
            String inviteCode) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserDto user) {}

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ForgotPasswordResponse(String message, String devResetToken) {}

    public record ResetPasswordRequest(@NotBlank String token,
                                       @NotBlank @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MSG) String newPassword,
                                       @NotBlank String confirmPassword) {}

    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Pattern(regexp = PASSWORD_RULE, message = PASSWORD_MSG) String newPassword,
                                        @NotBlank String confirmPassword) {}

    public record UserDto(Long id, String fullName, String email, String identifier, Role role, AccountStatus status,
                          String department, String departmentCode, String course, Integer semester,
                          String phone, String designation) {}

    public record UpdateStatusRequest(@NotNull AccountStatus status) {}
}
