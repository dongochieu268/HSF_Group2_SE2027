package com.recruit.recruitmentapplication.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordForm {
    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    private String currentPassword;

    // Độ mạnh do PasswordPolicy kiểm tra để liệt kê riêng từng yêu cầu chưa đạt {SCR-04}
    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmNewPassword;

    public ChangePasswordForm() {
    }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
