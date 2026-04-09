package com.demo.banking.service;

import com.demo.banking.model.Account;
import com.demo.banking.model.Transaction;
import com.demo.banking.repository.AccountRepository;
import com.demo.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("ACC001");
        sourceAccount.setBalance(new BigDecimal("1000.0"));

        destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("ACC002");
        destinationAccount.setBalance(new BigDecimal("500.0"));
    }

    @Test
    void testTransferMoney_Success() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Transaction transaction = transactionService.transferMoney("ACC001", "ACC002", 200.0);

        // Assert
        assertNotNull(transaction);
        assertEquals(new BigDecimal("800.0"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("700.0"), destinationAccount.getBalance());
        assertEquals("TRANSFER", transaction.getType());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testTransferMoney_InsufficientBalance() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(destinationAccount));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transferMoney("ACC001", "ACC002", 1500.0);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testTransferMoney_SourceAccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transferMoney("ACC001", "ACC002", 200.0);
        });
    }

    @Test
    void testTransferMoney_DestinationAccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transferMoney("ACC001", "ACC002", 200.0);
        });
    }

    @Test
    void testTransferMoney_NegativeAmount() {
        // Act & Assert - amount validation happens before repository access
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transferMoney("ACC001", "ACC002", -100.0);
        });
    }

    @Test
    void testTransferMoney_ZeroAmount() {
        // Act & Assert - amount validation happens before repository access
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transferMoney("ACC001", "ACC002", 0.0);
        });
    }

    @Test
    void testDeposit_Success() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Transaction transaction = transactionService.deposit("ACC001", 500.0);

        // Assert
        assertNotNull(transaction);
        assertEquals(new BigDecimal("1500.0"), sourceAccount.getBalance());
        assertEquals("DEPOSIT", transaction.getType());
        assertEquals(500.0, transaction.getAmount());
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testDeposit_AccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.deposit("ACC001", 500.0);
        });
    }

    @Test
    void testDeposit_NegativeAmount() {
        // Act & Assert - amount validation happens before repository access
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.deposit("ACC001", -100.0);
        });
    }

    @Test
    void testWithdraw_Success() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Transaction transaction = transactionService.withdraw("ACC001", 300.0);

        // Assert
        assertNotNull(transaction);
        assertEquals(new BigDecimal("700.0"), sourceAccount.getBalance());
        assertEquals("WITHDRAWAL", transaction.getType());
        assertEquals(300.0, transaction.getAmount());
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(sourceAccount));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdraw("ACC001", 1500.0);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_AccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdraw("ACC001", 300.0);
        });
    }

    @Test
    void testGetAccountTransactions_Success() {
        // Arrange
        Transaction tx1 = new Transaction();
        tx1.setId(1L);
        tx1.setAccountNumber("ACC001");
        tx1.setType("DEPOSIT");
        tx1.setAmount(500.0);

        Transaction tx2 = new Transaction();
        tx2.setId(2L);
        tx2.setAccountNumber("ACC001");
        tx2.setType("WITHDRAWAL");
        tx2.setAmount(200.0);

        List<Transaction> transactions = Arrays.asList(tx1, tx2);
        when(transactionRepository.findByAccountNumberOrderByTimestampDesc("ACC001")).thenReturn(transactions);

        // Act
        List<Transaction> result = transactionService.getAccountTransactions("ACC001");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("DEPOSIT", result.get(0).getType());
        assertEquals("WITHDRAWAL", result.get(1).getType());
    }

    @Test
    void testGetAccountTransactions_EmptyList() {
        // Arrange
        when(transactionRepository.findByAccountNumberOrderByTimestampDesc("ACC001")).thenReturn(Arrays.asList());

        // Act
        List<Transaction> result = transactionService.getAccountTransactions("ACC001");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTransactionById_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountNumber("ACC001");
        transaction.setType("DEPOSIT");
        transaction.setAmount(500.0);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // Act
        Transaction result = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DEPOSIT", result.getType());
    }

    @Test
    void testGetTransactionById_NotFound() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.getTransactionById(1L);
        });
    }
}
