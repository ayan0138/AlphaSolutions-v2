package controller;

import com.example.alphasolutionsv2.AlphaSolutionsV2Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AlphaSolutionsV2Application.class)
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testShouldReturnProjectListView_whenUserIsInSession() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/my-projects")
                .sessionAttr("userID", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("project-list"))
                .andExpect(model().attributeExists("projects"));

    }

    @Test
    void testShouldRedirectToLogin_whenUserIsNotInSession() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/my-projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

}