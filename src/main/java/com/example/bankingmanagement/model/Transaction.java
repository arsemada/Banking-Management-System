package com.example.bankingmanagement.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private Account account; // The primary account involved in the transaction

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    // For transfers, this stores the account number of the other party
    private String relatedAccountNum;

    // This is often for the database record creation time
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- ADD THIS FIELD FOR THE ACTUAL TRANSACTION DATE ---
    @Column(name = "transaction_date", nullable = false, updatable = false) // Add this column mapping
    private LocalDateTime transactionDate; // New field

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // You can also set transactionDate here if it always matches createdAt
        // However, if transactionDate can be different (e.g., historical transactions),
        // it's better to set it in the constructor or service layer.
        // For simplicity, let's set it here for now if not set elsewhere.
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    @Column(nullable = false)
    private String status; // e.g., "COMPLETED", "PENDING", "FAILED"

    public Transaction() {
        // Ensure transactionDate is also initialized for new objects if not done by @PrePersist reliably
        this.transactionDate = LocalDateTime.now();
    }

    // Constructor for simple transactions (deposit/withdrawal)
    public Transaction(Account account, TransactionType type, BigDecimal amount, String status) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.transactionDate = LocalDateTime.now(); // <--- Initialize here!
    }

    // Constructor for transfers (with related account number)
    public Transaction(Account account, TransactionType type, BigDecimal amount, String relatedAccountNum, String status) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.relatedAccountNum = relatedAccountNum;
        this.status = status;
        this.transactionDate = LocalDateTime.now(); // <--- Initialize here!
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- NEW: Getter and Setter for transactionDate ---
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    // --- END NEW ---

    public String getRelatedAccountNum() {
        return relatedAccountNum;
    }

    public void setRelatedAccountNum(String relatedAccountNum) {
        this.relatedAccountNum = relatedAccountNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}