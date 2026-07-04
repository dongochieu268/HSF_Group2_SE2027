package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.service.CandidateService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/my-applications")
public class MyApplicationController {
    private final ApplicationService applicationService;
    private final CandidateService candidateService;

    public MyApplicationController(ApplicationService applicationService, CandidateService candidateService) {
        this.applicationService = applicationService;
        this.candidateService = candidateService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model, HttpSession session) {
        Long candidateId = candidateService.getOrCreateProfileForUser(current(session).getId()).getId();
        List<Application> applications = applicationService.findMyApplications(candidateId, status);
        model.addAttribute("applications", applications);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statusCounts", applicationService.countMyApplicationsByStatus(candidateId));
        return "candidate/my-applications";
    }

    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id, HttpSession session) {
        Long candidateId = candidateService.getOrCreateProfileForUser(current(session).getId()).getId();
        try {
            applicationService.withdraw(id, candidateId);
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
        return "redirect:/my-applications";
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
