package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.JobPosting.PostingStatus;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.service.JobPostingService;
import jakarta.validation.Validator;
import java.time.LocalDate;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PhaseFourJobPostingServiceTests {
    @Autowired private JobPostingService service;
    @Autowired private JobPostingRepository repository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private Validator validator;

    @Test
    void formRequiresPdfJobFieldsAndCompany() {
        JobPostingForm form = new JobPostingForm();
        var paths = validator.validate(form).stream().map(v -> v.getPropertyPath().toString()).toList();
        assertTrue(paths.contains("title"));
        assertTrue(paths.contains("department"));
        assertTrue(paths.contains("location"));
        assertTrue(paths.contains("description"));
        assertTrue(paths.contains("companyId"));
    }

    @Test
    void createSetsDraftOwnerAndPdfFields() {
        Company company = companyRepository.findByName("TechCorp Inc.").orElseThrow();

        JobPosting created = service.create(validForm("Phase Four Backend", company.getId()), hrManager());

        assertNotNull(created.getId());
        assertEquals(PostingStatus.DRAFT, created.getStatus());
        assertEquals("Engineering", created.getDepartment());
        assertEquals("Spring Boot and SQL experience", created.getRequirements());
        assertEquals("15-20M VND", created.getSalaryRange());
        assertEquals(LocalDate.now(), created.getPostedDate());
        assertEquals(userRepository.findByUsernameWithRole("hrmanager").orElseThrow().getId(), created.getCreatedBy().getId());
        assertEquals(company.getId(), created.getCompany().getId());
        assertTrue(company.getJobPostings().contains(created));
    }

    @Test
    void publicSearchReturnsOnlyActiveJobsWithInitializedCompany() {
        var jobs = service.findOpenJobs("java");
        assertFalse(jobs.isEmpty());
        assertTrue(jobs.stream().allMatch(j -> j.getStatus() == PostingStatus.ACTIVE));
        assertTrue(jobs.stream().allMatch(j -> j.getTitle().toLowerCase().contains("java")));
        assertTrue(jobs.stream().allMatch(j -> Hibernate.isInitialized(j.getCompany())));
    }

    @Test
    void managedDetailAllowsAdminButScopesHrManagerToOwnedJobs() {
        JobPosting job = repository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();

        assertEquals(job.getId(), service.findManagedDetail(job.getId(), admin()).getId());
        assertEquals(job.getId(), service.findManagedDetail(job.getId(), hrManager()).getId());

        SessionUser otherHr = new SessionUser(999L, "otherhr", "Other HR", Role.HR_MANAGER);
        assertThrows(IllegalArgumentException.class, () -> service.findManagedDetail(job.getId(), otherHr));
    }

    @Test
    void publishCloseAndDeleteRespectStatusAndApplicationRules() {
        Company company = companyRepository.findByName("Creative Studio").orElseThrow();
        JobPosting draft = service.create(validForm("Publish Close Job", company.getId()), hrManager());

        service.publish(draft.getId(), hrManager());
        assertEquals(PostingStatus.ACTIVE, repository.findById(draft.getId()).orElseThrow().getStatus());

        service.close(draft.getId(), hrManager());
        assertEquals(PostingStatus.CLOSED, repository.findById(draft.getId()).orElseThrow().getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.publish(draft.getId(), hrManager()));

        JobPosting deletableDraft = service.create(validForm("Delete Draft Job", company.getId()), hrManager());
        service.deleteManaged(deletableDraft.getId(), hrManager());
        repository.flush();
        assertFalse(repository.existsById(deletableDraft.getId()));

        JobPosting jobWithApplications = repository.findByTitleAndCompany_Name("Senior Java Developer", "TechCorp Inc.").orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> service.deleteManaged(jobWithApplications.getId(), admin()));
    }

    private JobPostingForm validForm(String title, Long companyId) {
        JobPostingForm form = new JobPostingForm();
        form.setTitle(title);
        form.setDepartment("Engineering");
        form.setDescription("Phase Four description");
        form.setRequirements("Spring Boot and SQL experience");
        form.setLocation("HCM");
        form.setJobType("FULL_TIME");
        form.setSalaryRange("15-20M VND");
        form.setDeadline(LocalDate.now().plusDays(30));
        form.setCompanyId(companyId);
        return form;
    }

    private SessionUser hrManager() {
        return SessionUser.from(userRepository.findByUsernameWithRole("hrmanager").orElseThrow());
    }

    private SessionUser admin() {
        return SessionUser.from(userRepository.findByUsernameWithRole("admin").orElseThrow());
    }
}
