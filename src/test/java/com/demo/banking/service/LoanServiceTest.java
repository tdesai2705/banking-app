package com.demo.banking.service;

import com.demo.banking.model.Loan;
import com.demo.banking.model.User;
import com.demo.banking.repository.LoanRepository;
import com.demo.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService loanService;

    private User testUser;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");

        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setUserId(1L);
        testLoan.setAmount(10000.0);
        testLoan.setInterestRate(5.0);
        testLoan.setTermMonths(12);
        testLoan.setStatus("PENDING");
    }

    @Test
    void testApplyForLoan_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Loan loan = loanService.applyForLoan(1L, 10000.0, 12);

        // Assert
        assertNotNull(loan);
        assertEquals(10000.0, loan.getAmount());
        assertEquals("PENDING", loan.getStatus());
        // Interest rate should be 3.5% base + 0.5% for amount over $5k = 4.0%
        assertEquals(4.0, loan.getInterestRate());
        assertEquals(12, loan.getTermMonths());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testApplyForLoan_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, 10000.0, 12);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApplyForLoan_InvalidAmount() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, -1000.0, 12);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApplyForLoan_InvalidTerm() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, 10000.0, 0);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApproveLoan_Success() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        // Act
        Loan approvedLoan = loanService.approveLoan(1L);

        // Assert
        assertNotNull(approvedLoan);
        assertEquals("APPROVED", testLoan.getStatus());
        verify(loanRepository, times(1)).save(testLoan);
    }

    @Test
    void testApproveLoan_LoanNotFound() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.approveLoan(1L);
        });
    }

    @Test
    void testRejectLoan_Success() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);

        // Act
        Loan rejectedLoan = loanService.rejectLoan(1L, "Insufficient credit score");

        // Assert
        assertNotNull(rejectedLoan);
        assertEquals("REJECTED", testLoan.getStatus());
        assertEquals("Insufficient credit score", testLoan.getRejectionReason());
        verify(loanRepository, times(1)).save(testLoan);
    }

    @Test
    void testCalculateMonthlyPayment_Success() {
        // Act
        Double monthlyPayment = loanService.calculateMonthlyPayment(10000.0, 5.0, 12);

        // Assert
        assertNotNull(monthlyPayment);
        assertTrue(monthlyPayment > 0);
        assertTrue(monthlyPayment > 833.33); // Should be more than principal/months due to interest
    }

    @Test
    void testGetLoansByUserId_Success() {
        // Arrange
        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setUserId(1L);
        loan2.setAmount(5000.0);

        List<Loan> loans = Arrays.asList(testLoan, loan2);
        when(loanRepository.findByUserId(1L)).thenReturn(loans);

        // Act
        List<Loan> result = loanService.getLoansByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10000.0, result.get(0).getAmount());
        assertEquals(5000.0, result.get(1).getAmount());
    }

    @Test
    void testGetLoanById_Success() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));

        // Act
        Loan loan = loanService.getLoanById(1L);

        // Assert
        assertNotNull(loan);
        assertEquals(1L, loan.getId());
        assertEquals(10000.0, loan.getAmount());
    }

    @Test
    void testApplyForLoan_AmountTooLow() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, 500.0, 12);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApplyForLoan_AmountTooHigh() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, 1500000.0, 12);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApplyForLoan_TermTooShort() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, 10000.0, 3);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApplyForLoan_TermTooLong() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.applyForLoan(1L, 10000.0, 400);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }
}
