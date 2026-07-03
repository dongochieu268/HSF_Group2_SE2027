package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.RegisterForm;
import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.repository.RoleRepository;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.security.PasswordUtil;
import com.recruit.recruitmentapplication.service.UserService;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PhaseTwoWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private PasswordUtil passwordUtil;

    @Test
    void rolesAndHashedAdminAreSeeded() {
        assertTrue(roleRepository.findByName(Role.ADMIN).isPresent());
        assertTrue(roleRepository.findByName(Role.HR_MANAGER).isPresent());
        assertTrue(roleRepository.findByName(Role.CANDIDATE).isPresent());

        User admin = userRepository.findByUsernameWithRole("admin").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole().getName());
        assertFalse("Admin@123".equals(admin.getPassword()));
        assertTrue(passwordUtil.matches("Admin@123", admin.getPassword()));
    }

    @Test
    @Transactional
    void registrationHashesPasswordAndAssignsCandidateRole() {
        RegisterForm form = new RegisterForm();
        form.setUsername("phase2candidate");
        form.setPassword("secret123");
        form.setEmail("phase2candidate@example.com");
        form.setFullName("Phase Two Candidate");

        User user = userService.register(form);

        assertEquals(Role.CANDIDATE, user.getRole().getName());
        assertTrue(user.isEnabled());
        assertFalse("secret123".equals(user.getPassword()));
        assertTrue(passwordUtil.matches("secret123", user.getPassword()));
    }

    @Test
    @Transactional
    void administratorCanChangeRoleAndToggleAccountStatus() {
        RegisterForm form = new RegisterForm();
        form.setUsername("managedcandidate");
        form.setPassword("secret123");
        form.setEmail("managedcandidate@example.com");
        form.setFullName("Managed Candidate");
        User user = userService.register(form);

        userService.updateRole(user.getId(), Role.HR_MANAGER);
        assertEquals(Role.HR_MANAGER, userService.findById(user.getId()).getRole().getName());

        userService.toggleEnabled(user.getId());
        assertFalse(userService.findById(user.getId()).isEnabled());

        userService.toggleEnabled(user.getId());
        assertTrue(userService.findById(user.getId()).isEnabled());
    }

    @Test
    void authPagesArePublicAndAdminPageRequiresLogin() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/auth/login\"")));
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/auth/register\"")));
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void authenticatedUserCanOpenChangePasswordPageWithPasswordToggles() throws Exception {
        User admin = userRepository.findByUsernameWithRole("admin").orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(admin));

        mockMvc.perform(get("/profile/password").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-password"))
                .andExpect(content().string(containsString("name=\"currentPassword\"")))
                .andExpect(content().string(containsString("name=\"newPassword\"")))
                .andExpect(content().string(containsString("name=\"confirmNewPassword\"")))
                .andExpect(content().string(containsString("data-password-toggle=\"currentPassword\"")))
                .andExpect(content().string(containsString("data-password-toggle=\"newPassword\"")))
                .andExpect(content().string(containsString("data-password-toggle=\"confirmNewPassword\"")));
    }

    @Test
    void publicPasswordEntryPagesExposeShowHideControls() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-password-toggle=\"password\"")));

        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-password-toggle=\"password\"")))
                .andExpect(content().string(containsString("data-password-toggle=\"confirmPassword\"")));
    }

    @Test
    void adminCanLoginAndOpenUserManagement() throws Exception {
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .param("username", "admin")
                        .param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(request().sessionAttribute(SessionConstants.LOGGED_IN_USER,
                        org.hamcrest.Matchers.instanceOf(SessionUser.class)))
                .andReturn();

        mockMvc.perform(get("/admin/users").session((MockHttpSession) login.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin@recruit.com")));
    }

    @Test
    void adminUserManagementShowsScr08FiltersCreateFormAndPdfActions() throws Exception {
        mockMvc.perform(get("/admin/users").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("user/list"))
                .andExpect(content().string(containsString("name=\"keyword\"")))
                .andExpect(content().string(containsString("name=\"role\"")))
                .andExpect(content().string(containsString("name=\"status\"")))
                .andExpect(content().string(containsString("value=\"HR_MANAGER\"")))
                .andExpect(content().string(containsString("value=\"INTERVIEWER\"")))
                .andExpect(content().string(containsString("value=\"LOCKED\"")))
                .andExpect(content().string(containsString("value=\"INACTIVE\"")))
                .andExpect(content().string(containsString("Create Account")))
                .andExpect(content().string(containsString("data-dialog-open=\"createAccountDialog\"")))
                .andExpect(content().string(containsString("<dialog class=\"modal-dialog create-account-dialog\" id=\"createAccountDialog\"")))
                .andExpect(content().string(containsString("/js/admin-users.js")))
                .andExpect(content().string(not(containsString("data-open-on-load=\"true\""))))
                .andExpect(content().string(containsString("Deactivate")))
                .andExpect(content().string(containsString("Reactivate")))
                .andExpect(content().string(containsString("Not available in this version")));
    }

    @Test
    void createAccountPopupReopensWhenValidationFails() throws Exception {
        mockMvc.perform(post("/admin/users").session(adminSession())
                        .param("fullName", "")
                        .param("username", "")
                        .param("email", "")
                        .param("roleName", "")
                        .param("initialPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/list"))
                .andExpect(content().string(containsString("<dialog class=\"modal-dialog create-account-dialog\" id=\"createAccountDialog\" data-open-on-load=\"true\"")));
    }

    @Test
    void adminCanCreateHrManagerAndInterviewerAccountsFromScr08() throws Exception {
        mockMvc.perform(post("/admin/users").session(adminSession())
                        .param("fullName", "New HR Manager")
                        .param("username", "newhrmanager")
                        .param("email", "newhrmanager@example.com")
                        .param("roleName", Role.HR_MANAGER)
                        .param("initialPassword", "StrongPass1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?created=true"));

        User hr = userRepository.findByUsernameWithRole("newhrmanager").orElseThrow();
        assertEquals(Role.HR_MANAGER, hr.getRole().getName());
        assertTrue(hr.isEnabled());
        assertTrue(passwordUtil.matches("StrongPass1", hr.getPassword()));

        mockMvc.perform(post("/admin/users").session(adminSession())
                        .param("fullName", "Interview User")
                        .param("username", "interviewuser")
                        .param("email", "interviewuser@example.com")
                        .param("roleName", Role.INTERVIEWER)
                        .param("initialPassword", "StrongPass1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?created=true"));

        User interviewer = userRepository.findByUsernameWithRole("interviewuser").orElseThrow();
        assertEquals(Role.INTERVIEWER, interviewer.getRole().getName());
    }

    @Test
    void adminCanFilterDeactivateAndUnlockUsersWithoutDeletingThem() throws Exception {
        mockMvc.perform(post("/admin/users").session(adminSession())
                        .param("fullName", "Filter Target")
                        .param("username", "filtertarget")
                        .param("email", "filtertarget@example.com")
                        .param("roleName", Role.HR_MANAGER)
                        .param("initialPassword", "StrongPass1"))
                .andExpect(status().is3xxRedirection());
        User target = userRepository.findByUsernameWithRole("filtertarget").orElseThrow();

        mockMvc.perform(get("/admin/users").session(adminSession())
                        .param("keyword", "filter")
                        .param("role", Role.HR_MANAGER)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("filtertarget@example.com")))
                .andExpect(content().string(containsString("HR_MANAGER")))
                .andExpect(content().string(containsString("ACTIVE")));

        mockMvc.perform(post("/admin/users/{id}/deactivate", target.getId()).session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?deactivated=true"));

        User inactive = userService.findById(target.getId());
        assertFalse(inactive.isEnabled());
        assertTrue(userRepository.findById(target.getId()).isPresent());

        mockMvc.perform(get("/admin/users").session(adminSession()).param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("filtertarget@example.com")))
                .andExpect(content().string(containsString("INACTIVE")))
                .andExpect(content().string(containsString("Reactivate")));

        User alice = userRepository.findByUsernameWithRole("alice").orElseThrow();
        userService.lockAccount(alice.getId());
        mockMvc.perform(get("/admin/users").session(adminSession()).param("status", "LOCKED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice@example.com")))
                .andExpect(content().string(containsString("LOCKED")))
                .andExpect(content().string(containsString("Unlock")));

        mockMvc.perform(post("/admin/users/{id}/unlock", alice.getId()).session(adminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?unlocked=true"));
        assertTrue(userService.findById(alice.getId()).isEnabled());
    }

    @Test
    void userManagementHidesDeactivateForOnlyActiveAdmin() throws Exception {
        mockMvc.perform(get("/admin/users").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-user-row=\"admin\"")))
                .andExpect(content().string(containsString("Only active Admin account")));
    }

    @Test
    void nonAdminSessionIsBlockedFromAdminRoutes() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                new SessionUser(99L, "candidate", "Candidate User", Role.CANDIDATE));

        mockMvc.perform(get("/admin/users").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
    }

    private MockHttpSession adminSession() {
        User admin = userRepository.findByUsernameWithRole("admin").orElseThrow();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER, SessionUser.from(admin));
        return session;
    }
}
