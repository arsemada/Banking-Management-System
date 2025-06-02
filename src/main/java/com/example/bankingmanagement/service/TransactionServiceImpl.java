package com.example.bankingmanagement.service;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.Transaction;
import com.example.bankingmanagement.repository.AccountRepository;
import com.example.bankingmanagement.repository.TransactionRepository;
import com.example.bankingmanagement.service.TransactionService; // Correct import for the interface
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<Transaction> getTransactionsByAccountIdAndUser(Long accountId, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (!account.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Account not accessible to current user.");
        }

        return transactionRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}