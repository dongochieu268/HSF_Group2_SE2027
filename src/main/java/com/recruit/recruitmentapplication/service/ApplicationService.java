package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.entity.ApplicationDocument;
import com.recruit.recruitmentapplication.entity.ApplicationDocument.DocumentType;
import com.recruit.recruitmentapplication.entity.ApplicationNote;
import com.recruit.recruitmentapplication.entity.ApplicationStatusHistory;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.Evaluation;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationDocumentRepository;
import com.recruit.recruitmentapplication.repository.ApplicationNoteRepository;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.ApplicationStatusHistoryRepository;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.EvaluationRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    // SCR-14: CV file upload - Required. PDF or DOCX only. Max 5 MB.
    private static final long MAX_CV_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CV_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private static final Set<String> ALLOWED_CV_EXTENSIONS = Set.of(".pdf", ".docx");

    private final ApplicationRepository applicationRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationNoteRepository applicationNoteRepository,
                              ApplicationStatusHistoryRepository statusHistoryRepository,
                              ApplicationDocumentRepository applicationDocumentRepository,
                              EvaluationRepository evaluationRepository,
                              UserRepository userRepository,
                              CandidateRepository candidateRepository,
                              JobPostingRepository jobPostingRepository,
                              FileStorageService fileStorageService) {
        this.applicationRepository = applicationRepository;
        this.applicationNoteRepository = applicationNoteRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.applicationDocumentRepository = applicationDocumentRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.fileStorageService = fileStorageService;
    }

    public static String displayStatus(ApplicationStatus status) {
        return DISPLAY_STATUS.getOrDefault(status, status.name());
    }

    public static boolean canWithdraw(ApplicationStatus status) {
        return WITHDRAWABLE_BUCKETS.contains(displayStatus(status));
    }

    // SCR-17: pipeline stage controls context-sensitive theo trạng thái hiện tại,
    // ẩn hoàn toàn ở các trạng thái cuối (HIRED/REJECTED/WITHDRAWN)
    public static boolean isTerminal(ApplicationStatus status) {
        String bucket = displayStatus(status);
        return bucket.equals("HIRED") || bucket.equals("REJECTED") || bucket.equals("WITHDRAWN");
    }

    public static ApplicationStatus nextStageStatus(ApplicationStatus status) {
        return switch (displayStatus(status)) {
            case "APPLIED" -> ApplicationStatus.SCREENING;
            case "SCREENING" -> ApplicationStatus.INTERVIEW;
            case "INTERVIEW" -> ApplicationStatus.OFFER;
            case "OFFER" -> ApplicationStatus.HIRED;
            default -> null;
        };
    }

    public static String nextStageLabel(ApplicationStatus status) {
        return switch (displayStatus(status)) {
            case "APPLIED" -> "Chuyển sang Screening";
            case "SCREENING" -> "Chuyển sang Interview";
            case "INTERVIEW" -> "Chuyển sang Offer";
            case "OFFER" -> "Đánh dấu Hired";
            default -> null;
        };
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
    public List<Application> findByJob(Long jobId) {
        return applicationRepository.findByJobWithCandidate(jobId);
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

    // SCR-17: HR/Admin (quản lý đầy đủ) hoặc Interviewer (chỉ xem, cho application được assign)
    @Transactional(readOnly = true)
    public Application findDetailForViewer(Long applicationId, SessionUser user) {
        Application application = applicationRepository.findWithDetailsById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn ứng tuyển id=" + applicationId));
        ensureCanView(application, user);
        return application;
    }

    private void ensureCanView(Application application, SessionUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Access denied");
        }
        if (Role.ADMIN.equals(user.getRoleName()) || Role.HR_MANAGER.equals(user.getRoleName())) {
            ensureCanManage(application, user);
            return;
        }
        if (Role.INTERVIEWER.equals(user.getRoleName())) {
            boolean assigned = application.getInterviews().stream()
                    .anyMatch(iv -> iv.getInterviewer() != null && iv.getInterviewer().getId().equals(user.getId()));
            if (!assigned) {
                throw new IllegalArgumentException("Access denied");
            }
            return;
        }
        throw new IllegalArgumentException("Access denied");
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
    public List<ApplicationDocument> listDocuments(Long applicationId) {
        return applicationDocumentRepository.findByApplication_IdOrderByUploadedAtDesc(applicationId);
    }

    // SCR-17: "Evaluation summary" - danh sách đánh giá đã nộp cho đơn ứng tuyển này
    @Transactional(readOnly = true)
    public List<Evaluation> listEvaluations(Long applicationId) {
        return evaluationRepository.findByApplicationId(applicationId);
    }

    // SCR-17: "Download CV" - quyền được kiểm tra server-side trước khi trả file
    @Transactional(readOnly = true)
    public ApplicationDocument findDocumentForDownload(Long applicationId, Long documentId, SessionUser user) {
        Application application = findDetailForViewer(applicationId, user);
        return applicationDocumentRepository.findById(documentId)
                .filter(doc -> doc.getApplication().getId().equals(application.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
    }

    public Path resolveStoragePath(String storagePath) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(storagePath);
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

    // ===== Apply Now: ứng viên nộp đơn ứng tuyển từ trang chi tiết công việc =====

    @Transactional(readOnly = true)
    public boolean hasApplied(Long candidateId, Long jobId) {
        if (candidateId == null || jobId == null) {
            return false;
        }
        return applicationRepository.existsByCandidate_IdAndJobPosting_Id(candidateId, jobId);
    }

    @Transactional
    public Application apply(Long jobId, Long candidateId, String coverLetter, MultipartFile cvFile) {
        JobPosting jobPosting = jobPostingRepository.findActiveByIdWithDetails(jobId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vị trí tuyển dụng không tồn tại hoặc không còn nhận hồ sơ"));
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ ứng viên"));
        if (applicationRepository.existsByCandidate_IdAndJobPosting_Id(candidateId, jobId)) {
            throw new IllegalArgumentException("Bạn đã ứng tuyển vị trí này rồi");
        }
        validateCv(cvFile);

        String trimmedCoverLetter = coverLetter == null || coverLetter.isBlank() ? null : coverLetter.trim();
        Application application = new Application(candidate, jobPosting, trimmedCoverLetter);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus(ApplicationStatus.SUBMITTED);
        Application saved = applicationRepository.save(application);
        statusHistoryRepository.save(new ApplicationStatusHistory(
                saved, null, ApplicationStatus.SUBMITTED.name(), null, "Ứng viên nộp đơn ứng tuyển"));

        String storagePath = fileStorageService.store(cvFile, "applications/" + saved.getId());
        applicationDocumentRepository.save(new ApplicationDocument(
                saved, DocumentType.CV, cvFile.getOriginalFilename(), cvFile.getContentType(),
                cvFile.getSize(), storagePath));
        return saved;
    }

    // SCR-14: CV bắt buộc, chỉ nhận PDF/DOCX, tối đa 5MB
    private void validateCv(MultipartFile cvFile) {
        if (cvFile == null || cvFile.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng đính kèm CV (bắt buộc)");
        }
        if (cvFile.getSize() > MAX_CV_SIZE_BYTES) {
            throw new IllegalArgumentException("Dung lượng CV vượt quá 5MB");
        }
        String extension = extensionOf(cvFile.getOriginalFilename());
        boolean validExtension = ALLOWED_CV_EXTENSIONS.contains(extension.toLowerCase());
        boolean validContentType = ALLOWED_CV_CONTENT_TYPES.contains(cvFile.getContentType());
        if (!validExtension || !validContentType) {
            throw new IllegalArgumentException("CV chỉ chấp nhận định dạng PDF hoặc DOCX");
        }
    }

    private String extensionOf(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        return dotIndex < 0 ? "" : originalFileName.substring(dotIndex);
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
