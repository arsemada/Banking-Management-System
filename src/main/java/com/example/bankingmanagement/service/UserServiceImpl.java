package com.example.bankingmanagement.service;

import com.example.bankingmanagement.exception.ResourceNotFoundException; // Ensure this import is present
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For @Transactional

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

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

    // --- NEW Methods for Staff Dashboard ---

    @Override
    public List<User> getInactiveUsers() {
        // This will find users where the 'enabled' field is false.
        // It relies on the findByEnabledFalse() method in UserRepository.
        return userRepository.findByEnabledFalse();
    }

    @Override
    @Transactional // Ensures the operation is atomic
    public void approveUserRegistration(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.isEnabled()) {
            throw new RuntimeException("User with ID: " + userId + " is already active.");
        }

        user.setEnabled(true); // Set user to active
        userRepository.save(user);
    }
}