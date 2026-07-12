package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.ApplicationNote;
import com.recruit.recruitmentapplication.entity.ApplicationStatusHistory;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationDocumentRepository;
import com.recruit.recruitmentapplication.repository.ApplicationNoteRepository;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.ApplicationStatusHistoryRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    // enum ApplicationStatus có nhiều giá trị hơn 7 tab của SCR-15, tạm gom nhóm ở đây
    private static final Map<ApplicationStatus, String> DISPLAY_STATUS = new LinkedHashMap<>();
    static {
        DISPLAY_STATUS.put(ApplicationStatus.SUBMITTED, "APPLIED");
        DISPLAY_STATUS.put(ApplicationStatus.APPLIED, "APPLIED");
        DISPLAY_STATUS.put(ApplicationStatus.UNDER_REVIEW, "SCREENING");
        DISPLAY_STATUS.put(ApplicationStatus.SCREENING, "SCREENING");
        DISPLAY_STATUS.put(ApplicationStatus.SHORTLISTED, "SCREENING");
        DISPLAY_STATUS.put(ApplicationStatus.INTERVIEW, "INTERVIEW");
        DISPLAY_STATUS.put(ApplicationStatus.INTERVIEW_SCHEDULED, "INTERVIEW");
        DISPLAY_STATUS.put(ApplicationStatus.OFFER, "OFFER");
        DISPLAY_STATUS.put(ApplicationStatus.OFFERED, "OFFER");
        DISPLAY_STATUS.put(ApplicationStatus.HIRED, "HIRED");
        DISPLAY_STATUS.put(ApplicationStatus.REJECTED, "REJECTED");
        DISPLAY_STATUS.put(ApplicationStatus.WITHDRAWN, "WITHDRAWN");
    }

    private static final List<String> WITHDRAWABLE_BUCKETS = List.of("APPLIED", "SCREENING");
    private static final List<String> DISPLAY_BUCKETS =
            List.of("APPLIED", "SCREENING", "INTERVIEW", "OFFER", "HIRED", "REJECTED", "WITHDRAWN");

    private final ApplicationRepository applicationRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationNoteRepository applicationNoteRepository,
                              ApplicationStatusHistoryRepository statusHistoryRepository,
                              ApplicationDocumentRepository applicationDocumentRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationNoteRepository = applicationNoteRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.applicationDocumentRepository = applicationDocumentRepository;
        this.userRepository = userRepository;
    }

    public static String displayStatus(ApplicationStatus status) {
        return DISPLAY_STATUS.getOrDefault(status, status.name());
    }

    public static boolean canWithdraw(ApplicationStatus status) {
        return WITHDRAWABLE_BUCKETS.contains(displayStatus(status));
    }

    @Transactional(readOnly = true)
    public List<Application> findMyApplications(Long candidateId, String displayStatusFilter) {
        List<Application> all = applicationRepository.findByCandidateWithJob(candidateId);
        if (displayStatusFilter == null || displayStatusFilter.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(a -> displayStatus(a.getStatus()).equals(displayStatusFilter))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countMyApplicationsByStatus(Long candidateId) {
        List<Application> all = applicationRepository.findByCandidateWithJob(candidateId);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("ALL", (long) all.size());
        for (String bucket : DISPLAY_BUCKETS) {
            counts.put(bucket, all.stream().filter(a -> displayStatus(a.getStatus()).equals(bucket)).count());
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public List<Application> findManagedByJob(Long jobId, SessionUser user) {
        List<Application> applications = applicationRepository.findByJobWithCandidate(jobId);
        for (Application application : applications) {
            ensureCanManage(application, user);
        }
        return applications;
    }

    // ===== SCR-17: Application Detail =====

    @Transactional(readOnly = true)
    public Application findManagedDetail(Long applicationId, SessionUser user) {
        Application application = applicationRepository.findWithDetailsById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển id=" + applicationId));
        ensureCanManage(application, user);
        return application;
    }

    @Transactional(readOnly = true)
    public List<ApplicationNote> listNotes(Long applicationId) {
        return applicationNoteRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistory> listStatusHistory(Long applicationId) {
        return statusHistoryRepository.findByApplication_IdOrderByChangedAtDesc(applicationId);
    }

    @Transactional(readOnly = true)
    public List<com.recruit.recruitmentapplication.entity.ApplicationDocument> listDocuments(Long applicationId) {
        return applicationDocumentRepository.findByApplication_IdOrderByUploadedAtDesc(applicationId);
    }

    @Transactional
    public void addNote(Long applicationId, SessionUser sessionUser, String content) {
        Application application = findManagedDetail(applicationId, sessionUser);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Nội dung ghi chú không được để trống");
        }
        User author = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        applicationNoteRepository.save(new ApplicationNote(application, author, content.trim()));
    }

    @Transactional
    public void changeStatus(Long applicationId, SessionUser sessionUser, ApplicationStatus newStatus, String note) {
        Application application = findManagedDetail(applicationId, sessionUser);
        ApplicationStatus oldStatus = application.getStatus();
        if (oldStatus == newStatus) {
            return;
        }
        User changedBy = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        application.setStatus(newStatus);
        applicationRepository.save(application);
        statusHistoryRepository.save(new ApplicationStatusHistory(
                application, oldStatus.name(), newStatus.name(), changedBy, note));
    }

    @Transactional
    public void scheduleInterview(Long applicationId, SessionUser sessionUser, LocalDateTime scheduledAt,
                                  Interview.InterviewType type, Long interviewerId) {
        Application application = findManagedDetail(applicationId, sessionUser);
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Vui lòng chọn thời gian phỏng vấn");
        }
        User interviewer = null;
        if (interviewerId != null) {
            interviewer = userRepository.findById(interviewerId)
                    .filter(u -> Role.INTERVIEWER.equals(u.getRole().getName()))
                    .orElseThrow(() -> new IllegalArgumentException("Interviewer không hợp lệ"));
        }
        Interview interview = new Interview(scheduledAt, type, interviewer);
        application.addInterview(interview);
        if (displayStatus(application.getStatus()).equals("APPLIED")
                || displayStatus(application.getStatus()).equals("SCREENING")) {
            ApplicationStatus oldStatus = application.getStatus();
            application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
            statusHistoryRepository.save(new ApplicationStatusHistory(application, oldStatus.name(),
                    ApplicationStatus.INTERVIEW_SCHEDULED.name(), null, "Lên lịch phỏng vấn"));
        }
        applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<User> listInterviewerAccounts() {
        return userRepository.findByRole_Name(Role.INTERVIEWER);
    }

    private void ensureCanManage(Application application, SessionUser user) {
        if (user == null || !(Role.ADMIN.equals(user.getRoleName()) || Role.HR_MANAGER.equals(user.getRoleName()))) {
            throw new IllegalArgumentException("Access denied");
        }
        if (Role.ADMIN.equals(user.getRoleName())) {
            return;
        }
        User owner = application.getJobPosting().getCreatedBy();
        if (owner == null || !owner.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
    }

    @Transactional
    public void withdraw(Long applicationId, Long candidateId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển id=" + applicationId));
        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new IllegalArgumentException("Đơn ứng tuyển không thuộc về ứng viên này");
        }
        if (!canWithdraw(application.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể rút đơn khi đang ở trạng thái Applied hoặc Screening");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }
}
