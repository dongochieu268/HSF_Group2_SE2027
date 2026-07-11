package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhaseSixHrDashboardWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private JobPostingRepository jobPostingRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void dashboardRequiresHrManagerOrAdmin() throws Exception {
        mockMvc.perform(get("/hr/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        mockMvc.perform(get("/hr/dashboard").session(session("alice")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));

        MockHttpSession interviewer = new MockHttpSession();
        interviewer.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(77L, "interviewer", "Interview User", Role.INTERVIEWER));
        mockMvc.perform(get("/hr/dashboard").session(interviewer))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));

        mockMvc.perform(get("/hr/dashboard").session(session("hrmanager")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/hr"));
        mockMvc.perform(get("/hr/dashboard").session(session("admin")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/hr"));
    }

    @Test
    void dashboardShowsScr06MetricsTableAndNavigation() throws Exception {
        applicationRepository.updateStatus(3L, Application.ApplicationStatus.APPLIED);

        mockMvc.perform(get("/hr/dashboard").session(session("hrmanager")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HR Dashboard")))
                .andExpect(content().string(containsString("Active Jobs")))
                .andExpect(content().string(containsString("data-metric=\"active-jobs\"")))
                .andExpect(content().string(containsString("data-value=\"3\"")))
                .andExpect(content().string(containsString("Applications Awaiting Review")))
                .andExpect(content().string(containsString("data-metric=\"awaiting-review\"")))
                .andExpect(content().string(containsString("data-value=\"1\"")))
                .andExpect(content().string(containsString("Upcoming Interviews")))
                .andExpect(content().string(containsString("data-metric=\"upcoming-interviews\"")))
                .andExpect(content().string(containsString("/manage/jobs?status=ACTIVE")))
                .andExpect(content().string(containsString("/manage/jobs?applicationStatus=APPLIED")))
                .andExpect(content().string(containsString("/manage/jobs/new")))
                .andExpect(content().string(containsString("Senior Java Developer")))
                .andExpect(content().string(containsString("Frontend Developer")))
                .andExpect(content().string(containsString("Data Analyst")))
                .andExpect(content().string(containsString("/manage/jobs/1")))
                .andExpect(content().string(containsString("/manage/jobs/2/applications")))
                .andExpect(content().string(containsString("Dashboard")))
                .andExpect(content().string(containsString("href=\"/manage/jobs\">Jobs</a>")))
                .andExpect(content().string(not(containsString("href=\"/jobs\">Jobs</a>"))))
                .andExpect(content().string(not(containsString("app-sidebar"))))
                .andExpect(content().string(containsString("Profile")));
    }

    @Test
    void loginRedirectsHrManagerToHrDashboardAndKeepsAdminDashboardRedirect() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("username", "hrmanager")
                        .param("password", "Recruiter@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/hr/dashboard"))
                .andExpect(request().sessionAttribute(SessionConstants.LOGGED_IN_USER,
                        org.hamcrest.Matchers.instanceOf(SessionUser.class)));

        mockMvc.perform(post("/auth/login")
                        .param("username", "admin")
                        .param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    void dashboardShowsEmptyStateWhenNoActiveJobsAreInScope() throws Exception {
        jobPostingRepository.updateStatus(1L, JobPosting.PostingStatus.CLOSED);
        jobPostingRepository.updateStatus(2L, JobPosting.PostingStatus.CLOSED);
        jobPostingRepository.updateStatus(3L, JobPosting.PostingStatus.CLOSED);

        mockMvc.perform(get("/hr/dashboard").session(session("hrmanager")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You have no active job postings. Create one to start receiving applications.")))
                .andExpect(content().string(containsString("/manage/jobs/new")))
                .andExpect(content().string(not(containsString("<table class=\"dashboard-table\""))));
    }

    private MockHttpSession session(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                SessionUser.from(userRepository.findByUsernameWithRole(username).orElseThrow()));
        return session;
    }
}
