package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class InterviewScheduleForm {

    @NotBlank(message = "Vui lòng chọn người phỏng vấn")
    private String interviewerName;

    @NotNull(message = "Vui lòng chọn ngày phỏng vấn")
    @FutureOrPresent(message = "Interview must be scheduled for a future date and time.")
    private LocalDate interviewDate;

    @NotBlank(message = "Vui lòng nhập giờ")
    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Format phải là HH:mm (24-hour clock)")
    private String interviewTime;

    @Size(max = 500, message = "Location cannot exceed 500 characters")
    private String location;

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }

    public String getInterviewTime() { return interviewTime; }
    public void setInterviewTime(String interviewTime) { this.interviewTime = interviewTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}