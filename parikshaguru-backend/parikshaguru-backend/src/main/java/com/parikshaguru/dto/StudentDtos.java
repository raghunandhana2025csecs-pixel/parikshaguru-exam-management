package com.parikshaguru.dto;

import com.parikshaguru.dto.NotificationDtos.NotificationDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class StudentDtos {
    private StudentDtos() {}

    /** state: UPCOMING | AVAILABLE | IN_PROGRESS | SUBMITTED | MISSED */
    public record StudentExamDto(Long examId, String examCode, String title, String subjectCode, String subjectName,
                                 Instant startTime, Instant endTime, int durationMinutes, double totalMarks, int numberOfQuestions,
                                 boolean negativeMarking, String instructions, String state, Long sessionId,
                                 boolean canStart, boolean canResume, long remainingSeconds, int answeredCount) {}

    public record StudentDashboardDto(String fullName, String rollNumber, String department, String course, int semester,
                                      long availableExams, long activeAssessments, long completedExams, long pendingResults,
                                      long unreadNotifications, List<StudentExamDto> exams, List<NotificationDto> notifications) {}

    public record StudentProfileDto(Long userId, String fullName, String email, String rollNumber, String department,
                                    String course, int semester, String phone) {}

    public record UpdateStudentProfileRequest(@Size(min = 2, max = 120) String fullName,
                                              @Pattern(regexp = "^[0-9+\\-() ]{0,20}$", message = "Invalid phone number") String phone) {}
}
