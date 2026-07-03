package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.service.CompanyService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/manage/jobs")
public class ManagedJobPostingController {
    private final JobPostingService jobPostingService;
    private final CompanyService companyService;

    public ManagedJobPostingController(JobPostingService jobPostingService, CompanyService companyService) {
        this.jobPostingService = jobPostingService;
        this.companyService = companyService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model, HttpSession session) {
        try {
            model.addAttribute("jobs", jobPostingService.findManagedJobs(current(session), status));
            model.addAttribute("selectedStatus", status);
            return "jobposting/manage-list";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareForm(model, new JobPostingForm(), false, null);
        return "jobposting/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result, Model model, HttpSession session) {
        if (result.hasErrors()) return formView(model, form, false, null);
        try {
            return "redirect:/manage/jobs/" + jobPostingService.create(form, current(session)).getId();
        } catch (IllegalArgumentException exception) {
            result.reject("jobPostingForm.error", exception.getMessage());
            return formView(model, form, false, null);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            JobPosting job = jobPostingService.findManagedDetail(id, current(session));
            long applicationCount = jobPostingService.countApplications(id);
            model.addAttribute("job", job);
            model.addAttribute("applicationCount", applicationCount);
            model.addAttribute("pipelineStages", jobPostingService.buildPipelineSummary(id));
            model.addAttribute("canEdit", job.getStatus() == JobPosting.PostingStatus.DRAFT
                    || job.getStatus() == JobPosting.PostingStatus.ACTIVE);
            model.addAttribute("canPublish", job.getStatus() == JobPosting.PostingStatus.DRAFT);
            model.addAttribute("canClose", job.getStatus() == JobPosting.PostingStatus.ACTIVE);
            model.addAttribute("canDelete", job.getStatus() == JobPosting.PostingStatus.DRAFT && applicationCount == 0);
            return "jobposting/manage-detail";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            JobPosting job = jobPostingService.findManagedDetail(id, current(session));
            prepareForm(model, JobPostingForm.from(job), true, id);
            model.addAttribute("jobStatus", job.getStatus());
            return "jobposting/form";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result, Model model, HttpSession session) {
        if (result.hasErrors()) return formView(model, form, true, id);
        try {
            jobPostingService.update(id, form, current(session));
            return "redirect:/manage/jobs/" + id;
        } catch (IllegalArgumentException exception) {
            result.reject("jobPostingForm.error", exception.getMessage());
            return formView(model, form, true, id);
        }
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, HttpSession session) {
        try {
            jobPostingService.publish(id, current(session));
            return "redirect:/manage/jobs/" + id;
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, HttpSession session) {
        try {
            jobPostingService.close(id, current(session));
            return "redirect:/manage/jobs/" + id;
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        try {
            jobPostingService.deleteManaged(id, current(session));
            return "redirect:/manage/jobs";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    private String formView(Model model, JobPostingForm form, boolean edit, Long id) {
        prepareForm(model, form, edit, id);
        return "jobposting/form";
    }

    private void prepareForm(Model model, JobPostingForm form, boolean edit, Long id) {
        model.addAttribute("jobPostingForm", form);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("jobTypes", JobPosting.JobType.values());
        model.addAttribute("skills", jobPostingService.findAllSkills());
        model.addAttribute("editMode", edit);
        model.addAttribute("jobId", id);
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
