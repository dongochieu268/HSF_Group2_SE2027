package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.service.JobPostingService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PipelineReportController {
    private final JobPostingService jobPostingService;

    public PipelineReportController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @GetMapping("/manage/reports")
    public String report(@RequestParam(required = false) Long jobId, Model model, HttpSession session) {
        try {
            SessionUser user = current(session);
            List<JobPosting> jobs = jobPostingService.findManagedJobs(user, null, null, "");
            model.addAttribute("jobs", jobs);

            // Spec yêu cầu default là "most recently updated"; entity chưa có updatedAt
            // nên dùng postedDate (null-safe) làm proxy — deviation ghi nhận trong report.
            JobPosting selected;
            if (jobId != null) {
                selected = jobPostingService.findManagedDetail(jobId, user);
            } else {
                selected = jobs.stream()
                        .max(Comparator.comparing(JobPosting::getPostedDate,
                                        Comparator.nullsFirst(Comparator.naturalOrder()))
                                .thenComparing(JobPosting::getId))
                        .orElse(null);
            }

            populate(model, selected, false);
            return "jobposting/report";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @GetMapping("/manage/jobs/{id}/report")
    public String reportForJob(@PathVariable Long id, Model model, HttpSession session) {
        try {
            JobPosting job = jobPostingService.findManagedDetail(id, current(session));
            populate(model, job, true);
            return "jobposting/report";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    private void populate(Model model, JobPosting selected, boolean fromJobDetail) {
        model.addAttribute("fromJobDetail", fromJobDetail);
        model.addAttribute("selectedJob", selected);
        if (selected == null) {
            return;
        }
        long applicationCount = jobPostingService.countApplications(selected.getId());
        model.addAttribute("applicationCount", applicationCount);
        model.addAttribute("pipelineStages", jobPostingService.buildPipelineSummary(selected.getId()));
        model.addAttribute("candidateRows", jobPostingService.buildCandidateReport(selected.getId()));
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
