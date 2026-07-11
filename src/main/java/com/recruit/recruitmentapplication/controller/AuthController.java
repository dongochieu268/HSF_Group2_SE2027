package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.LoginForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.ActivityLog.ActivityEventType;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.service.ActivityLogService;
import com.recruit.recruitmentapplication.service.UserService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public AuthController(UserService userService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerForm") RegisterForm form,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        if (form.getConfirmPassword() == null || form.getConfirmPassword().isBlank()) {
            result.rejectValue("confirmPassword", "confirmPassword.required", "Vui lòng xác nhận mật khẩu");
            return "auth/register";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "confirmPassword.mismatch", "Mật khẩu xác nhận không khớp");
            return "auth/register";
        }
        try {
            User user = userService.register(form);
            activityLogService.log(ActivityEventType.ACCOUNT_CREATED, user, "Tài khoản được tạo qua đăng ký", null);
            return "redirect:/auth/login?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginForm") LoginForm form,
                               BindingResult result, HttpSession session, HttpServletRequest request, Model model) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        User user = userService.authenticate(form.getUsername(), form.getPassword()).orElse(null);
        if (user == null) {
            activityLogService.log(ActivityEventType.SIGN_IN_FAILURE, null,
                    "Đăng nhập thất bại cho tài khoản '" + form.getUsername() + "'", request.getRemoteAddr());
            model.addAttribute("loginError", "Sai tài khoản, mật khẩu hoặc tài khoản đã bị khóa");
            return "auth/login";
        }

        session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(user));
        activityLogService.log(ActivityEventType.SIGN_IN_SUCCESS, user, "Đăng nhập thành công", request.getRemoteAddr());
        if (Role.ADMIN.equals(user.getRole().getName())) {
            return "redirect:/admin/dashboard";
        }
        if (Role.CANDIDATE.equals(user.getRole().getName())) {
            return "redirect:/my-applications";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout=true";
    }
}
