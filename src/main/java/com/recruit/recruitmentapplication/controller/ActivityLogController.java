package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.service.ActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/activity-log")
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activityLogs", activityLogService.findAll());
        return "dashboard/activity-log";
    }
}
