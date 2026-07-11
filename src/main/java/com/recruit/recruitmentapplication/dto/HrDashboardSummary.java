package com.recruit.recruitmentapplication.dto;

public class HrDashboardSummary {
    private final long activeJobsCount;
    private final long applicationsAwaitingReviewCount;
    private final long upcomingInterviewsCount;

    public HrDashboardSummary(long activeJobsCount, long applicationsAwaitingReviewCount, long upcomingInterviewsCount) {
        this.activeJobsCount = activeJobsCount;
        this.applicationsAwaitingReviewCount = applicationsAwaitingReviewCount;
        this.upcomingInterviewsCount = upcomingInterviewsCount;
    }

    public long getActiveJobsCount() { return activeJobsCount; }
    public long getApplicationsAwaitingReviewCount() { return applicationsAwaitingReviewCount; }
    public long getUpcomingInterviewsCount() { return upcomingInterviewsCount; }
}
