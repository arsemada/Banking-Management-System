package com.example.bankingmanagement.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // For handling bidirectional relationships

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

    @Column(nullable = false, precision = 19, scale = 2) // Recommended for currency
    private BigDecimal balance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Many-to-one relationship with User
    // --- IMPORTANT CHANGE: fetch = FetchType.EAGER ---
    @ManyToOne(fetch = FetchType.EAGER) // Changed from LAZY to EAGER to ensure User is loaded for admin view
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column
    @JsonBackReference // Prevents infinite recursion in JSON serialization
    private User user;

    // Getters and Setters

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
}