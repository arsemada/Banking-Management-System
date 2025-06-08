package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.Transaction;
import com.example.bankingmanagement.payload.request.CreateAccountRequest;
import com.example.bankingmanagement.payload.request.TransactionRequest;
import com.example.bankingmanagement.payload.request.TransferRequest;
import com.example.bankingmanagement.payload.response.MessageResponse;
import com.example.bankingmanagement.service.AccountService;
import com.example.bankingmanagement.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer") // New base path for customer-specific actions
@PreAuthorize("hasRole('CUSTOMER')") // Only customers can access these endpoints
public class CustomerController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    // --- Customer-specific Account Creation (if customer can self-create) ---
    // Moved from AccountController if you want this exclusively for customers
    // However, your AccountController has roles 'CUSTOMER', 'STAFF', 'ADMIN'
    // For now, let's keep AccountController's createAccount for broader access
    // If you want ONLY customers to create via this endpoint, remove it from AccountController
    /*
    @PostMapping("/accounts")
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
    */

    // --- Customer's Own Account Listing ---
    @GetMapping("/accounts")
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

    // --- Get a Specific Account for the Customer ---
    @GetMapping("/accounts/{accountId}")
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

    // --- Customer Deposit ---
    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<?> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request,
            Principal principal) {
        try {
            Account updatedAccount = accountService.deposit(principal.getName(), accountId, request.getAmount());
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalArgumentException | IllegalStateException e) { // Catch both here
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error during deposit: " + e.getMessage()));
        }
    }

    // --- Customer Withdrawal ---
    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<?> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request,
            Principal principal) {
        try {
            Account updatedAccount = accountService.withdraw(principal.getName(), accountId, request.getAmount());
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalArgumentException | IllegalStateException e) { // Catch both here
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error during withdrawal: " + e.getMessage()));
        }
    }

    // --- Customer Fund Transfer ---
    @PostMapping("/accounts/transfer")
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
        } catch (IllegalArgumentException | IllegalStateException e) { // Catch both here
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error during transfer: " + e.getMessage()));
        }
    }

    // --- Customer Transaction History for an Account ---
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getAccountTransactions(@PathVariable Long accountId, Principal principal) {
        try {
            // Ensure the customer can only see transactions for *their own* account
            // transactionService.getTransactionsByAccountIdAndUser will handle this check
            List<Transaction> transactions = transactionService.getTransactionsByAccountIdAndUser(accountId, principal.getName());
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error fetching transactions: " + e.getMessage()));
        }
    }
}