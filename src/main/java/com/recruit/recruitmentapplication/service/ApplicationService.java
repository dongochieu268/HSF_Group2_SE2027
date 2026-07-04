package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Application.ApplicationStatus;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
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
        DISPLAY_STATUS.put(ApplicationStatus.SCREENING, "APPLIED");
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

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public static String displayStatus(ApplicationStatus status) {
        return DISPLAY_STATUS.getOrDefault(status, status.name());
    }

    public static boolean canWithdraw(ApplicationStatus status) {
        return WITHDRAWABLE_BUCKETS.contains(displayStatus(status));
    }

    @Transactional(readOnly = true)
    public List<Application> findMyApplications(Long candidateId, String displayStatusFilter) {
        List<Application> all = applicationRepository.findByCandidate_Id(candidateId);
        if (displayStatusFilter == null || displayStatusFilter.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(a -> displayStatus(a.getStatus()).equals(displayStatusFilter))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countMyApplicationsByStatus(Long candidateId) {
        List<Application> all = applicationRepository.findByCandidate_Id(candidateId);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("ALL", (long) all.size());
        for (String bucket : DISPLAY_BUCKETS) {
            counts.put(bucket, all.stream().filter(a -> displayStatus(a.getStatus()).equals(bucket)).count());
        }
        return counts;
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
