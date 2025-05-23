package com.example.alphasolutionsv2.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {
    // Viser forsiden når brugeren går til ind på siden.
    @GetMapping({"/", "/frontpage"})
    public String showFrontPage() {
        return "calcura-frontpage"; // matcher filnavn i templates-mappen
    }

    @GetMapping("/login")
    public String loginRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();

                if (role.equals("ROLE_ADMIN")) {
                    return "redirect:/admin-dashboard";
                } else if (role.equals("ROLE_USER")) {
                    return "redirect:/my-projects";
                }
            }
            // Hvis ingen kendt rolle
            return "redirect:/frontpage";
        }

        // Hvis ikke logget ind
        return "login";
    }
}
