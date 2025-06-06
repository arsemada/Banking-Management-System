package com.example.bankingmanagement.repository;

import com.example.bankingmanagement.model.Transaction;
import com.example.bankingmanagement.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount(Account account);

    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);



}