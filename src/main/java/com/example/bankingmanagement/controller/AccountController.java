package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountType;
import com.example.bankingmanagement.payload.request.CreateAccountRequest;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.service.AccountService;
import jakarta.validation.Valid; // For @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Endpoint to create a new bank account for the authenticated user.
     * Accessible by customers (their own accounts) and staff/admins.
     * Expects a JSON request body containing accountType and initialBalance.
     * POST /api/accounts
     * @param request The DTO containing account type and initial balance.
     * @param principal The authenticated user's principal.
     * @return ResponseEntity with the created Account or an error message.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request, Principal principal) {
        try {
            Account createdAccount = accountService.createAccountForUser(
                    principal.getName(), request.getAccountType(), request.getInitialBalance()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating account: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to get all accounts for the authenticated user.
     * Accessible by customers (their own accounts) and staff/admins.
     * GET /api/accounts
     * @param principal The authenticated user's principal.
     * @return ResponseEntity with a list of accounts or an error message.
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyAccounts(Principal principal) { // Added Principal to get the current user
        try {
            List<Account> accounts = accountService.getMyAccounts(principal.getName());
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
     * GET /api/accounts/{accountId}
     * @param accountId The ID of the account to retrieve.
     * @param principal The authenticated user's principal.
     * @return ResponseEntity with the Account details or an error message.
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId, Principal principal) {
        try {
            Optional<Account> account = accountService.getAccountByIdAndUser(accountId, principal.getName());
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