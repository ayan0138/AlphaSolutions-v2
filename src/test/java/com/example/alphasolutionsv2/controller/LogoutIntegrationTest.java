package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.config.SecurityConfig;
import com.example.alphasolutionsv2.service.ApplicationUserDetailsService;
import com.example.alphasolutionsv2.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers =  LogoutIntegrationTest.class)
@Import({SecurityConfig.class, LogoutIntegrationTest.MockedSecurityBeans.class})
public class LogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testLogoutShouldRedirectAndInvalidateSession() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(cookie().maxAge("JSESSIONID", 0)); // session cookie slettes
    }

    @TestConfiguration
    static class MockedSecurityBeans {
        @Bean
        public ApplicationUserDetailsService customUserDetailsService() {
            return Mockito.mock(ApplicationUserDetailsService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }
}
