package com.demo.banking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String accountNumber;

    @NotBlank
    private String ownerName;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal balance;

    public Account() {}

    public Account(String ownerName, BigDecimal balance) {
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }

    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getOwnerName() { return ownerName; }

    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }

    // Helper method to get balance as Double (for compatibility)
    public Double getBalanceAsDouble() {
        return balance != null ? balance.doubleValue() : 0.0;
    }

    // Helper method to set balance from Double
    public void setBalance(Double balance) {
        this.balance = balance != null ? BigDecimal.valueOf(balance) : BigDecimal.ZERO;
    }
}
