package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {

    Account createAccount(AccountType accountType); // Keep this if used elsewhere

    Account createAccountForUser(String username, AccountType accountType, BigDecimal initialBalance);

    List<Account> getMyAccounts(String username);

    Optional<Account> getAccountByIdAndUser(Long accountId, String username);

    Optional<Account> getAccountById(Long accountId); // Keep this if used elsewhere

    // METHODS FOR DEPOSIT AND WITHDRAWAL ---
    Account deposit(String username, Long accountId, BigDecimal amount);
    Account withdraw(String username, Long accountId, BigDecimal amount);
}