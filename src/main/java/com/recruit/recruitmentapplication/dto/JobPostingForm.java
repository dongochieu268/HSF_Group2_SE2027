package com.recruit.recruitmentapplication.dto;

import com.recruit.recruitmentapplication.entity.JobPosting;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JobPostingForm {
    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Job title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must be at most 100 characters")
    private String location;

    @NotBlank(message = "Job description is required")
    @Size(max = 4000, message = "Job description must be at most 4000 characters")
    private String description;

    @Size(max = 4000, message = "Requirements must be at most 4000 characters")
    private String requirements;

    @NotBlank(message = "Please select a job type")
    private String jobType;

    @DecimalMin(value = "0", message = "Minimum salary cannot be negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0", message = "Maximum salary cannot be negative")
    private BigDecimal salaryMax;

    @Size(max = 100, message = "Salary range must be at most 100 characters")
    private String salaryRange;

    private LocalDate deadline;

    @NotNull(message = "Please select a company")
    private Long companyId;

    private Set<Long> skillIds = new HashSet<>();

    public JobPostingForm() {}

    public static JobPostingForm from(JobPosting posting) {
        JobPostingForm form = new JobPostingForm();
        form.setTitle(posting.getTitle());
        form.setDepartment(posting.getDepartment());
        form.setLocation(posting.getLocation());
        form.setDescription(posting.getDescription());
        form.setRequirements(posting.getRequirements());
        form.setJobType(posting.getJobType() == null ? null : posting.getJobType().name());
        form.setSalaryMin(posting.getSalaryMin());
        form.setSalaryMax(posting.getSalaryMax());
        form.setSalaryRange(posting.getSalaryRange());
        form.setDeadline(posting.getDeadline());
        form.setCompanyId(posting.getCompany() == null ? null : posting.getCompany().getId());
        form.setSkillIds(posting.getRequiredSkills().stream().map(skill -> skill.getId()).collect(Collectors.toSet()));
        return form;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }
    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Set<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(Set<Long> skillIds) { this.skillIds = skillIds == null ? new HashSet<>() : skillIds; }
}
