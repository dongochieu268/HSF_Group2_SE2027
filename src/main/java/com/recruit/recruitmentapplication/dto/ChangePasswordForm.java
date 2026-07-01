package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordForm {

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    private String currentPassword;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, message = "At least 8 characters required")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).*$", message = "Must include at least 1 uppercase letter and 1 number")
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmPassword;

    // Getters and Setters
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}