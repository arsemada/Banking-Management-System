package com.example.bankingmanagement.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransferRequest {

    @NotNull(message = "Source account ID cannot be null")
    private Long fromAccountId;

    @NotBlank(message = "Destination account number cannot be empty")
    @Size(min = 10, max = 12, message = "Account number must be between 10 and 12 characters") // Adjust based on your accountNumber generation
    private String toAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be positive")
    @Digits(integer=17, fraction=2, message = "Amount format invalid")
    private BigDecimal amount;

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}