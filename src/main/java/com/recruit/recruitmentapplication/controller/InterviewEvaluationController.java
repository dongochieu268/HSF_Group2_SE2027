package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Interview;
import com.recruit.recruitmentapplication.service.EvaluationService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * SCR-19: form đánh giá được gộp inline vào SCR-17 (application/detail.html) -
 * route ở đây chỉ còn "My Interviews" (danh sách) và endpoint POST nộp đánh giá,
 * không còn trang GET riêng để hiển thị form.
 */
@Controller
@RequestMapping("/interviews")
public class InterviewEvaluationController {

    private final EvaluationService evaluationService;

    public InterviewEvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public String myInterviews(Model model, HttpSession session) {
        SessionUser user = current(session);
        model.addAttribute("interviews", evaluationService.findMyInterviews(user.getId()));
        return "interview/my-interviews";
    }

    // Nộp xong (hoặc lỗi) đều quay về SCR-17 (Application Detail), không còn trang riêng
    @PostMapping("/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
                                   @RequestParam Integer rating,
                                   @RequestParam String feedback,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        SessionUser user = current(session);
        try {
            Interview interview = evaluationService.submitEvaluation(id, user, rating, feedback);
            redirectAttributes.addFlashAttribute("successMessage", "Evaluation submitted. Thank you.");
            return "redirect:/manage/applications/" + interview.getApplication().getId();
        } catch (IllegalArgumentException exception) {
            try {
                Interview interview = evaluationService.findMyInterviewOrThrow(id, user);
                redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                return "redirect:/manage/applications/" + interview.getApplication().getId();
            } catch (IllegalArgumentException accessDenied) {
                return "redirect:/error/403";
            }
        }
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
