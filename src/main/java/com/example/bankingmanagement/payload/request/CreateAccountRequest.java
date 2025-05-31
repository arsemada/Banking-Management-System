package com.example.bankingmanagement.payload.request;

import com.example.bankingmanagement.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAccountRequest {
    @NotNull(message = "Account type cannot be null")
    private AccountType accountType;

    @NotNull(message = "Initial balance cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Initial balance must be zero or positive")
    private BigDecimal initialBalance;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(AccountType accountType, BigDecimal initialBalance) {
        this.accountType = accountType;
        this.initialBalance = initialBalance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    @Override
    public String toString() {
        return "CreateAccountRequest{" +
                "accountType=" + accountType +
                ", initialBalance=" + initialBalance +
                '}';
    }
}