package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.InterviewScheduleForm;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.ApplicationRepository;
import com.recruit.recruitmentapplication.service.InterviewService;
import com.recruit.recruitmentapplication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manage/applications/{applicationId}/interviews")
public class InterviewController {
    private final InterviewService interviewService;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    public InterviewController(InterviewService interviewService, ApplicationRepository applicationRepository, UserService userService) {
        this.interviewService = interviewService;
        this.applicationRepository = applicationRepository;
        this.userService = userService;
    }

    @GetMapping("/new")
    public String showScheduleForm(@PathVariable Long applicationId, Model model) {
        Application application = applicationRepository.findWithDetailsById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        prepareModel(model, application, new InterviewScheduleForm());
        return "interview/schedule-form";
    }

    @PostMapping("/new")
    public String scheduleInterview(@PathVariable Long applicationId,
                                    @Valid @ModelAttribute("interviewForm") InterviewScheduleForm form,
                                    BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        Application application = applicationRepository.findWithDetailsById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (result.hasErrors()) {
            prepareModel(model, application, form);
            return "interview/schedule-form";
        }

        try {
            Interview scheduledInterview = interviewService.scheduleInterview(applicationId, form);
            // Flash message chuyển hướng theo document SCR-18
            redirectAttributes.addFlashAttribute("successMessage",
                    "Interview scheduled. " + scheduledInterview.getInterviewer().getFullName() + " has been assigned.");
            return "redirect:/manage/applications/" + applicationId; // Quay về Application Detail (SCR-17)
        } catch (IllegalArgumentException e) {
            // Map lỗi vào trường interviewDate trên view
            result.rejectValue("interviewDate", "error.interviewDate", e.getMessage());
            prepareModel(model, application, form);
            return "interview/schedule-form";
        }
    }

    private void prepareModel(Model model, Application application, InterviewScheduleForm form) {
        model.addAttribute("app", application);
        model.addAttribute("interviewForm", form);

        // Theo chuẩn tài liệu: Lấy các INTERVIEWER đang Active (enabled) và xếp theo tên (fullName)
        List<User> interviewers = userService.findAll().stream()
                .filter(u -> "INTERVIEWER".equals(u.getRole().getName()) && u.isEnabled())
                .sorted((u1, u2) -> u1.getFullName().compareToIgnoreCase(u2.getFullName()))
                .toList();

        model.addAttribute("interviewers", interviewers);
    }
}