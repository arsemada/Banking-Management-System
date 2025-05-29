package com.example.bankingmanagement.repository;

import com.example.bankingmanagement.model.Account;
import com.example.bankingmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Account entity.
 * Provides methods for database operations on Account objects.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUser(User user); // To find account by associated user
    Boolean existsByAccountNumber(String accountNumber);
}
