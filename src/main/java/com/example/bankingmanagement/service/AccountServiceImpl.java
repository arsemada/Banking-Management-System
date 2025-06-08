package com.example.bankingmanagement.service;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountStatus; // Import the new enum
import com.example.bankingmanagement.model.AccountType;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.model.Transaction;
import com.example.bankingmanagement.model.TransactionType;
import com.example.bankingmanagement.repository.AccountRepository;
import com.example.bankingmanagement.repository.UserRepository;
import com.example.bankingmanagement.repository.TransactionRepository;
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
        // createdAt and status are set in Account constructor or @PrePersist if you add it there

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
        newAccount.setAccountNumber(generateAccountNumber());
        newAccount.setUser(user);
        newAccount.setStatus(AccountStatus.ACTIVE); // Ensure status is explicitly set for new accounts
        // createdAt is set in Account constructor

        Account savedAccount = accountRepository.save(newAccount);

        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            // Ensure status is set to "COMPLETED"
            Transaction initialDeposit = new Transaction(savedAccount, TransactionType.DEPOSIT, initialBalance, "COMPLETED");
            transactionRepository.save(initialDeposit);
        }

        return savedAccount;
    }

    @Override
    public List<Account> getMyAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        // You might want to ensure only active accounts are returned for customers
        // return accountRepository.findByUserAndStatus(user, AccountStatus.ACTIVE);
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

    @Override
    @Transactional
    public Account deposit(String username, Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        Account account = getAccountByIdAndUser(accountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not accessible."));

        // Add check for account status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active and cannot accept deposits.");
        }

        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);

        // Ensure status is set to "COMPLETED"
        Transaction transaction = new Transaction(updatedAccount, TransactionType.DEPOSIT, amount, "COMPLETED");
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    @Override
    @Transactional
    public Account withdraw(String username, Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }

        Account account = getAccountByIdAndUser(accountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not accessible."));

        // Add check for account status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active and cannot process withdrawals.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);

        // Ensure status is set to "COMPLETED"
        Transaction transaction = new Transaction(updatedAccount, TransactionType.WITHDRAWAL, amount, "COMPLETED");
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    @Override
    @Transactional
    public void transferFunds(String username, Long fromAccountId, String toAccountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        Account fromAccount = getAccountByIdAndUser(fromAccountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found or not accessible."));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found."));

        // Add check for account status
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active and cannot initiate transfers.");
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination account is not active and cannot receive transfers.");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        // Ensure status is set to "COMPLETED" for both transfer transactions
        Transaction fromTransaction = new Transaction(fromAccount, TransactionType.TRANSFER_OUT, amount, toAccount.getAccountNumber(), "COMPLETED");
        transactionRepository.save(fromTransaction);

        Transaction toTransaction = new Transaction(toAccount, TransactionType.TRANSFER_IN, amount, fromAccount.getAccountNumber(), "COMPLETED");
        transactionRepository.save(toTransaction);
    }

    private String generateAccountNumber() {
        // This generates a simple 10-character account number.
        // For production, consider a more robust and unique generation strategy.
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }


    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getAccountByIdAdmin(Long accountId) {
        return accountRepository.findById(accountId);
    }

    // --- NEW Methods for Staff Dashboard ---
    @Override
    @Transactional
    public void freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new RuntimeException("Account with ID: " + accountId + " is already frozen.");
        }
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new RuntimeException("Account with ID: " + accountId + " is already active.");
        }
        account.setStatus(AccountStatus.ACTIVE); // Set back to active
        accountRepository.save(account);
    }
}