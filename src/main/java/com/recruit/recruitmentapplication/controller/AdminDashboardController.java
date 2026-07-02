package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ActivityLogRepository;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.repository.InterviewRepository;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    public AdminDashboardController(UserRepository userRepository, ActivityLogRepository activityLogRepository,
                                    JobPostingRepository jobPostingRepository, ApplicationRepository applicationRepository,
                                    InterviewRepository interviewRepository) {
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        // 1. Thống kê User và Nhóm theo Role
        List<User> allUsers = userRepository.findAllWithRole();
        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().getName(), Collectors.counting()));
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("usersByRole", usersByRole);

        // 2. Thống kê tài khoản bị khóa (locked = !enabled)
        long lockedCount = allUsers.stream().filter(u -> !u.isEnabled()).count();
        model.addAttribute("lockedCount", lockedCount);

        // 3. Lấy 10 hoạt động gần nhất
        model.addAttribute("recentActivities", activityLogRepository.findTop10ByOrderByCreatedAtDesc());

        // 4. Thống kê tuyển dụng (Recruitment Summary)
        List<JobPosting> activeJobs = jobPostingRepository.findOpenJobsWithCompany();
        model.addAttribute("activeJobsCount", activeJobs.size());
        model.addAttribute("activeJobs", activeJobs); // Phục vụ cho bảng Active jobs table

        List<Application> appliedApps = applicationRepository.findByStatus(Application.ApplicationStatus.APPLIED);
        model.addAttribute("awaitingReviewCount", appliedApps.size());

        // Đếm Interview sắp tới (trong 7 ngày tới)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next7Days = now.plusDays(7);
        long upcomingInterviewsCount = interviewRepository.findAll().stream()
                .filter(i -> i.getScheduledAt() != null && i.getScheduledAt().isAfter(now) && i.getScheduledAt().isBefore(next7Days))
                .count();
        model.addAttribute("upcomingInterviewsCount", upcomingInterviewsCount);

        return "dashboard/admin";
    }
}