package com.recruit.recruitmentapplication.dto;

import java.time.LocalDate;

public class ActiveJobDashboardRow {
    private final Long id;
    private final String title;
    private final String department;
    private final long applicationCount;
    private final LocalDate deadline;

    public ActiveJobDashboardRow(Long id, String title, String department, long applicationCount, LocalDate deadline) {
        this.id = id;
        this.title = title;
        this.department = department;
        this.applicationCount = applicationCount;
        this.deadline = deadline;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public long getApplicationCount() { return applicationCount; }
    public LocalDate getDeadline() { return deadline; }
}
