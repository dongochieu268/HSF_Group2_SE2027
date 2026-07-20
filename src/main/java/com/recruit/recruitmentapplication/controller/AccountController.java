package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.ChangePasswordForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.dto.UpdateProfileForm;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.security.PasswordPolicy;
import com.recruit.recruitmentapplication.service.UserService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        model.addAttribute("user", userService.findById(current(session).getId()));
        return "auth/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(HttpSession session, Model model) {
        User user = userService.findById(current(session).getId());
        model.addAttribute("updateProfileForm", new UpdateProfileForm(user.getFullName(), user.getEmail()));
        return "auth/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("updateProfileForm") UpdateProfileForm form,
                                BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "auth/profile-edit";
        }
        try {
            User updated = userService.updateProfile(current(session).getId(), form);
            session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(updated));
            return "redirect:/profile?updated=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("profileError", ex.getMessage());
            return "auth/profile-edit";
        }
    }

    @GetMapping("/profile/password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/change-password";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult result, HttpSession session, Model model) {
        // Field bỏ trống: dừng ngay, các kiểm tra bên dưới cần giá trị khác null
        if (result.hasErrors()) {
            return "auth/change-password";
        }

        Long userId = current(session).getId();
        // SCR-04: mỗi lỗi hiển thị inline ngay dưới field gây lỗi, không gom vào banner chung
        if (!userService.matchesCurrentPassword(userId, form.getCurrentPassword())) {
            result.rejectValue("currentPassword", "password.current.invalid",
                    "Mật khẩu hiện tại không đúng");
        }
        for (String violation : PasswordPolicy.violations(form.getNewPassword())) {
            result.rejectValue("newPassword", "password.weak", violation);
        }
        if (userService.matchesCurrentPassword(userId, form.getNewPassword())) {
            result.rejectValue("newPassword", "password.unchanged",
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            result.rejectValue("confirmNewPassword", "password.mismatch",
                    "Mật khẩu xác nhận không khớp");
        }
        if (result.hasErrors()) {
            return "auth/change-password";
        }

        try {
            userService.changePassword(userId, form);
            return "redirect:/profile?passwordChanged=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("passwordError", ex.getMessage());
            return "auth/change-password";
        }
    }

    private SessionUser current(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }
}
