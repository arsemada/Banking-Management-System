package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.ERole;
import com.example.bankingmanagement.model.Role;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.payload.request.LoginRequest;
import com.example.bankingmanagement.payload.request.SignupRequest;
import com.example.bankingmanagement.payload.response.JwtResponse;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.repository.RoleRepository;
import com.example.bankingmanagement.repository.UserRepository;
import com.example.bankingmanagement.security.jwt.JwtUtils;
import com.example.bankingmanagement.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Use method reference for clarity
                .collect(Collectors.toList());

        // Return JwtResponse with all user details, consistent with previous discussions
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    public ResponseEntity<?> registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        // Assuming signupRequest.getRoles() returns a Set<String> (e.g., {"admin", "staff"})
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>(); // This Set will hold Role Entities

        if (strRoles == null || strRoles.isEmpty()) {
            // Default to ROLE_CUSTOMER if no roles are specified
            Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_CUSTOMER' is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                // Convert to lowercase for case-insensitive matching of input strings
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_ADMIN' is not found."));
                        roles.add(adminRole);
                        break;
                    case "staff":
                        Role staffRole = roleRepository.findByName(ERole.ROLE_STAFF)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_STAFF' is not found."));
                        roles.add(staffRole);
                        break;
                    case "customer": // Explicitly handle 'customer' string input
                        Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_CUSTOMER' is not found."));
                        roles.add(customerRole);
                        break;
                    default: // Assign default role if an unknown string is provided
                        Role defaultRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Default role 'ROLE_CUSTOMER' is not found."));
                        roles.add(defaultRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}