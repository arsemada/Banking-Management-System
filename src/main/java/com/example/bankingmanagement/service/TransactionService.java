package com.example.bankingmanagement.service;

import com.example.bankingmanagement.model.Transaction;

import java.util.List;

public interface TransactionService {
    List<Transaction> getTransactionsByAccountIdAndUser(Long accountId, String username);

    List<Transaction> getAllTransactions();
}