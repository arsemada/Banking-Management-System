package com.example.bankingmanagement.service;

import com.example.bankingmanagement.exception.ResourceNotFoundException;
import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.Transaction;
import com.example.bankingmanagement.repository.AccountRepository;
import com.example.bankingmanagement.repository.TransactionRepository;
import com.example.bankingmanagement.payload.response.TransactionDto; // Import the new DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; // Import Collectors for stream operations

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
    public List<TransactionDto> getAllTransactions() {
        // Fetch all transactions
        List<Transaction> transactions = transactionRepository.findAll();

        // Map each Transaction entity to a TransactionDto
        return transactions.stream().map(transaction -> {
            String accountNumber = null;
            String username = null;

            // Safely get account and user details, as 'account' might be null due to initial data or setup
            if (transaction.getAccount() != null) {
                accountNumber = transaction.getAccount().getAccountNumber();
                if (transaction.getAccount().getUser() != null) {
                    username = transaction.getAccount().getUser().getUsername();
                }
            }

            return new TransactionDto(
                    transaction.getId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getRelatedAccountNum(), // Will still be null if not set during creation
                    transaction.getCreatedAt(),
                    transaction.getStatus(),           // Will still be "UNKNOWN" if not set during creation
                    accountNumber,
                    username
            );
        }).collect(Collectors.toList());
    }
}