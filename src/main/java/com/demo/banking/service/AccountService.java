package com.demo.banking.service;

import com.demo.banking.model.Account;
import com.demo.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository repo;

    public AccountService(AccountRepository repo) { this.repo = repo; }

    public List<Account> findAll() { return repo.findAll(); }

    public Optional<Account> findById(Long id) { return repo.findById(id); }

    public Account create(Account account) { return repo.save(account); }

    @Transactional
    public Account deposit(Long id, BigDecimal amount) {
        Account acc = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive");
        acc.setBalance(acc.getBalance().add(amount));
        return repo.save(acc);
    }

    @Transactional
    public Account withdraw(Long id, BigDecimal amount) {
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
