package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.InterviewScheduleForm;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;

    public InterviewService(InterviewRepository interviewRepository, ApplicationRepository applicationRepository) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Interview scheduleInterview(Long applicationId, InterviewScheduleForm form) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        LocalTime time = LocalTime.parse(form.getInterviewTime());
        LocalDateTime scheduledAt = form.getInterviewDate().atTime(time);

        // Validation: Cấm tạo lịch trong quá khứ (SCR-18 requirement)
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Interview must be scheduled for a future date and time.");
        }

        Interview interview = new Interview(scheduledAt, Interview.InterviewType.TECHNICAL, form.getInterviewerName());
        interview.setNotes(form.getLocation()); // Lưu location/meeting link vào notes

        application.addInterview(interview);
        return interviewRepository.save(interview);
    }
}