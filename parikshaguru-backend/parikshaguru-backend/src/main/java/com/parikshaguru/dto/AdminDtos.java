package com.parikshaguru.dto;

public final class AdminDtos {
    private AdminDtos() {}

    public record DashboardDto(long totalStudents, long totalFaculty, long totalSubjects, long activeExams,
                               long publishedExams, long draftExams, long pendingSubmissions,
                               long pendingResultApprovals, long pendingUserApprovals) {}
}
