package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);


    List<User> getAllUsers(); // To get all users
    Optional<User> getUserById(Long id);
}