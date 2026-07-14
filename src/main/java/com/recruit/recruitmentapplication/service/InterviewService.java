package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.InterviewScheduleForm;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.ApplicationStatusHistory;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.ApplicationStatusHistoryRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class InterviewService {
    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;

    public InterviewService(InterviewRepository interviewRepository, ApplicationRepository applicationRepository, UserRepository userRepository, ApplicationStatusHistoryRepository statusHistoryRepository) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
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

        User interviewer = userRepository.findById(form.getInterviewerId())
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found"));

        Interview interview = new Interview(scheduledAt, Interview.InterviewType.TECHNICAL, interviewer);
        interview.setNotes(form.getLocation()); // Lưu location/meeting link vào notes

        application.addInterview(interview);

        // SCR-18: Cập nhật trạng thái application và lưu lịch sử (nếu cần thiết)
        if (application.getStatus() == Application.ApplicationStatus.APPLIED ||
            application.getStatus() == Application.ApplicationStatus.SCREENING ||
            application.getStatus() == Application.ApplicationStatus.SHORTLISTED ||
            application.getStatus() == Application.ApplicationStatus.UNDER_REVIEW) {
            
            Application.ApplicationStatus oldStatus = application.getStatus();
            application.setStatus(Application.ApplicationStatus.INTERVIEW_SCHEDULED);
            statusHistoryRepository.save(new ApplicationStatusHistory(application, oldStatus.name(),
                    Application.ApplicationStatus.INTERVIEW_SCHEDULED.name(), null, "Interview scheduled"));
        }

        // SCR-18: Gửi email notification (Mock)
        log.info("Sending email to candidate {} ({}): Interview scheduled on {} at {} with {}",
                application.getCandidate().getName(), application.getCandidate().getEmail(),
                form.getInterviewDate(), form.getInterviewTime(), interviewer.getFullName());
        log.info("Sending email to interviewer {} ({}): You have been assigned an interview with {} on {} at {}",
                interviewer.getFullName(), interviewer.getEmail(),
                application.getCandidate().getName(), form.getInterviewDate(), form.getInterviewTime());

        return interviewRepository.save(interview);
    }
}