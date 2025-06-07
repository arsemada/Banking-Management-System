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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/css/**"),
                new AntPathRequestMatcher("/js/**"),
                new AntPathRequestMatcher("/favicon.ico")
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // API authentication endpoints (login, registration)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Publicly accessible HTML pages (login, register, dashboard, admin, error)
                        // All these HTML pages are served. Frontend JS will handle redirects based on roles.
                        .requestMatchers("/", "/login", "/register", "/dashboard", "/admin", "/error").permitAll() // <-- CHANGED THIS BACK
                        // API endpoints requiring ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // API endpoints requiring STAFF or ADMIN role
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                        // API endpoints requiring CUSTOMER or ADMIN role
                        .requestMatchers("/api/user/**").hasAnyRole("CUSTOMER", "ADMIN")
                        // All other API requests must be authenticated
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}