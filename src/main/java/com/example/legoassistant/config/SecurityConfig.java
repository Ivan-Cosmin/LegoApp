package com.example.legoassistant.config;

import com.example.legoassistant.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Allow access to the login page + static assets without authentication
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/actuator/health", "/actuator/info").permitAll()
                // Logout is handled by Spring Security (POST /logout)
                .requestMatchers("/logout").permitAll()
                // Everything else requires a logged-in user
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                // Always go home after login
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                // POST /logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Explicitly configure authentication to use our database-backed users.
     * This ensures the login uses CustomUserDetailsService + BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
