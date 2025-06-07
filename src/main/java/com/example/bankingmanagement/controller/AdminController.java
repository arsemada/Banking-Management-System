package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.payload.response.TransactionDto; // Import the new DTO
import com.example.bankingmanagement.service.AccountService;
import com.example.bankingmanagement.service.TransactionService;
import com.example.bankingmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok().body(userOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found."));
            }
        } catch (Exception e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching user: " + e.getMessage()));
        }
    }

    // --- Account Management Endpoints ---

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

    // --- Transaction Management Endpoint ---

    @GetMapping("/transactions")
    // Changed return type to List<TransactionDto>
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        try {
            List<TransactionDto> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            // It's good practice to have a specific error DTO for consistency
            // For now, using MessageResponse, but return type is List<TransactionDto>
            // So, for errors, it's better to return a ResponseEntity<?> or ErrorResponse
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or new ErrorResponse(...)
        }
    }
}