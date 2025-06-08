package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.payload.request.UserCreateRequest;
import com.example.bankingmanagement.payload.request.UserUpdateRequest;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.payload.response.TransactionDto;
import com.example.bankingmanagement.payload.response.UserResponse; // Import new DTO
import com.example.bankingmanagement.service.AccountService;
import com.example.bankingmanagement.service.TransactionService;
import com.example.bankingmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    // --- User Management Endpoints ---

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() { // Changed return type to List<UserResponse>
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new) // Convert User entity to UserResponse DTO
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            // It's better to return an empty list or an ErrorResponse DTO for consistency
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of()); // Return empty list on error
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok().body(new UserResponse(userOptional.get())); // Convert to UserResponse
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found."));
            }
        } catch (Exception e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching user: " + e.getMessage()));
        }
    }

    // --- NEW: Create User Endpoint ---
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        try {
            User newUser = userService.createUser(userCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(newUser)); // Return UserResponse
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) { // For role not found etc.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating user: " + e.getMessage()));
        }
    }

    // --- NEW: Update User Endpoint ---
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        try {
            User updatedUser = userService.updateUser(id, userUpdateRequest);
            return ResponseEntity.ok(new UserResponse(updatedUser)); // Return UserResponse
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (com.example.bankingmanagement.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating user: " + e.getMessage()));
        }
    }

    // --- NEW: Enable User Endpoint ---
    @PutMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            userService.enableUser(id);
            return ResponseEntity.ok(new MessageResponse("User enabled successfully!"));
        } catch (com.example.bankingmanagement.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error enabling user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error enabling user: " + e.getMessage()));
        }
    }

    // --- NEW: Disable User Endpoint ---
    @PutMapping("/users/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            userService.disableUser(id);
            return ResponseEntity.ok(new MessageResponse("User disabled successfully!"));
        } catch (com.example.bankingmanagement.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error disabling user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error disabling user: " + e.getMessage()));
        }
    }

    // --- NEW: Delete User (Soft Delete) Endpoint ---
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id); // This performs a soft delete (disables)
            return ResponseEntity.ok(new MessageResponse("User (ID: " + id + ") successfully disabled."));
        } catch (com.example.bankingmanagement.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error disabling user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error disabling user: " + e.getMessage()));
        }
    }

    // --- Pending Approvals Endpoint ---
    @GetMapping("/users/inactive")
    public ResponseEntity<List<UserResponse>> getInactiveUsers() {
        try {
            List<User> inactiveUsers = userService.getInactiveUsers();
            List<UserResponse> userResponses = inactiveUsers.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            System.err.println("Error fetching inactive users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        try {
            userService.approveUserRegistration(id);
            return ResponseEntity.ok(new MessageResponse("User registration approved successfully!"));
        } catch (com.example.bankingmanagement.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) { // For "already active"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error approving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error approving user: " + e.getMessage()));
        }
    }

    // --- Account Management Endpoints (Existing, no changes needed for this task) ---

    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        }
        catch (Exception e) {
            System.err.println("Error fetching accounts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching accounts: " + e.getMessage()));
        }
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        try {
            Optional<Account> accountOptional = accountService.getAccountByIdAdmin(id);
            if (accountOptional.isPresent()) {
                return ResponseEntity.ok().body(accountOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Account not found."));
            }
        } catch (Exception e) {
            System.err.println("Error fetching account by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching account: " + e.getMessage()));
        }
    }

    // --- Transaction Management Endpoint (Existing, no changes needed for this task) ---

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        try {
            List<TransactionDto> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of()); // Return empty list or specific error DTO
        }
    }
}