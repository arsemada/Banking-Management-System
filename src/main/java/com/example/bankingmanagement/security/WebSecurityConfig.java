package com.example.bankingmanagement.security;

import com.example.bankingmanagement.security.jwt.AuthEntryPointJwt;
import com.example.bankingmanagement.security.jwt.AuthTokenFilter;
import com.example.bankingmanagement.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // Keep this import
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // Keep this import

@Configuration
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, @HasRole, etc.
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // This bean handles ignoring static resources, which is good.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/css/**"),
                new AntPathRequestMatcher("/js/**"),
                new AntPathRequestMatcher("/favicon.ico"),
                // Add any other static resources or images here if needed
                new AntPathRequestMatcher("/images/**")
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // API authentication endpoints (login, registration) - MUST BE PERMITTED
                        // This covers /api/auth/signup and /api/auth/login
                        .requestMatchers("/api/auth/**").permitAll()

                        // Publicly accessible HTML pages (served by FrontendController)
                        // These are the actual HTML files that should load without authentication.
                        // Client-side JavaScript will then manage UI elements and protected API calls.
                        .requestMatchers("/", "/login", "/register", "/dashboard", "/admin", "/staff", "/error").permitAll()

                        // API endpoints requiring ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // API endpoints requiring STAFF or ADMIN role
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")

                        // API endpoints requiring CUSTOMER or ADMIN role (adjust roles as per your design)
                        // If 'USER' is a generic role for all logged-in customers, keep it.
                        // If only 'CUSTOMER' role is for regular users, then remove 'USER'.
                        // "/api/accounts/**" is typically for logged-in customers
                        .requestMatchers("/api/accounts/**").hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                        // You might have other customer-specific APIs, e.g., if you have /api/customer/profile
                        // .requestMatchers("/api/customer/**").hasRole("CUSTOMER") // Example

                        // All other API requests must be authenticated by default
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}