package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.service.AccountService;
import com.example.bankingmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')") // Staff and Admin can access these endpoints
public class StaffController {

    @Autowired
    private UserService userService; // To manage user registrations

    @Autowired
    private AccountService accountService; // To manage accounts (freeze/unfreeze)

    // --- Customer Registration Approval Endpoints ---

    @GetMapping("/registrations/pending")
    public ResponseEntity<?> getPendingRegistrations() {
        try {
            // Assuming you have a method in UserService to find users with a 'PENDING' status or similar
            // For now, let's assume getInactiveUsers() returns users awaiting approval.
            // You might need to adjust this based on how you track registration status.
            List<User> pendingUsers = userService.getInactiveUsers(); // You'll implement this method
            return ResponseEntity.ok(pendingUsers);
        } catch (Exception e) {
            System.err.println("Error fetching pending registrations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching pending registrations: " + e.getMessage()));
        }
    }

    @PostMapping("/registrations/{userId}/approve")
    public ResponseEntity<?> approveRegistration(@PathVariable Long userId) {
        try {
            // You'll implement this method in UserService to change user status to active
            userService.approveUserRegistration(userId);
            return ResponseEntity.ok(new MessageResponse("User registration approved successfully!"));
        } catch (RuntimeException e) { // Catch specific exceptions like UserNotFoundException
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error approving registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error approving registration: " + e.getMessage()));
        }
    }

    // --- Customer Account Management Endpoints ---

    @GetMapping("/accounts")
    public ResponseEntity<?> getAllCustomerAccounts() {
        try {
            // Staff might only view active customer accounts, or all accounts.
            // For now, let's use the same as admin, you can filter later if needed.
            List<Account> accounts = accountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            System.err.println("Error fetching customer accounts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching accounts: " + e.getMessage()));
        }
    }

    @PutMapping("/accounts/{accountId}/freeze")
    public ResponseEntity<?> freezeAccount(@PathVariable Long accountId) {
        try {
            accountService.freezeAccount(accountId); // You'll implement this method
            return ResponseEntity.ok(new MessageResponse("Account frozen successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error freezing account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error freezing account: " + e.getMessage()));
        }
    }

    @PutMapping("/accounts/{accountId}/unfreeze")
    public ResponseEntity<?> unfreezeAccount(@PathVariable Long accountId) {
        try {
            accountService.unfreezeAccount(accountId); // You'll implement this method
            return ResponseEntity.ok(new MessageResponse("Account unfrozen successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error unfreezing account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error unfreezing account: " + e.getMessage()));
        }
    }
}