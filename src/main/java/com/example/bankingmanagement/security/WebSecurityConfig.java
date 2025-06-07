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
import org.springframework.security.config.annotation.web.builders.WebSecurity; // Import WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // Import WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // Required for AntPathRequestMatcher

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

    /**
     * This bean is crucial for telling Spring Security to completely ignore
     * certain paths from its entire filter chain. This is ideal for static
     * resources and direct HTML page requests that should not be secured.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                // Static resources (CSS, JS, images, favicon)
                new AntPathRequestMatcher("/css/**"),
                new AntPathRequestMatcher("/js/**"),
                new AntPathRequestMatcher("/favicon.ico"),
                // Publicly accessible HTML pages
                new AntPathRequestMatcher("/"),          // Root path (if you have an index.html)
                new AntPathRequestMatcher("/login"),
                new AntPathRequestMatcher("/register"),
                new AntPathRequestMatcher("/dashboard"),
                new AntPathRequestMatcher("/admin"),
                // The error page itself should be accessible to display messages
                new AntPathRequestMatcher("/error")
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // API authentication endpoints (JWT will be handled by AuthController)
                        .requestMatchers("/api/auth/**").permitAll()
                        // API endpoints requiring ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // API endpoints requiring USER or ADMIN role
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // All other API requests must be authenticated (e.g., /api/accounts, /api/transactions)
                        .anyRequest().authenticated()
                );

        // Add the JWT token filter before the Spring Security UsernamePasswordAuthenticationFilter
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}