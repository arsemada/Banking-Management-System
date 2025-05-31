package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.AccountStatus;
import com.example.bankingmanagement.model.AccountType;
import com.example.bankingmanagement.model.User;
import com.example.bankingmanagement.repository.AccountRepository;
import com.example.bankingmanagement.repository.UserRepository;
import com.example.bankingmanagement.security.services.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found in database."));
    }

    @Transactional
    public Account createAccount(AccountType accountType) {
        User currentUser = getCurrentAuthenticatedUser();

        String accountNumber = generateUniqueAccountNumber();
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateUniqueAccountNumber();
        }

        Account newAccount = new Account();
        newAccount.setAccountNumber(accountNumber);
        newAccount.setAccountType(accountType);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setStatus(AccountStatus.ACTIVE);
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUser(currentUser);

        return accountRepository.save(newAccount);
    }

    public Optional<Account> getAccountById(Long accountId) {
        User currentUser = getCurrentAuthenticatedUser();
        return accountRepository.findById(accountId)
                .filter(account -> account.getUser().getId().equals(currentUser.getId()));
    }

    public List<Account> getMyAccounts() {
        User currentUser = getCurrentAuthenticatedUser();
        return accountRepository.findByUser(currentUser);
    }

    private String generateUniqueAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}