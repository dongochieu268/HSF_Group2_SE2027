package com.recruit.recruitmentapplication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateRegistrationLoginWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private com.recruit.recruitmentapplication.repository.UserRepository userRepository;
    @Autowired private com.recruit.recruitmentapplication.repository.CandidateRepository candidateRepository;

    @Test
    void newlyRegisteredCandidateCanLoginAndOpenMyApplications() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("fullName", "New Candidate")
                        .param("username", "newcandidate")
                        .param("email", "newcandidate@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?registered=true"));

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .param("username", "newcandidate")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-applications"))
                .andReturn();

        mockMvc.perform(get("/my-applications").session((org.springframework.mock.web.MockHttpSession)
                        login.getRequest().getSession(false)))
                .andExpect(status().isOk());

        assertNotNull(candidateRepository.findByEmail("newcandidate@example.com")
                .orElseThrow().getProfile());
    }

    @Test
    void existingCandidateCanLoginAndOpenMyApplicationsWithJobs() throws Exception {
        mockMvc.perform(get("/my-applications").session(userSession("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Senior Java Developer")));
    }

    @Test
    void adminCreatedHrManagerCanLoginAndOpenDashboard() throws Exception {
        mockMvc.perform(post("/admin/users").session(adminSession())
                        .param("fullName", "Created HR")
                        .param("username", "createdhr")
                        .param("email", "createdhr@example.com")
                        .param("roleName", "HR_MANAGER")
                        .param("initialPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users?created=true"));

        mockMvc.perform(post("/auth/login")
                        .param("username", "createdhr")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/hr/dashboard"));
    }

    @Test
    void registrationLinksExistingCandidateProfileByEmail() throws Exception {
        candidateRepository.save(new com.recruit.recruitmentapplication.entity.Candidate(
                "Imported Candidate", "imported@example.com", "0909999999"));

        mockMvc.perform(post("/auth/register")
                        .param("fullName", "Imported Candidate")
                        .param("username", "importedcandidate")
                        .param("email", "imported@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?registered=true"));

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .param("username", "importedcandidate")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-applications"))
                .andReturn();

        mockMvc.perform(get("/my-applications").session((org.springframework.mock.web.MockHttpSession)
                        login.getRequest().getSession(false)))
                .andExpect(status().isOk());
    }

    private org.springframework.mock.web.MockHttpSession adminSession() {
        return userSession("admin");
    }

    private org.springframework.mock.web.MockHttpSession userSession(String username) {
        org.springframework.mock.web.MockHttpSession session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute(com.recruit.recruitmentapplication.util.SessionConstants.LOGGED_IN_USER,
                com.recruit.recruitmentapplication.dto.SessionUser.from(
                        userRepository.findByUsernameWithRole(username).orElseThrow()));
        return session;
    }
}
