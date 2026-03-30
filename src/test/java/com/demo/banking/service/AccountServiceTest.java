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
}
