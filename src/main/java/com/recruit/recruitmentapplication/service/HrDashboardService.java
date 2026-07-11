package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.ActiveJobDashboardRow;
import com.recruit.recruitmentapplication.dto.HrDashboardSummary;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HrDashboardService {
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobPostingRepository jobPostingRepository;

    public HrDashboardService(ApplicationRepository applicationRepository, InterviewRepository interviewRepository,
                              JobPostingRepository jobPostingRepository) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(readOnly = true)
    public HrDashboardSummary buildSummary(SessionUser user, LocalDate today) {
        ensureDashboardAccess(user);
        return new HrDashboardSummary(
                countActiveJobs(user),
                countApplicationsAwaitingReview(user),
                countUpcomingInterviews(user, today)
        );
    }

    @Transactional(readOnly = true)
    public List<ActiveJobDashboardRow> findActiveJobRows(SessionUser user) {
        ensureDashboardAccess(user);
        List<JobPosting> jobs = Role.ADMIN.equals(user.getRoleName())
                ? jobPostingRepository.findActiveDashboardJobs()
                : jobPostingRepository.findActiveDashboardJobsByOwner(user.getId());
        return jobs.stream()
                .map(job -> new ActiveJobDashboardRow(
                        job.getId(),
                        job.getTitle(),
                        job.getDepartment(),
                        applicationRepository.countByJobPosting_Id(job.getId()),
                        job.getDeadline()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public long countActiveJobs(SessionUser user) {
        ensureDashboardAccess(user);
        if (Role.ADMIN.equals(user.getRoleName())) {
            return jobPostingRepository.countByStatus(JobPosting.PostingStatus.ACTIVE);
        }
        return jobPostingRepository.countByStatusAndCreatedBy_Id(JobPosting.PostingStatus.ACTIVE, user.getId());
    }

    @Transactional(readOnly = true)
    public long countApplicationsAwaitingReview(SessionUser user) {
        ensureDashboardAccess(user);
        Application.ApplicationStatus awaitingReview = Application.ApplicationStatus.APPLIED;
        if (Role.ADMIN.equals(user.getRoleName())) {
            return applicationRepository.countByStatus(awaitingReview);
        }
        return applicationRepository.countByStatusAndJobPosting_CreatedBy_Id(awaitingReview, user.getId());
    }

    @Transactional(readOnly = true)
    public long countUpcomingInterviews(SessionUser user, LocalDate today) {
        ensureDashboardAccess(user);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(8).atStartOfDay();
        if (Role.ADMIN.equals(user.getRoleName())) {
            return interviewRepository.countUpcomingByResult(Interview.InterviewResult.PENDING, start, end);
        }
        return interviewRepository.countUpcomingByResultAndOwner(
                Interview.InterviewResult.PENDING,
                start,
                end,
                user.getId()
        );
    }

    private void ensureDashboardAccess(SessionUser user) {
        if (user == null || !(Role.ADMIN.equals(user.getRoleName()) || Role.HR_MANAGER.equals(user.getRoleName()))) {
            throw new IllegalArgumentException("Access denied");
        }
    }
}
