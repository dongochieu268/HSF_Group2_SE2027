package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SCR-17: Application Detail.
 * Reached from SCR-16 (Application List) via its "Open" button; links out to
 * interview scheduling for the "SCR-18 interviewer sidebar" section.
 */
@Controller
@RequestMapping("/manage/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        try {
            SessionUser user = current(session);
            Application application = applicationService.findManagedDetail(id, user);
            model.addAttribute("application", application);
            model.addAttribute("displayStatus", ApplicationService.displayStatus(application.getStatus()));
            model.addAttribute("statuses", Application.ApplicationStatus.values());
            model.addAttribute("notes", applicationService.listNotes(id));
            model.addAttribute("statusHistory", applicationService.listStatusHistory(id));
            model.addAttribute("documents", applicationService.listDocuments(id));
            model.addAttribute("interviewTypes", Interview.InterviewType.values());
            model.addAttribute("canManage", Role.ADMIN.equals(user.getRoleName())
                    || Role.HR_MANAGER.equals(user.getRoleName()));
            return "application/detail";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @PostMapping("/{id}/notes")
    public String addNote(@PathVariable Long id, @RequestParam String content, HttpSession session) {
        try {
            applicationService.addNote(id, current(session), content);
        } catch (IllegalArgumentException ignored) {
            // Nội dung rỗng hoặc không có quyền: quay lại trang chi tiết, không thêm ghi chú.
        }
        return "redirect:/manage/applications/" + id;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam String status,
                               @RequestParam(required = false) String note, HttpSession session) {
        try {
            Application.ApplicationStatus newStatus = Application.ApplicationStatus.valueOf(status);
            applicationService.changeStatus(id, current(session), newStatus, note);
        } catch (IllegalArgumentException ignored) {
            // Trạng thái không hợp lệ hoặc không có quyền: bỏ qua, quay lại trang chi tiết.
        }
        return "redirect:/manage/applications/" + id;
    }

    @PostMapping("/{id}/interviews")
    public String scheduleInterview(@PathVariable Long id,
                                    @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                                            pattern = "yyyy-MM-dd'T'HH:mm")
                                    LocalDateTime scheduledAt,
                                    @RequestParam Interview.InterviewType interviewType,
                                    @RequestParam(required = false) String interviewerName,
                                    HttpSession session) {
        try {
            applicationService.scheduleInterview(id, current(session), scheduledAt, interviewType, interviewerName);
        } catch (IllegalArgumentException ignored) {
            // Dữ liệu không hợp lệ hoặc không có quyền: bỏ qua, quay lại trang chi tiết.
        }
        return "redirect:/manage/applications/" + id;
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
