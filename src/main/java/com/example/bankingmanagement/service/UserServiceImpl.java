package com.example.bankingmanagement.service.impl;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.ERole; // Import ERole
import com.example.bankingmanagement.model.Role;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.payload.request.UserCreateRequest; // New
import com.example.bankingmanagement.payload.request.UserUpdateRequest; // New
import com.example.bankingmanagement.repository.RoleRepository;
import com.example.bankingmanagement.repository.UserRepository;
import com.example.bankingmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Inject RoleRepository

    @Autowired
    private PasswordEncoder encoder; // Inject PasswordEncoder

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getInactiveUsers() {
        return userRepository.findByEnabledFalse();
    }

    @Override
    @Transactional
    public void approveUserRegistration(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.isEnabled()) {
            throw new RuntimeException("User with ID: " + userId + " is already active.");
        }

        user.setEnabled(true); // Set user to active
        userRepository.save(user);
    }

    // --- NEW: Admin User Management Implementations ---

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Error: Username '" + request.getUsername() + "' is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email '" + request.getEmail() + "' is already in use!");
        }

        // Create new user's account with detailed profile fields and initial enabled status
        User user = new User(request.getUsername(),
                request.getEmail(),
                encoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getEnabled() != null ? request.getEnabled() : true // Use provided enabled status, default to true if null
        );

        Set<String> strRoles = request.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // Default to CUSTOMER if no roles are specified by admin
            Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toUpperCase()) { // Convert to uppercase for ERole enum matching
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "STAFF":
                        Role staffRole = roleRepository.findByName(ERole.ROLE_STAFF)
                                .orElseThrow(() -> new RuntimeException("Error: Role STAFF is not found."));
                        roles.add(staffRole);
                        break;
                    case "CUSTOMER":
                        Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER is not found."));
                        roles.add(customerRole);
                        break;
                    default:
                        throw new IllegalArgumentException("Error: Role " + role + " is invalid!");
                }
            });
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if email is changed and if new email is already in use by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email '" + request.getEmail() + "' is already in use by another user!");
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(request.getEnabled()); // Update enabled status

        // Update roles
        Set<String> strRoles = request.getRoles();
        if (strRoles != null && !strRoles.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            strRoles.forEach(role -> {
                switch (role.toUpperCase()) { // Convert to uppercase for ERole enum matching
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "STAFF":
                        Role staffRole = roleRepository.findByName(ERole.ROLE_STAFF)
                                .orElseThrow(() -> new RuntimeException("Error: Role STAFF is not found."));
                        roles.add(staffRole);
                        break;
                    case "CUSTOMER":
                        Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role CUSTOMER is not found."));
                        roles.add(customerRole);
                        break;
                    default:
                        throw new IllegalArgumentException("Error: Role " + role + " is invalid!");
                }
            });
            user.setRoles(roles);
        } else {
            // Disallow removing all roles
            throw new IllegalArgumentException("At least one role must be assigned to the user.");
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        // This method performs a soft delete by disabling the user.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setEnabled(false); // Disable the user
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
    }
}