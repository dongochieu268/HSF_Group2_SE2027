package com.recruit.recruitmentapplication.service;

import com.recruit.recruitmentapplication.entity.PasswordResetToken;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.PasswordResetTokenRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_LIFETIME_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordUtil passwordUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordUtil = passwordUtil;
    }

    @Transactional
    public void requestReset(String email, String baseUrl) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) {
            return;
        }

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        if (!user.isEnabled()) {
            return;
        }

        PasswordResetToken resetToken = new PasswordResetToken(
                generateUniqueToken(),
                user,
                LocalDateTime.now().plusMinutes(TOKEN_LIFETIME_MINUTES)
        );
        tokenRepository.save(resetToken);
        log.info("Password reset URL for {}: {}", user.getEmail(),
                buildResetUrl(baseUrl, resetToken.getToken()));
    }

    @Transactional(readOnly = true)
    public boolean isTokenUsable(String token) {
        String normalizedToken = normalizeToken(token);
        if (normalizedToken.isEmpty()) {
            return false;
        }
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(normalizedToken);
        if (resetToken.isEmpty()) {
            return false;
        }
        return resetToken.get().isUsable(LocalDateTime.now());
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        validatePassword(newPassword, confirmPassword);

        Optional<PasswordResetToken> resetTokenOptional = tokenRepository.findByToken(normalizeToken(token));
        if (resetTokenOptional.isEmpty() || !resetTokenOptional.get().isUsable(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link is expired or already used.");
        }

        PasswordResetToken resetToken = resetTokenOptional.get();

        User user = resetToken.getUser();
        user.setPassword(passwordUtil.hash(newPassword));
        resetToken.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        tokenRepository.save(resetToken);
    }

    private String generateUniqueToken() {
        String token;
        do {
            byte[] bytes = new byte[TOKEN_BYTES];
            secureRandom.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (tokenRepository.findByToken(token).isPresent());
        return token;
    }

    private String buildResetUrl(String baseUrl, String token) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/auth/password-reset/confirm")
                .queryParam("token", token)
                .toUriString();
    }

    private void validatePassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6 || newPassword.length() > 100) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeToken(String token) {
        return token == null ? "" : token.trim();
    }
}
