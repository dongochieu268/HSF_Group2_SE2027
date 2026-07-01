package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.PasswordResetConfirmForm;
import com.recruit.recruitmentapplication.dto.PasswordResetRequestForm;
import com.recruit.recruitmentapplication.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/auth/password-reset")
public class PasswordResetController {
    private static final String REQUEST_VIEW = "auth/password-reset-request";
    private static final String CONFIRM_VIEW = "auth/password-reset-confirm";
    private static final String GENERIC_SUCCESS =
            "If an account with this email exists, a reset link has been sent.";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/request")
    public String showRequestForm(Model model) {
        model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        return REQUEST_VIEW;
    }

    @PostMapping("/request")
    public String processRequest(@Valid @ModelAttribute("passwordResetRequestForm") PasswordResetRequestForm form,
                                 BindingResult result,
                                 Model model) {
        if (!result.hasErrors()) {
            passwordResetService.requestReset(form.getEmail(), currentBaseUrl());
            model.addAttribute("resetRequestSuccess", GENERIC_SUCCESS);
        }
        return REQUEST_VIEW;
    }

    @GetMapping("/confirm")
    public String showConfirmForm(@RequestParam(value = "token", required = false) String token, Model model) {
        PasswordResetConfirmForm form = new PasswordResetConfirmForm();
        form.setToken(token);
        model.addAttribute("passwordResetConfirmForm", form);
        model.addAttribute("tokenUsable", passwordResetService.isTokenUsable(token));
        return CONFIRM_VIEW;
    }

    @PostMapping("/confirm")
    public String processConfirm(@Valid @ModelAttribute("passwordResetConfirmForm") PasswordResetConfirmForm form,
                                 BindingResult result,
                                 Model model) {
        if (!passwordResetService.isTokenUsable(form.getToken())) {
            model.addAttribute("tokenUsable", false);
            return CONFIRM_VIEW;
        }
        if (result.hasErrors()) {
            model.addAttribute("tokenUsable", true);
            return CONFIRM_VIEW;
        }

        try {
            passwordResetService.resetPassword(form.getToken(), form.getNewPassword(), form.getConfirmPassword());
            return "redirect:/auth/login?reset=true";
        } catch (IllegalArgumentException ex) {
            if (passwordResetService.isTokenUsable(form.getToken())) {
                result.rejectValue("confirmPassword", "passwordReset.confirmPassword", ex.getMessage());
                model.addAttribute("tokenUsable", true);
            } else {
                model.addAttribute("tokenUsable", false);
            }
            return CONFIRM_VIEW;
        }
    }

    private String currentBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
