package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.ChangePasswordForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // Màn hình SCR-04: User Profile (Tạm thời là placeholder để Hủy/Thành công có chỗ quay về)
    @GetMapping
    public String profile() {
        return "user/profile"; // Bạn cần tạo thêm view này cho SCR-04 sau
    }

    // Màn hình SCR-05: Hiển thị form đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordForm", new ChangePasswordForm());
        return "user/change-password";
    }

    // Màn hình SCR-05: Xử lý submit
    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                        BindingResult result,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {

        // Validation: Passwords do not match
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.passwordForm", "Passwords do not match.");
        }

        if (result.hasErrors()) {
            return "user/change-password";
        }

        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);

        try {
            userService.changePassword(sessionUser.getId(), form.getCurrentPassword(), form.getNewPassword());

            // Redirect về SCR-04 kèm flash message thành công
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
            return "redirect:/profile";

        } catch (IllegalArgumentException e) {
            // Xử lý mapping lỗi từ Service ra Form UI
            if (e.getMessage().equals("Incorrect current password.")) {
                result.rejectValue("currentPassword", "error.passwordForm", e.getMessage());
            } else {
                result.rejectValue("newPassword", "error.passwordForm", e.getMessage());
            }
            return "user/change-password";
        }
    }
}