package com.recruit.recruitmentapplication.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Quy tắc độ mạnh mật khẩu theo spec SCR-03 / SCR-04:
 * "At least 8 characters, including 1 uppercase letter and 1 number".
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 100;
    public static final String HELPER_TEXT =
            "Tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ hoa và 1 chữ số";

    private PasswordPolicy() {
    }

    /**
     * Trả về danh sách các yêu cầu chưa đạt, rỗng nếu mật khẩu hợp lệ.
     * SCR-04 yêu cầu liệt kê từng yêu cầu không đạt thay vì một thông báo chung.
     */
    public static List<String> violations(String rawPassword) {
        List<String> violations = new ArrayList<>();
        if (rawPassword == null || rawPassword.isBlank()) {
            return violations;
        }
        if (rawPassword.length() < MIN_LENGTH) {
            violations.add("Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự");
        }
        if (rawPassword.length() > MAX_LENGTH) {
            violations.add("Mật khẩu không được vượt quá " + MAX_LENGTH + " ký tự");
        }
        if (rawPassword.chars().noneMatch(Character::isUpperCase)) {
            violations.add("Mật khẩu phải có ít nhất 1 chữ hoa");
        }
        if (rawPassword.chars().noneMatch(Character::isDigit)) {
            violations.add("Mật khẩu phải có ít nhất 1 chữ số");
        }
        return violations;
    }
}
