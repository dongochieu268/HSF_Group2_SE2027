package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Application;
import com.recruit.recruitmentapplication.entity.ApplicationDocument;
import com.recruit.recruitmentapplication.entity.Evaluation;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.service.ApplicationService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SCR-17: Application Detail.
 * Reached from SCR-16 (Application List) via its "Open" button; links out to
 * interview scheduling for the "SCR-18 interviewer sidebar" section.
 * HR Manager (own jobs) / Admin (all) get full pipeline management actions;
 * Interviewer gets a read-only view limited to applications they are assigned to.
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
            Application application = applicationService.findDetailForViewer(id, user);
            boolean canManage = Role.ADMIN.equals(user.getRoleName()) || Role.HR_MANAGER.equals(user.getRoleName());
            boolean isInterviewer = Role.INTERVIEWER.equals(user.getRoleName());

            model.addAttribute("app", application);
            model.addAttribute("displayStatus", ApplicationService.displayStatus(application.getStatus()));
            model.addAttribute("statuses", Application.ApplicationStatus.values());
            model.addAttribute("notes", applicationService.listNotes(id));
            model.addAttribute("statusHistory", applicationService.listStatusHistory(id));
            model.addAttribute("documents", applicationService.listDocuments(id));
            model.addAttribute("interviewTypes", Interview.InterviewType.values());
            model.addAttribute("interviewerAccounts", applicationService.listInterviewerAccounts());
            model.addAttribute("canManage", canManage);
            model.addAttribute("isInterviewer", isInterviewer);

            // SCR-17: pipeline stage controls context-sensitive (Move to X / Reject)
            model.addAttribute("isTerminalStatus", ApplicationService.isTerminal(application.getStatus()));
            model.addAttribute("nextStageStatus",
                    ApplicationService.nextStageStatus(application.getStatus()));
            model.addAttribute("nextStageLabel",
                    ApplicationService.nextStageLabel(application.getStatus()));

            // SCR-17: "Evaluation summary" (HR/Admin) - danh sách đánh giá đã nộp
            List<Evaluation> evaluations = applicationService.listEvaluations(id);
            model.addAttribute("evaluations", evaluations);

            if (isInterviewer) {
                Set<Long> evaluatedInterviewIds = evaluations.stream()
                        .map(e -> e.getInterview().getId())
                        .collect(Collectors.toSet());
                List<Interview> myInterviews = application.getInterviews().stream()
                        .filter(iv -> iv.getInterviewer() != null && iv.getInterviewer().getId().equals(user.getId()))
                        .toList();
                model.addAttribute("myInterviews", myInterviews);
                model.addAttribute("evaluatedInterviewIds", evaluatedInterviewIds);
            }

            return "application/detail";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    // SCR-17: "Download CV" - quyền được kiểm tra server-side trước khi trả byte nào
    @GetMapping("/{id}/documents/{documentId}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, @PathVariable Long documentId,
                                                     HttpSession session) {
        try {
            ApplicationDocument document = applicationService.findDocumentForDownload(id, documentId, current(session));
            Path path = applicationService.resolveStoragePath(document.getStoragePath());
            Resource resource = new UrlResource(path.toUri());
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(document.getContentType());
            } catch (RuntimeException ex) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (MalformedURLException exception) {
            return ResponseEntity.notFound().build();
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
                                    @RequestParam(required = false) Long interviewerId,
                                    HttpSession session) {
        try {
            applicationService.scheduleInterview(id, current(session), scheduledAt, interviewType, interviewerId);
        } catch (IllegalArgumentException ignored) {
            // Dữ liệu không hợp lệ hoặc không có quyền: bỏ qua, quay lại trang chi tiết.
        }
        return "redirect:/manage/applications/" + id;
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
