package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.payload.request.UserCreateRequest; // New
import com.example.bankingmanagement.payload.request.UserUpdateRequest; // New
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);

    List<User> getAllUsers();
    Optional<User> getUserById(Long id);

    List<User> getInactiveUsers(); // For pending approvals
    void approveUserRegistration(Long userId); // For pending approvals

    // --- NEW: Admin User Management Methods ---
    User createUser(UserCreateRequest request);
    User updateUser(Long userId, UserUpdateRequest request);
    void deleteUser(Long userId); // Soft delete (disable)
    void enableUser(Long userId); // Enable a disabled user
    void disableUser(Long userId); // Disable a user
}