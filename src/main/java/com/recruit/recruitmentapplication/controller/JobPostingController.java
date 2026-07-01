package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.JobPostingForm;
import com.recruit.recruitmentapplication.entity.JobPosting;
import com.recruit.recruitmentapplication.repository.JobPostingRepository;
import com.recruit.recruitmentapplication.service.CompanyService;
import com.recruit.recruitmentapplication.service.JobPostingService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/jobs")
public class JobPostingController {
    private final JobPostingService jobPostingService;
    private final CompanyService companyService;
    private final JobPostingRepository jobPostingRepository;

    public JobPostingController(JobPostingService jobPostingService, CompanyService companyService, JobPostingRepository jobPostingRepository) {
        this.jobPostingService = jobPostingService;
        this.companyService = companyService;
        this.jobPostingRepository = jobPostingRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("jobs", jobPostingService.findOpenJobs(keyword));
        model.addAttribute("keyword", keyword);
        return "jobposting/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobPostingService.findById(id));
        return "jobposting/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareForm(model, new JobPostingForm(), false, null, null);
        return "jobposting/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return formView(model, form, result, false, null, null);
        try {
            JobPosting job = jobPostingService.create(form);
            // Document SCR-11: Luôn save as DRAFT khi tạo mới
            jobPostingRepository.updateStatus(job.getId(), JobPosting.PostingStatus.DRAFT);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting saved as Draft.");
            return "redirect:/jobs/" + job.getId();
        } catch (IllegalArgumentException exception) {
            result.reject("jobPostingForm.error", exception.getMessage());
            return formView(model, form, result, false, null, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);
        prepareForm(model, JobPostingForm.from(job), true, id, job.getStatus().name());
        return "jobposting/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("jobPostingForm") JobPostingForm form,
                         BindingResult result, Model model,
                         @RequestParam(value = "action", required = false) String action,
                         RedirectAttributes redirectAttributes) {

        JobPosting existing = jobPostingService.findById(id);
        String currentStatus = existing.getStatus().name();

        // Ngăn chặn update nếu đã CLOSED
        if ("CLOSED".equals(currentStatus)) {
            return "redirect:/jobs/" + id;
        }

        if (result.hasErrors()) return formView(model, form, result, true, id, currentStatus);

        try {
            jobPostingService.update(id, form);
            // Xử lý nút Publish
            if ("publish".equals(action) && "DRAFT".equals(currentStatus)) {
                jobPostingRepository.updateStatus(id, JobPosting.PostingStatus.OPEN);
                redirectAttributes.addFlashAttribute("successMessage", "Job posting published successfully.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Changes saved.");
            }
            return "redirect:/jobs/" + id;
        } catch (IllegalArgumentException exception) {
            result.reject("jobPostingForm.error", exception.getMessage());
            return formView(model, form, result, true, id, currentStatus);
        }
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id) {
        jobPostingService.close(id);
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        jobPostingService.delete(id);
        return "redirect:/jobs";
    }

    private String formView(Model model, JobPostingForm form, BindingResult result, boolean edit, Long id, String jobStatus) {
        prepareForm(model, form, edit, id, jobStatus);
        return "jobposting/form";
    }

    private void prepareForm(Model model, JobPostingForm form, boolean edit, Long id, String jobStatus) {
        model.addAttribute("jobPostingForm", form);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("jobTypes", JobPosting.JobType.values());
        model.addAttribute("skills", jobPostingService.findAllSkills());
        model.addAttribute("editMode", edit);
        model.addAttribute("jobId", id);
        model.addAttribute("jobStatus", jobStatus);
    }
}