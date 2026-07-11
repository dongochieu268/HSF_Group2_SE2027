package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.service.HrDashboardService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hr/dashboard")
public class HrDashboardController {
    private final HrDashboardService hrDashboardService;

    public HrDashboardController(HrDashboardService hrDashboardService) {
        this.hrDashboardService = hrDashboardService;
    }

    @GetMapping
    public String showDashboard(Model model, HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
        try {
            model.addAttribute("summary", hrDashboardService.buildSummary(user, LocalDate.now()));
            model.addAttribute("activeJobs", hrDashboardService.findActiveJobRows(user));
            return "dashboard/hr";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }
}
