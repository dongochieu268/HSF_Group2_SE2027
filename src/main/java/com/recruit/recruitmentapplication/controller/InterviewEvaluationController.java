package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Evaluation;
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

    @GetMapping("/{id}/evaluate")
    public String evaluateForm(@PathVariable Long id, Model model, HttpSession session) {
        try {
            SessionUser user = current(session);
            Interview interview = evaluationService.findMyInterviewOrThrow(id, user);
            Evaluation existing = evaluationService.findExistingEvaluation(id);
            model.addAttribute("interview", interview);
            model.addAttribute("existingEvaluation", existing);
            return "interview/record-result-form";
        } catch (IllegalArgumentException exception) {
            return "redirect:/error/403";
        }
    }

    @PostMapping("/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
                                   @RequestParam Integer rating,
                                   @RequestParam String feedback,
                                   Model model, HttpSession session) {
        SessionUser user = current(session);
        try {
            evaluationService.submitEvaluation(id, user, rating, feedback);
            return "redirect:/interviews";
        } catch (IllegalArgumentException exception) {
            try {
                Interview interview = evaluationService.findMyInterviewOrThrow(id, user);
                model.addAttribute("interview", interview);
                model.addAttribute("existingEvaluation", evaluationService.findExistingEvaluation(id));
                model.addAttribute("errorMessage", exception.getMessage());
                return "interview/record-result-form";
            } catch (IllegalArgumentException accessDenied) {
                return "redirect:/error/403";
            }
        }
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
