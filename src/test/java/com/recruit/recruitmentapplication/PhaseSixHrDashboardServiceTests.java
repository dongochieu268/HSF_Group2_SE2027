package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.dto.ActiveJobDashboardRow;
import com.recruit.recruitmentapplication.dto.HrDashboardSummary;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.service.HrDashboardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@Transactional
class PhaseSixHrDashboardServiceTests {
    @Autowired private HrDashboardService dashboardService;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private JobPostingRepository jobPostingRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void applicationsAwaitingReviewCountsOnlyAppliedStatus() {
        applicationRepository.updateStatus(3L, Application.ApplicationStatus.APPLIED);
        applicationRepository.updateStatus(4L, Application.ApplicationStatus.SUBMITTED);

        SessionUser hrManager = SessionUser.from(userRepository.findByUsernameWithRole("hrmanager").orElseThrow());
        SessionUser admin = SessionUser.from(userRepository.findByUsernameWithRole("admin").orElseThrow());

        assertEquals(1, dashboardService.countApplicationsAwaitingReview(hrManager));
        assertEquals(1, dashboardService.countApplicationsAwaitingReview(admin));
    }

    @Test
    void summaryScopesHrManagerDataAndLetsAdminSeeAllData() {
        applicationRepository.updateStatus(3L, Application.ApplicationStatus.APPLIED);
        applicationRepository.updateStatus(4L, Application.ApplicationStatus.SUBMITTED);
        createPendingInterview(3L, LocalDateTime.of(2026, 7, 7, 10, 0));
        createOtherHrActiveJobWithAppliedApplicationAndInterview();

        HrDashboardSummary hrSummary = dashboardService.buildSummary(
                session("hrmanager"),
                LocalDate.of(2026, 7, 3)
        );
        HrDashboardSummary adminSummary = dashboardService.buildSummary(
                session("admin"),
                LocalDate.of(2026, 7, 3)
        );

        assertEquals(3, hrSummary.getActiveJobsCount());
        assertEquals(1, hrSummary.getApplicationsAwaitingReviewCount());
        assertEquals(1, hrSummary.getUpcomingInterviewsCount());
        assertEquals(4, adminSummary.getActiveJobsCount());
        assertEquals(2, adminSummary.getApplicationsAwaitingReviewCount());
        assertEquals(2, adminSummary.getUpcomingInterviewsCount());
    }

    @Test
    void activeJobRowsAreScopedSortedMostRecentFirstAndIncludeApplicationCounts() {
        List<ActiveJobDashboardRow> rows = dashboardService.findActiveJobRows(session("hrmanager"));

        assertIterableEquals(List.of("Data Analyst", "Frontend Developer", "Senior Java Developer"),
                rows.stream().map(ActiveJobDashboardRow::getTitle).toList());
        assertEquals("Analytics", rows.get(0).getDepartment());
        assertEquals(LocalDate.of(2026, 7, 16), rows.get(0).getDeadline());
        assertEquals(1, rows.get(0).getApplicationCount());
        assertEquals(2, rows.get(1).getApplicationCount());
        assertEquals(1, rows.get(2).getApplicationCount());
    }

    private void createOtherHrActiveJobWithAppliedApplicationAndInterview() {
        Role hrRole = roleRepository.findByName(Role.HR_MANAGER).orElseThrow();
        User otherHr = userRepository.save(new User(
                "dashboardhr",
                "hash",
                "dashboardhr@example.com",
                "Dashboard HR",
                hrRole
        ));
        Company company = companyRepository.findByName("Creative Studio").orElseThrow();
        JobPosting job = new JobPosting(
                "Other HR Active Job",
                "Operations",
                "Owned by another HR manager",
                "Operations experience",
                "Da Nang",
                JobPosting.JobType.FULL_TIME,
                null,
                null,
                null,
                LocalDate.of(2026, 8, 15)
        );
        job.setStatus(JobPosting.PostingStatus.ACTIVE);
        job.setPostedDate(LocalDate.of(2026, 7, 2));
        job.setCreatedBy(otherHr);
        company.addJobPosting(job);
        JobPosting savedJob = jobPostingRepository.save(job);

        Candidate candidate = candidateRepository.findById(4L).orElseThrow();
        Application application = new Application(candidate, savedJob, "Other HR candidate");
        application.setStatus(Application.ApplicationStatus.APPLIED);
        Application savedApplication = applicationRepository.save(application);
        Interview interview = new Interview(
                LocalDateTime.of(2026, 7, 8, 9, 30),
                Interview.InterviewType.HR,
                "Other Interviewer"
        );
        interview.setResult(Interview.InterviewResult.PENDING);
        savedApplication.addInterview(interview);
        interviewRepository.save(interview);
    }

    private void createPendingInterview(Long applicationId, LocalDateTime scheduledAt) {
        Application application = applicationRepository.findById(applicationId).orElseThrow();
        Interview interview = new Interview(scheduledAt, Interview.InterviewType.HR, "Jane Interviewer");
        interview.setResult(Interview.InterviewResult.PENDING);
        application.addInterview(interview);
        interviewRepository.save(interview);
    }

    private SessionUser session(String username) {
        return SessionUser.from(userRepository.findByUsernameWithRole(username).orElseThrow());
    }
}
