package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountType;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts") // Base path for all account-related endpoints
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Endpoint to create a new bank account for the authenticated user.
     * Accessible by customers (their own accounts) and staff/admins (for their own or later for others).
     * @param accountType The type of account to create (e.g., SAVINGS, CURRENT).
     * @return ResponseEntity with the created Account or an error message.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> createAccount(@RequestParam @NotNull AccountType accountType) {
        try {
            Account newAccount = accountService.createAccount(accountType);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
        } catch (IllegalStateException e) {
            // This catches if the user is not authenticated (from AccountService helper)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            // Catch any other unexpected errors during account creation
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating account: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to get all accounts for the authenticated user.
     * Accessible by customers (their own accounts) and staff/admins.
     * @return ResponseEntity with a list of accounts or an error message.
     */
    @GetMapping("/my-accounts")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyAccounts() {
        try {
            List<Account> accounts = accountService.getMyAccounts();
            if (accounts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("No accounts found for the current user."));
            }
            return ResponseEntity.ok(accounts);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving accounts: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to get a specific account by its ID, ensuring it belongs to the authenticated user.
     * Accessible by customers (their own specific account) and staff/admins (any specific account, if allowed by future logic).
     * @param accountId The ID of the account to retrieve.
     * @return ResponseEntity with the Account details or an error message.
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId) {
        try {
            Optional<Account> account = accountService.getAccountById(accountId);
            if (account.isPresent()) {
                return ResponseEntity.ok(account.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Account not found or not accessible to current user."));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving account: " + e.getMessage()));
        }
    }


}