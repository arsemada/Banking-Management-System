package com.example.bankingmanagement.repository;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    Optional<Account> findByAccountNumber(String accountNumber);
}