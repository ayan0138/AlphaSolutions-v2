package com.example.alphasolutionsv2.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Offentlige ressourcer
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/frontpage", "/").permitAll()

                        // Admin-kun ressourcer - KUN hasAuthority overalt
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // Projektleder og Admin kan oprette og slette projekter/subprojekter/opgaver
                        .requestMatchers("/projects/create", "/projects/*/subprojects/create")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PROJEKTLEDER")
                        .requestMatchers("/tasks/create", "/tasks/project/*/create")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PROJEKTLEDER")
                        .requestMatchers("/tasks/*/delete", "/projects/*/delete")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PROJEKTLEDER")

                        // Projektleder og Admin kan tildele opgaver
                        .requestMatchers("/tasks/*/assign")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PROJEKTLEDER")

                        // Medarbejdere kan se deres tildelte opgaver
                        .requestMatchers("/tasks/my-assigned-tasks").authenticated()
                        .requestMatchers("/projects/**").authenticated() // Allow all authenticated users to view projects

                        // Alle andre kræver authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/my-projects", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout ->
                        logout.logoutSuccessUrl("/login?logout=true")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                );
        return http.build();
    }
}
