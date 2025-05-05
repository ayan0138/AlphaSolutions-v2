package controller;

import org.springframework.web.bind.annotation.GetMapping;

public class ProjectController {
    @GetMapping("/create")
    public String showCreateForm() {
        return "create-project"; // Matcher create-project.html -> redirect til post.
    }
}

