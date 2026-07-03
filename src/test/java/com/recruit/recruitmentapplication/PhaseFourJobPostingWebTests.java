package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhaseFourJobPostingWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private JobPostingRepository jobRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void publicJobsAreReadableAndShowOnlyActivePostings() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobposting/list"))
                .andExpect(content().string(containsString("Senior Java Developer")))
                .andExpect(content().string(containsString("TechCorp Inc.")))
                .andExpect(content().string(not(containsString("Đăng tin mới"))));
        mockMvc.perform(get("/jobs/{id}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Develop backend services")));
    }

    @Test
    void searchFiltersActiveJobsByTitle() throws Exception {
        mockMvc.perform(get("/jobs").param("keyword", "frontend"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Frontend Developer")))
                .andExpect(content().string(not(containsString("Senior Java Developer"))));
    }

    @Test
    void candidateCannotUseInternalJobManagementRoutes() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();
        MockHttpSession candidate = session("alice");
        mockMvc.perform(get("/manage/jobs/{id}", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(get("/manage/jobs/new").session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(get("/manage/jobs/{id}/edit", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/manage/jobs").session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/manage/jobs/{id}/publish", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/manage/jobs/{id}/close", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(post("/manage/jobs/{id}/delete", job.getId()).session(candidate)).andExpect(redirectedUrl("/error/403"));
    }

    @Test
    void hrManagerCanOpenFormWithPdfFields() throws Exception {
        mockMvc.perform(get("/manage/jobs/new").session(session("hrmanager")))
                .andExpect(status().isOk()).andExpect(view().name("jobposting/form"))
                .andExpect(content().string(containsString("name=\"department\"")))
                .andExpect(content().string(containsString("name=\"requirements\"")))
                .andExpect(content().string(containsString("name=\"salaryRange\"")))
                .andExpect(content().string(containsString("TechCorp Inc.")));
    }

    @Test
    void adminCanCreateDraftJob() throws Exception {
        Company company = companyRepository.findByName("FinanceHub Ltd.").orElseThrow();
        mockMvc.perform(post("/manage/jobs").session(session("admin"))
                        .param("title", "Phase Four Web Job").param("department", "Data")
                        .param("description", "Created from web").param("requirements", "SQL reporting")
                        .param("location", "Ha Noi").param("jobType", "REMOTE")
                        .param("salaryRange", "20-30M VND")
                        .param("deadline", "2026-12-31").param("companyId", company.getId().toString()))
                .andExpect(status().is3xxRedirection());
        assertTrue(jobRepository.findByTitleAndCompany_Name("Phase Four Web Job", "FinanceHub Ltd.").isPresent());
    }

    @Test
    void managedDetailShowsScr12SummaryAndActionsByStatus() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();

        mockMvc.perform(get("/manage/jobs/{id}", job.getId()).session(session("hrmanager")))
                .andExpect(status().isOk())
                .andExpect(view().name("jobposting/manage-detail"))
                .andExpect(content().string(containsString("Senior Java Developer")))
                .andExpect(content().string(containsString("Engineering")))
                .andExpect(content().string(containsString("Backend service development")))
                .andExpect(content().string(containsString("Total applications")))
                .andExpect(content().string(containsString("Applied")))
                .andExpect(content().string(containsString("Screening")))
                .andExpect(content().string(containsString("Interview")))
                .andExpect(content().string(containsString("Offer")))
                .andExpect(content().string(containsString("Hired")))
                .andExpect(content().string(containsString("Rejected")))
                .andExpect(content().string(containsString("Withdrawn")))
                .andExpect(content().string(containsString("/manage/jobs/" + job.getId() + "/applications")))
                .andExpect(content().string(containsString("/manage/jobs/" + job.getId() + "/report")))
                .andExpect(content().string(containsString("Close")))
                .andExpect(content().string(not(containsString("Publish"))))
                .andExpect(content().string(not(containsString("Delete"))));
    }

    @Test
    void draftManagedDetailUsesReadOnlyInfoFormAndShowsZeroPipelineStages() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("DevOps Engineer", "FinanceHub Ltd.").orElseThrow();

        mockMvc.perform(get("/manage/jobs/{id}", job.getId()).session(session("hrmanager")))
                .andExpect(status().isOk())
                .andExpect(view().name("jobposting/manage-detail"))
                .andExpect(content().string(containsString("readonly-job-form")))
                .andExpect(content().string(containsString("name=\"department\"")))
                .andExpect(content().string(containsString("readonly")))
                .andExpect(content().string(containsString("Platform")))
                .andExpect(content().string(containsString("Docker, Kubernetes, AWS, and CI/CD experience.")))
                .andExpect(content().string(containsString("Total applications")))
                .andExpect(content().string(containsString("Applied")))
                .andExpect(content().string(containsString("Screening")))
                .andExpect(content().string(containsString("Interview")))
                .andExpect(content().string(containsString("Offer")))
                .andExpect(content().string(containsString("Hired")))
                .andExpect(content().string(containsString("Rejected")))
                .andExpect(content().string(containsString("Withdrawn")))
                .andExpect(content().string(containsString("Edit")))
                .andExpect(content().string(containsString("Publish")))
                .andExpect(content().string(containsString("Delete")))
                .andExpect(content().string(not(containsString("Close"))));
    }

    @Test
    void hrManagerCannotViewAnotherHrManagersJobButAdminCan() throws Exception {
        JobPosting job = jobRepository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();
        MockHttpSession otherHr = new MockHttpSession();
        otherHr.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(999L, "otherhr", "Other HR", Role.HR_MANAGER));

        mockMvc.perform(get("/manage/jobs/{id}", job.getId()).session(otherHr))
                .andExpect(redirectedUrl("/error/403"));
        mockMvc.perform(get("/manage/jobs/{id}", job.getId()).session(session("admin")))
                .andExpect(status().isOk());
    }

    private MockHttpSession session(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                SessionUser.from(userRepository.findByUsernameWithRole(username).orElseThrow()));
        return session;
    }
}
