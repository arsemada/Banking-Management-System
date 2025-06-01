package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountType;
import com.example.bankingmanagement.payload.request.CreateAccountRequest;
import com.example.bankingmanagement.payload.request.TransactionRequest;
import com.example.bankingmanagement.payload.request.TransferRequest;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.service.AccountService;
import jakarta.validation.Valid;
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

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyAccounts(Principal principal) {
        try {
            List<Account> accounts = accountService.getMyAccounts(principal.getName());
            return ResponseEntity.ok(accounts);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving accounts: " + e.getMessage()));
        }
    }

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

    @PostMapping("/{accountId}/deposit")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request,
            Principal principal) {
        try {
            Account updatedAccount = accountService.deposit(principal.getName(), accountId, request.getAmount());
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error during deposit: " + e.getMessage()));
        }
    }

    @PostMapping("/{accountId}/withdraw")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request,
            Principal principal) {
        try {
            Account updatedAccount = accountService.withdraw(principal.getName(), accountId, request.getAmount());
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error during withdrawal: " + e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request,
            Principal principal) {
        try {
            accountService.transferFunds(
                    principal.getName(),
                    request.getFromAccountId(),
                    request.getToAccountNumber(),
                    request.getAmount()
            );
            return ResponseEntity.ok(new MessageResponse("Funds transferred successfully!"));
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            // Catch specific business logic errors for clearer messages
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            // Catch any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error during transfer: " + e.getMessage()));
        }
    }
}