package com.recruit.recruitmentapplication.service;


import com.recruit.recruitmentapplication.dto.ChangePasswordForm;
import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.dto.UpdateProfileForm;
import com.recruit.recruitmentapplication.dto.UserAccountForm;
import com.recruit.recruitmentapplication.entity.Candidate;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.entity.User.AccountStatus;
import com.recruit.recruitmentapplication.repository.CandidateRepository;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Set<String> ADMIN_CREATABLE_ROLES = Set.of(Role.HR_MANAGER, Role.INTERVIEWER);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordUtil passwordUtil;
    private final CandidateRepository candidateRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordUtil passwordUtil,
                       CandidateRepository candidateRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordUtil = passwordUtil;
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public User register(RegisterForm form) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role candidateRole = roleRepository.findByName(Role.CANDIDATE)
                .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo vai trò CANDIDATE"));
        Candidate existingCandidate = candidateRepository.findByEmail(email).orElse(null);
        if (existingCandidate != null && existingCandidate.getUser() != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User user = new User(
                username,
                passwordUtil.hash(form.getPassword()),
                email,
                form.getFullName().trim(),
                candidateRole
        );
        user.setAccountStatus(AccountStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        if (existingCandidate != null) {
            existingCandidate.setUser(savedUser);
            existingCandidate.setName(savedUser.getFullName());
            candidateRepository.save(existingCandidate);
        }
        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByUsernameWithRole(username.trim())
                .filter(User::isEnabled)
                .filter(user -> passwordUtil.matches(rawPassword, user.getPassword()));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllWithRole();
    }

    @Transactional(readOnly = true)
    public List<User> findUsers(String keyword, String roleName, String statusName) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String normalizedRole = roleName == null ? "" : roleName.trim();
        AccountStatus status = parseStatusOrNull(statusName);

        return userRepository.findAllWithRole().stream()
                .filter(user -> normalizedKeyword.isEmpty()
                        || user.getFullName().toLowerCase().contains(normalizedKeyword)
                        || user.getEmail().toLowerCase().contains(normalizedKeyword))
                .filter(user -> normalizedRole.isEmpty() || user.getRole().getName().equals(normalizedRole))
                .filter(user -> status == null || user.getAccountStatus() == status)
                .toList();
    }

    @Transactional
    public User createManagedAccount(UserAccountForm form) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();
        String roleName = form.getRoleName().trim();

        if (!ADMIN_CREATABLE_ROLES.contains(roleName)) {
            throw new IllegalArgumentException("Admin can create only HR Manager or Interviewer accounts.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("This username is already taken. Please choose another.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email address is already registered.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role is not available."));
        User user = new User(
                username,
                passwordUtil.hash(form.getInitialPassword()),
                email,
                form.getFullName().trim(),
                role
        );
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> findAdminCreatableRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> ADMIN_CREATABLE_ROLES.contains(role.getName()))
                .toList();
    }

    @Transactional
    public User updateRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ"));
        user.setRole(role);
        return userRepository.save(user);
    }



    @Transactional
    public User updateProfile(Long userId, UpdateProfileForm form) {
        User user = findById(userId);
        String newEmail = form.getEmail().trim().toLowerCase();

        if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
        }

        user.setFullName(form.getFullName().trim());
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    @Transactional
    public User changePassword(Long userId, ChangePasswordForm form) {
        User user = findById(userId);

        if (!passwordUtil.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }
        if (passwordUtil.matches(form.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordUtil.hash(form.getNewPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User toggleEnabled(Long userId) {
        User user = findById(userId);
        user.setAccountStatus(user.isEnabled() ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public User deactivate(Long userId) {
        User user = findById(userId);
        if (Role.ADMIN.equals(user.getRole().getName()) && activeAdminCount() <= 1) {
            throw new IllegalArgumentException("The only active Admin account cannot be deactivated.");
        }
        user.setAccountStatus(AccountStatus.INACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public User unlock(Long userId) {
        User user = findById(userId);
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User lockAccount(Long userId) {
        User user = findById(userId);
        user.setAccountStatus(AccountStatus.LOCKED);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    public boolean canDeactivate(User user) {
        if (user == null || user.getAccountStatus() == AccountStatus.INACTIVE) {
            return false;
        }
        return !(Role.ADMIN.equals(user.getRole().getName()) && activeAdminCount() <= 1);
    }

    private long activeAdminCount() {
        return userRepository.findByRole_Name(Role.ADMIN).stream()
                .filter(User::isEnabled)
                .count();
    }

    private AccountStatus parseStatusOrNull(String statusName) {
        if (statusName == null || statusName.trim().isEmpty()) {
            return null;
        }
        try {
            return AccountStatus.valueOf(statusName.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
