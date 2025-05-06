package controller;

import com.example.alphasolutionsv2.AlphaSolutionsV2Application;
import com.example.alphasolutionsv2.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AlphaSolutionsV2Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testShouldReturnProjectListView_whenUserIsInSession() throws Exception {
        // Opret testbruger med userId = 1 (skal findes i DB)
        User testUser = new User();
        testUser.setUserId(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/projects")
                .sessionAttr("loggedInUser", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attributeExists("projects"));

    }

    @Test
    void testShouldRedirectToLogin_whenUserIsNotInSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void showCreateForm() {
    }

    @Test
    void testShowCreateForm() {
    }
}