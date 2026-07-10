package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.CandidateReportRow;
import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.PipelineStageSummary;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.ApplicationStatusHistory;
import com.recruit.recruitmentapplication.entity.Company;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.ApplicationStatusHistoryRepository;
import com.recruit.recruitmentapplication.repository.CompanyRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.SkillRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobPostingService {
    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository, CompanyRepository companyRepository,
                             SkillRepository skillRepository, UserRepository userRepository,
                             ApplicationRepository applicationRepository,
                             ApplicationStatusHistoryRepository statusHistoryRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.companyRepository = companyRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findOpenJobs(String keyword) {
        return keyword == null || keyword.trim().isEmpty()
                ? jobPostingRepository.findOpenJobsWithCompany()
                : jobPostingRepository.findOpenJobsByTitle(keyword.trim());
    }

    @Transactional(readOnly = true)
    public JobPosting findById(Long id) {
        return jobPostingRepository.findByIdWithCompany(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public JobPosting findPublicDetail(Long id) {
        return jobPostingRepository.findActiveByIdWithDetails(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public JobPosting findManagedDetail(Long id, SessionUser user) {
        JobPosting posting = jobPostingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> notFound(id));
        ensureCanManagePosting(posting, user);
        return posting;
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findManagedJobs(SessionUser user, JobPosting.PostingStatus status, String department,
                                             String keyword) {
        ensureManagerRole(user);
        Long ownerId = managedOwnerId(user);
        return jobPostingRepository.findManagedJobsFiltered(ownerId, status, trimToNull(department),
                trimToNull(keyword));
    }

    @Transactional(readOnly = true)
    public List<String> findManagedDepartments(SessionUser user) {
        ensureManagerRole(user);
        return jobPostingRepository.findManagedDepartments(managedOwnerId(user));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countManagedByStatus(SessionUser user) {
        ensureManagerRole(user);
        Long ownerId = managedOwnerId(user);
        Map<String, Long> counts = new LinkedHashMap<>();
        long total = 0;
        for (JobPosting.PostingStatus status : JobPosting.PostingStatus.values()) {
            long count = jobPostingRepository.countManagedByStatus(status, ownerId);
            counts.put(status.name(), count);
            total += count;
        }
        counts.put("ALL", total);
        return counts;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> countApplicationsFor(List<JobPosting> jobs) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (JobPosting job : jobs) {
            counts.put(job.getId(), applicationRepository.countByJobPosting_Id(job.getId()));
        }
        return counts;
    }

    private Long managedOwnerId(SessionUser user) {
        return Role.ADMIN.equals(user.getRoleName()) ? null : user.getId();
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findByCompany(Long companyId) {
        return jobPostingRepository.findByCompany_Id(companyId);
    }

    @Transactional(readOnly = true)
    public List<com.recruit.recruitmentapplication.entity.Skill> findAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long countApplications(Long jobId) {
        return applicationRepository.countByJobPosting_Id(jobId);
    }

    @Transactional(readOnly = true)
    public List<PipelineStageSummary> buildPipelineSummary(Long jobId) {
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put("Applied", 0L);
        buckets.put("Screening", 0L);
        buckets.put("Interview", 0L);
        buckets.put("Offer", 0L);
        buckets.put("Hired", 0L);
        buckets.put("Rejected", 0L);
        buckets.put("Withdrawn", 0L);

        for (Object[] row : applicationRepository.countByJobPostingIdGroupedByStatus(jobId)) {
            Application.ApplicationStatus status = (Application.ApplicationStatus) row[0];
            long count = (Long) row[1];
            String bucket = pipelineBucket(status);
            buckets.put(bucket, buckets.get(bucket) + count);
        }

        long total = buckets.values().stream().mapToLong(Long::longValue).sum();
        List<PipelineStageSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : buckets.entrySet()) {
            int percentage = total == 0 ? 0 : (int) Math.round(entry.getValue() * 100.0 / total);
            summaries.add(new PipelineStageSummary(entry.getKey(), entry.getValue(), percentage));
        }
        return summaries;
    }

    @Transactional(readOnly = true)
    public List<CandidateReportRow> buildCandidateReport(Long jobId) {
        List<CandidateReportRow> rows = new ArrayList<>();
        for (Application application : applicationRepository.findByJobWithCandidate(jobId)) {
            String stage = pipelineBucket(application.getStatus());

            LocalDateTime stageSince = statusHistoryRepository
                    .findFirstByApplication_IdOrderByChangedAtDesc(application.getId())
                    .map(ApplicationStatusHistory::getChangedAt)
                    .orElse(application.getAppliedAt());
            long daysInStage = stageSince == null
                    ? 0
                    : ChronoUnit.DAYS.between(stageSince.toLocalDate(), LocalDate.now());

            String interviewerName = application.getInterviews().stream()
                    .filter(interview -> interview.getScheduledAt() != null)
                    .max(Comparator.comparing(Interview::getScheduledAt))
                    .map(Interview::getInterviewerName)
                    .orElse(null);

            rows.add(new CandidateReportRow(application.getCandidate().getName(), stage, daysInStage, interviewerName));
        }
        return rows;
    }

    @Transactional
    public JobPosting create(JobPostingForm form, SessionUser user) {
        ensureManagerRole(user);
        validateSalary(form.getSalaryMin(), form.getSalaryMax());
        Company company = findCompany(form.getCompanyId());
        User creator = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        JobPosting posting = new JobPosting(
                form.getTitle().trim(),
                form.getDepartment().trim(),
                form.getDescription().trim(),
                trimToNull(form.getRequirements()),
                form.getLocation().trim(),
                parseJobType(form.getJobType()),
                form.getSalaryMin(),
                form.getSalaryMax(),
                trimToNull(form.getSalaryRange()),
                form.getDeadline());
        posting.setCreatedBy(creator);
        posting.setStatus(JobPosting.PostingStatus.DRAFT);
        company.addJobPosting(posting);
        replaceSkills(posting, form);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting update(Long id, JobPostingForm form, SessionUser user) {
        validateSalary(form.getSalaryMin(), form.getSalaryMax());
        JobPosting posting = findManagedDetail(id, user);
        if (posting.getStatus() == JobPosting.PostingStatus.CLOSED) {
            throw new IllegalArgumentException("Closed job postings cannot be edited");
        }
        Company selectedCompany = findCompany(form.getCompanyId());
        if (!posting.getCompany().getId().equals(selectedCompany.getId())) {
            posting.getCompany().removeJobPosting(posting);
            selectedCompany.addJobPosting(posting);
        }
        posting.setTitle(form.getTitle().trim());
        posting.setDepartment(form.getDepartment().trim());
        posting.setDescription(form.getDescription().trim());
        posting.setRequirements(trimToNull(form.getRequirements()));
        posting.setLocation(form.getLocation().trim());
        posting.setJobType(parseJobType(form.getJobType()));
        posting.setSalaryMin(form.getSalaryMin());
        posting.setSalaryMax(form.getSalaryMax());
        posting.setSalaryRange(trimToNull(form.getSalaryRange()));
        posting.setDeadline(form.getDeadline());
        replaceSkills(posting, form);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting publish(Long id, SessionUser user) {
        JobPosting posting = findManagedDetail(id, user);
        if (posting.getStatus() != JobPosting.PostingStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft job postings can be published");
        }
        posting.setStatus(JobPosting.PostingStatus.ACTIVE);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public JobPosting close(Long id, SessionUser user) {
        JobPosting posting = findManagedDetail(id, user);
        if (posting.getStatus() != JobPosting.PostingStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active job postings can be closed");
        }
        posting.setStatus(JobPosting.PostingStatus.CLOSED);
        return jobPostingRepository.save(posting);
    }

    @Transactional
    public void deleteManaged(Long id, SessionUser user) {
        JobPosting posting = findManagedDetail(id, user);
        if (posting.getStatus() != JobPosting.PostingStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft job postings can be deleted");
        }
        if (applicationRepository.countByJobPosting_Id(id) > 0) {
            throw new IllegalArgumentException("Cannot delete a job posting that has applications");
        }
        jobPostingRepository.delete(posting);
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found id=" + id));
    }

    private void replaceSkills(JobPosting posting, JobPostingForm form) {
        posting.clearRequiredSkills();
        for (Long skillId : form.getSkillIds()) {
            posting.addRequiredSkill(skillRepository.findById(skillId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid skill")));
        }
    }

    private JobPosting.JobType parseJobType(String value) {
        try {
            return JobPosting.JobType.valueOf(value);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid job type");
        }
    }

    private void validateSalary(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
        }
    }

    private void ensureManagerRole(SessionUser user) {
        if (user == null || !(Role.ADMIN.equals(user.getRoleName()) || Role.HR_MANAGER.equals(user.getRoleName()))) {
            throw new IllegalArgumentException("Access denied");
        }
    }

    private void ensureCanManagePosting(JobPosting posting, SessionUser user) {
        ensureManagerRole(user);
        if (Role.ADMIN.equals(user.getRoleName())) {
            return;
        }
        if (posting.getCreatedBy() == null || !posting.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
    }

    private String pipelineBucket(Application.ApplicationStatus status) {
        return switch (status) {
            case SUBMITTED, APPLIED -> "Applied";
            case UNDER_REVIEW, SCREENING, SHORTLISTED -> "Screening";
            case INTERVIEW, INTERVIEW_SCHEDULED -> "Interview";
            case OFFER, OFFERED -> "Offer";
            case HIRED -> "Hired";
            case REJECTED -> "Rejected";
            case WITHDRAWN -> "Withdrawn";
        };
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private IllegalArgumentException notFound(Long id) {
        return new IllegalArgumentException("Job posting not found id=" + id);
    }
}
