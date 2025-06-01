package com.example.bankingmanagement.service;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.Account;
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

    @Override
    @Transactional
    public Account deposit(String username, Long accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        Account account = getAccountByIdAndUser(accountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found or not accessible."));

        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(updatedAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
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

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(updatedAccount);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transactionRepository.save(transaction);

        return updatedAccount;
    }

    @Override
    @Transactional // Ensures both debit and credit happen or none do
    public void transferFunds(String username, Long fromAccountId, String toAccountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        // 1. Get the source account and verify ownership
        Account fromAccount = getAccountByIdAndUser(fromAccountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found or not accessible."));

        // 2. Get the destination account by account number (can be any user's account)
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found."));

        // Prevent transfer to self (same account ID) unless specifically allowed
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }

        // 3. Check for sufficient funds in the source account
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer.");
        }

        // 4. Perform the debit from the source account
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        // 5. Perform the credit to the destination account
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        // 6. Record transactions for both accounts
        Transaction fromTransaction = new Transaction();
        fromTransaction.setAccount(fromAccount);
        fromTransaction.setAmount(amount);
        fromTransaction.setType(TransactionType.TRANSFER_OUT);
        transactionRepository.save(fromTransaction);

        Transaction toTransaction = new Transaction();
        toTransaction.setAccount(toAccount);
        toTransaction.setAmount(amount);
        toTransaction.setType(TransactionType.TRANSFER_IN);
        transactionRepository.save(toTransaction);
    }

    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}