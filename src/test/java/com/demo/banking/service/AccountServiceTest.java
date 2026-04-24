package com.demo.banking.service;

import com.demo.banking.model.Account;
import com.demo.banking.repository.AccountRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository repo;
    @InjectMocks AccountService service;

    private Account sampleAccount() {
        Account a = new Account("Alice", new BigDecimal("500.00"));
        try {
            var f = Account.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(a, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        return a;
    }

    @Test void depositIncreasesBalance() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        Account result = service.deposit(1L, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("600.00"), result.getBalance());
    }

    @Test void withdrawDecreasesBalance() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        Account result = service.withdraw(1L, new BigDecimal("200.00"));
        assertEquals(new BigDecimal("300.00"), result.getBalance());
    }

    @Test void withdrawThrowsOnInsufficientFunds() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        assertThrows(IllegalArgumentException.class,
            () -> service.withdraw(1L, new BigDecimal("1000.00")));
    }

    @Test void depositThrowsOnNegativeAmount() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        assertThrows(IllegalArgumentException.class,
            () -> service.deposit(1L, new BigDecimal("-50.00")));
    }

    @Test void findByIdReturnsEmpty() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertTrue(service.findById(99L).isEmpty());
    }

    @Test void withdrawThrowsOnNegativeAmount() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        assertThrows(IllegalArgumentException.class,
            () -> service.withdraw(1L, new BigDecimal("-50.00")));
    }

    @Test void withdrawThrowsOnNullAmount() {
        // Amount validation happens before repository access
        assertThrows(IllegalArgumentException.class,
            () -> service.withdraw(1L, null));
    }

    @Test void depositThrowsOnNullAmount() {
        // Amount validation happens before repository access
        assertThrows(IllegalArgumentException.class,
            () -> service.deposit(1L, null));
    }

    @Test void withdrawExactBalance() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        Account result = service.withdraw(1L, new BigDecimal("500.00"));
        assertEquals(new BigDecimal("0.00"), result.getBalance());
    }

    @Test void createAccountWithZeroBalance() {
        // Should now throw exception due to minimum balance requirement
        assertThrows(IllegalArgumentException.class,
            () -> service.createAccount("Bob", BigDecimal.ZERO));
    }

    @Test void depositZeroAmount() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        assertThrows(IllegalArgumentException.class,
            () -> service.deposit(1L, BigDecimal.ZERO));
    }

    @Test void withdrawZeroAmount() {
        Account acc = sampleAccount();
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        assertThrows(IllegalArgumentException.class,
            () -> service.withdraw(1L, BigDecimal.ZERO));
    }

    @Test void findAccountByOwnerName() {
        Account acc = sampleAccount();
        when(repo.findByOwnerName("Alice")).thenReturn(java.util.Arrays.asList(acc));
        var accounts = service.findByOwnerName("Alice");
        assertEquals(1, accounts.size());
        assertEquals("Alice", accounts.get(0).getOwnerName());
    }

    @Test void withdrawExceedsLimit() {
        Account acc = new Account("Rich", new BigDecimal("10000.00"));
        try {
            var f = Account.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(acc, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        // Withdrawal over $5000 should fail
        assertThrows(IllegalArgumentException.class,
            () -> service.withdraw(1L, new BigDecimal("5001.00")));
    }

    @Test void depositExceedsMaxBalance() {
        Account acc = new Account("Wealthy", new BigDecimal("999000.00"));
        try {
            var f = Account.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(acc, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        when(repo.findById(1L)).thenReturn(Optional.of(acc));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        // Deposit that would exceed $1,000,000 should fail
        assertThrows(IllegalArgumentException.class,
            () -> service.deposit(1L, new BigDecimal("2000.00")));
    }
}
