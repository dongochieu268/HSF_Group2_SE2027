package com.recruit.recruitmentapplication;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.repository.UserRepository;
import com.recruit.recruitmentapplication.util.SessionConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Scr16ApplicationNavigationWebTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    void managerCanOpenApplicationDetailFromApplicationList() throws Exception {
        mockMvc.perform(get("/manage/applications/3").session(session("hrmanager")))
                .andExpect(status().isOk())
                .andExpect(view().name("application/detail"))
                .andExpect(content().string(containsString("Carol Le")))
                .andExpect(content().string(containsString("/manage/jobs/3/applications")));
    }

    private MockHttpSession session(String username) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConstants.LOGGED_IN_USER,
                SessionUser.from(userRepository.findByUsernameWithRole(username).orElseThrow()));
        return session;
    }
}
