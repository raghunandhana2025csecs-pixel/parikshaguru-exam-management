package com.parikshaguru.dto;

import com.parikshaguru.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class NotificationDtos {
    private NotificationDtos() {}

    public record NotificationDto(Long id, String title, String message, String type, boolean seen, Instant createdAt) {}

    /** Target: userId, OR role (+ optional semester for students). */
    public record NotificationRequest(@NotBlank @Size(max = 200) String title, @NotBlank String message,
                                      @Size(max = 30) String type, Long userId, Role role, Integer semester) {}
}
