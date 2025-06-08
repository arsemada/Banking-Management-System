package com.example.bankingmanagement.model;

import com.example.bankingmanagement.model.AccountStatus;
import com.example.bankingmanagement.model.AccountType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // CHANGE THIS DEFAULT TO PENDING
    private AccountStatus status = AccountStatus.PENDING; // Default to PENDING

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    public Account() {
        this.createdAt = LocalDateTime.now();
        // CHANGE THIS DEFAULT TO PENDING
        this.status = AccountStatus.PENDING; // Ensure default status is set
    }

    // You can add a constructor with all fields if Lombok's @AllArgsConstructor isn't fully replacing manual ones
    // Or rely on Lombok if you put @Data or @Getter/@Setter on this class.
    // Assuming you want to keep manual getters/setters as provided in your original.

    // If using Lombok's @Data, you can remove these manual getters/setters.
    // If not, ensure you add the new getStatus() and setStatus() methods.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // --- Getters and Setters for status ---
    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    // Constructor for creating new accounts, ensuring status is set
    public Account(String accountNumber, AccountType accountType, BigDecimal balance, User user) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.user = user;
        this.createdAt = LocalDateTime.now(); // Set creation timestamp
        // CHANGE THIS DEFAULT TO PENDING
        this.status = AccountStatus.PENDING; // Default status for new accounts
    }
}