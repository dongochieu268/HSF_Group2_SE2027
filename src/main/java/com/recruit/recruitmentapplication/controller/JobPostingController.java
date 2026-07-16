package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.service.CandidateService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobs")
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final ApplicationService applicationService;
    private final CandidateService candidateService;

    public JobPostingController(JobPostingService jobPostingService, ApplicationService applicationService,
                                CandidateService candidateService) {
        this.jobPostingService = jobPostingService;
        this.applicationService = applicationService;
        this.candidateService = candidateService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("jobs", jobPostingService.findOpenJobs(keyword));
        model.addAttribute("keyword", keyword);
        return "jobposting/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        JobPosting job = jobPostingService.findPublicDetail(id);
        model.addAttribute("job", job);

        SessionUser user = current(session);
        boolean isCandidate = user != null && Role.CANDIDATE.equals(user.getRoleName());
        model.addAttribute("isCandidate", isCandidate);
        if (isCandidate) {
            Long candidateId = candidateService.getOrCreateProfileForUser(user.getId()).getId();
            model.addAttribute("hasApplied", applicationService.hasApplied(candidateId, id));
        } else {
            model.addAttribute("hasApplied", false);
        }
        return "jobposting/detail";
    }

    // "Apply Now": hiển thị cho ứng viên chưa nộp đơn cho công việc này {S3}
    @GetMapping("/{id}/apply")
    public String showApplyForm(@PathVariable Long id, Model model, HttpSession session) {
        SessionUser user = current(session);
        if (user == null || !Role.CANDIDATE.equals(user.getRoleName())) {
            return "redirect:/error/403";
        }
        JobPosting job = jobPostingService.findPublicDetail(id);
        Long candidateId = candidateService.getOrCreateProfileForUser(user.getId()).getId();
        if (applicationService.hasApplied(candidateId, id)) {
            return "redirect:/jobs/" + id;
        }
        model.addAttribute("job", job);
        return "application/apply-form";
    }

    @PostMapping("/{id}/apply")
    public String submitApplication(@PathVariable Long id, @RequestParam(required = false) String coverLetter,
                                    Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        SessionUser user = current(session);
        if (user == null || !Role.CANDIDATE.equals(user.getRoleName())) {
            return "redirect:/error/403";
        }
        Long candidateId = candidateService.getOrCreateProfileForUser(user.getId()).getId();
        try {
            applicationService.apply(id, candidateId, coverLetter);
            redirectAttributes.addFlashAttribute("successMessage", "Nộp đơn ứng tuyển thành công!");
            return "redirect:/my-applications";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("job", jobPostingService.findPublicDetail(id));
            model.addAttribute("coverLetter", coverLetter);
            model.addAttribute("errorMessage", exception.getMessage());
            return "application/apply-form";
        }
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
