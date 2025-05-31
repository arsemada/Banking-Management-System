package com.example.bankingmanagement.service;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountType;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.model.Transaction; // Import Transaction model
import com.example.bankingmanagement.model.TransactionType; // Import TransactionType enum
import com.example.bankingmanagement.repository.AccountRepository;
import com.example.bankingmanagement.repository.UserRepository;
import com.example.bankingmanagement.repository.TransactionRepository; // Import TransactionRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Account createAccount(AccountType accountType) {
        Account newAccount = new Account();
        newAccount.setAccountType(accountType);
        newAccount.setAccountNumber(generateAccountNumber());
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(newAccount);
    }

    @Override
    @Transactional
    public Account createAccountForUser(String username, AccountType accountType, BigDecimal initialBalance) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Account newAccount = new Account();
        newAccount.setAccountType(accountType);
        newAccount.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setAccountNumber(generateAccountNumber());
        newAccount.setUser(user);

        return accountRepository.save(newAccount);
    }

    @Override
    public List<Account> getMyAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return accountRepository.findByUser(user);
    }

    @Override
    public Optional<Account> getAccountByIdAndUser(Long accountId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return accountRepository.findByIdAndUser(accountId, user);
    }

    @Override
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    // --- NEW DEPOSIT METHOD ---
    @Override
    @Transactional
    public Account deposit(String username, Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        // Find the account and ensure it belongs to the authenticated user
        Account account = getAccountByIdAndUser(accountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not accessible."));

        // Update account balance
        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(updatedAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    // WITHDRAWAL METHOD ---
    @Override
    @Transactional
    public Account withdraw(String username, Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }

        // Find the account and ensure it belongs to the authenticated user
        Account account = getAccountByIdAndUser(accountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not accessible."));

        // Check for sufficient funds
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal.");
        }

        // Update account balance
        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(updatedAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}