package com.example.bankingmanagement.payload.response;

import com.example.bankingmanagement.model.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // Keep this
public class TransactionDto {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String relatedAccountNum; // For transfers
    private LocalDateTime createdAt;
    private String status;
    private String accountNumber; // From the associated Account
    private String username;      // From the associated User of the Account

    public TransactionDto(Long id, TransactionType type, BigDecimal amount, String relatedAccountNum,
                          LocalDateTime createdAt, String status, String accountNumber, String username) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.relatedAccountNum = relatedAccountNum;
        this.createdAt = createdAt;
        this.status = status;
        this.accountNumber = accountNumber;
        this.username = username;
    }
}