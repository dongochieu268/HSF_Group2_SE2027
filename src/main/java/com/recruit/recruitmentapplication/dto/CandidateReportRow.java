package com.recruit.recruitmentapplication.dto;

public class CandidateReportRow {
    private final String candidateName;
    private final String currentStage;
    private final long daysInStage;
    private final String interviewerName;

    public CandidateReportRow(String candidateName, String currentStage, long daysInStage, String interviewerName) {
        this.candidateName = candidateName;
        this.currentStage = currentStage;
        this.daysInStage = daysInStage;
        this.interviewerName = interviewerName;
    }

    public String getCandidateName() { return candidateName; }
    public String getCurrentStage() { return currentStage; }
    public long getDaysInStage() { return daysInStage; }
    public String getInterviewerName() { return interviewerName; }
}
