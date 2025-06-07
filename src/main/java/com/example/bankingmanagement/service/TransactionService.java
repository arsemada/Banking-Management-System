package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.Transaction;
import com.example.bankingmanagement.payload.response.TransactionDto; // Import the new DTO

import java.util.List;

public interface TransactionService {
    List<Transaction> getTransactionsByAccountIdAndUser(Long accountId, String username);

    // Changed return type to List<TransactionDto>
    List<TransactionDto> getAllTransactions();
}