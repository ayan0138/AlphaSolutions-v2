package com.example.alphasolutionsv2.config;

import com.example.alphasolutionsv2.repository.UserRepository;
import com.example.alphasolutionsv2.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@EnableWebSecurity
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
                        .requestMatchers("/", "/frontpage", "/login", "/css/**",
                                "/js/**", "/images/**").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/my-projects", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout ->  // Task 7.1 En metode til at logge brugeren ud
                        logout.logoutSuccessUrl("/login?logout=true") //Task 7.4: Redirect til login-siden efter logout
                        .invalidateHttpSession(true)  //Task 7.3: Lav en metode der afslutter sessionen
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }
}
