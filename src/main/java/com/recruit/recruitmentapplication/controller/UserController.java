package com.recruit.recruitmentapplication.controller;

import com.recruit.recruitmentapplication.dto.UserAccountForm;
import com.recruit.recruitmentapplication.dto.UserRoleForm;
import com.recruit.recruitmentapplication.entity.User.AccountStatus;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "") String keyword,
                            @RequestParam(defaultValue = "") String role,
                            @RequestParam(defaultValue = "") String status,
                            Model model) {
        prepareListModel(model, keyword, role, status, new UserAccountForm());
        return "user/list";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("userAccountForm") UserAccountForm form,
                             BindingResult result,
                             @RequestParam(defaultValue = "") String keyword,
                             @RequestParam(defaultValue = "") String role,
                             @RequestParam(defaultValue = "") String status,
                             Model model) {
        if (result.hasErrors()) {
            prepareListModel(model, keyword, role, status, form);
            return "user/list";
        }
        try {
            userService.createManagedAccount(form);
            return "redirect:/admin/users?created=true";
        } catch (IllegalArgumentException exception) {
            result.reject("userAccountForm.error", exception.getMessage());
            prepareListModel(model, keyword, role, status, form);
            return "user/list";
        }
    }

    @GetMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id) {
        try {
            userService.deactivate(id);
            return "redirect:/admin/users?deactivated=true";
        } catch (IllegalArgumentException exception) {
            return "redirect:/admin/users?error=last-admin";
        }
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Long id) {
        userService.unlock(id);
        return "redirect:/admin/users?unlocked=true";
    }

    @GetMapping("/{id}/edit-role")
    public String showEditRole(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("userRoleForm", new UserRoleForm(user.getRole().getName()));
        model.addAttribute("roles", userService.findAllRoles());
        return "user/form";
    }

    @PostMapping("/{id}/edit-role")
    public String updateRole(@PathVariable Long id,
                             @Valid @ModelAttribute("userRoleForm") UserRoleForm form,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.findById(id));
            model.addAttribute("roles", userService.findAllRoles());
            return "user/form";
        }
        userService.updateRole(id, form.getRoleName());
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    private void prepareListModel(Model model, String keyword, String role, String status, UserAccountForm form) {
        List<User> users = userService.findUsers(keyword, role, status);
        Map<Long, Boolean> canDeactivateById = users.stream()
                .collect(Collectors.toMap(User::getId, userService::canDeactivate));
        model.addAttribute("users", users);
        model.addAttribute("canDeactivateById", canDeactivateById);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("roles", userService.findAllRoles());
        model.addAttribute("creatableRoles", userService.findAdminCreatableRoles());
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("userAccountForm", form);
    }
}
