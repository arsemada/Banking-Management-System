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

    // --- ENSURE THIS IS FetchType.EAGER ---
    @ManyToOne(fetch = FetchType.EAGER) // This must be EAGER to load the Account and its User
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private Account account; // The primary account involved in the transaction

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    private String relatedAccountNum;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    private String status;

    // --- Constructors (as previously updated) ---
    public Transaction() {}

    public Transaction(Account account, TransactionType type, BigDecimal amount, String status) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.status = status;
    }

    public Transaction(Account account, TransactionType type, BigDecimal amount, String relatedAccountNum, String status) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.relatedAccountNum = relatedAccountNum;
        this.status = status;
    }

    // --- Getters and Setters (as previously updated) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRelatedAccountNum() { return relatedAccountNum; }
    public void setRelatedAccountNum(String relatedAccountNum) { this.relatedAccountNum = relatedAccountNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}