package com.demo.banking.service;

import com.demo.banking.model.Account;
import com.demo.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository repo;

    public AccountService(AccountRepository repo) { this.repo = repo; }

    public List<Account> findAll() { return repo.findAll(); }

    public Optional<Account> findById(Long id) { return repo.findById(id); }

    public Account create(Account account) { return repo.save(account); }

    public Account createAccount(String ownerName, BigDecimal balance) {
        // Validate minimum initial balance
        BigDecimal minimumBalance = new BigDecimal("100.00");
        if (balance.compareTo(minimumBalance) < 0) {
            throw new IllegalArgumentException("Initial balance must be at least $100.00");
        }
        Account account = new Account(ownerName, balance);
        // Generate a unique account number
        account.setAccountNumber("ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return repo.save(account);
    }

    public List<Account> findByOwnerName(String ownerName) {
        return repo.findByOwnerName(ownerName);
    }

    @Transactional
    public Account deposit(Long id, BigDecimal amount) {
        if (amount == null)
            throw new IllegalArgumentException("Deposit amount cannot be null");
        Account acc = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive");
        acc.setBalance(acc.getBalance().add(amount));
        return repo.save(acc);
    }

    @Transactional
    public Account withdraw(Long id, BigDecimal amount) {
        if (amount == null)
            throw new IllegalArgumentException("Withdrawal amount cannot be null");
        Account acc = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        if (acc.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient funds");
        acc.setBalance(acc.getBalance().subtract(amount));
        return repo.save(acc);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
