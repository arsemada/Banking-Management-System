package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountStatus;
import com.example.bankingmanagement.model.AccountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {

    Account createAccount(AccountType accountType);

    Account createAccountForUser(String username, AccountType accountType, BigDecimal initialBalance);

    List<Account> getMyAccounts(String username);

    Optional<Account> getAccountByIdAndUser(Long accountId, String username);

    Optional<Account> getAccountById(Long accountId);

    Account deposit(String username, Long accountId, BigDecimal amount);

    Account withdraw(String username, Long accountId, BigDecimal amount);

    void transferFunds(String username, Long fromAccountId, String toAccountNumber, BigDecimal amount);

    List<Account> getAllAccounts();
    Optional<Account> getAccountByIdAdmin(Long accountId);

    // --- NEW Methods for Staff Dashboard (Account Status Management) ---
    void freezeAccount(Long accountId);
    void unfreezeAccount(Long accountId);
    void closeAccount(Long accountId); // Added for closing accounts
    Account updateAccountStatus(Long accountId, AccountStatus newStatus); // Added for general status updates
}