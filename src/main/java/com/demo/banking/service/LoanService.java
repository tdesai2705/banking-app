package com.demo.banking.service;

import com.demo.banking.model.Loan;
import com.demo.banking.model.User;
import com.demo.banking.repository.LoanRepository;
import com.demo.banking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Loan applyForLoan(Long userId, Double amount, Integer termMonths) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Validate amount
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }

        // Validate term
        if (termMonths == null || termMonths <= 0) {
            throw new IllegalArgumentException("Loan term must be greater than zero");
        }

        // Create loan application
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setAmount(amount);
        loan.setInterestRate(5.0); // Default interest rate
        loan.setTermMonths(termMonths);
        loan.setStatus("PENDING");
        loan.setAppliedAt(LocalDateTime.now());

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with id: " + loanId));

        loan.setStatus("APPROVED");
        loan.setApprovedAt(LocalDateTime.now());

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with id: " + loanId));

        loan.setStatus("REJECTED");
        loan.setRejectionReason(reason);

        return loanRepository.save(loan);
    }

    public Double calculateMonthlyPayment(Double amount, Double annualInterestRate, Integer termMonths) {
        if (amount <= 0 || termMonths <= 0) {
            throw new IllegalArgumentException("Amount and term must be greater than zero");
        }

        // Convert annual rate to monthly rate
        Double monthlyRate = annualInterestRate / 100 / 12;

        // Calculate monthly payment using formula: M = P * [r(1+r)^n] / [(1+r)^n - 1]
        if (monthlyRate == 0) {
            return amount / termMonths;
        }

        Double monthlyPayment = amount * (monthlyRate * Math.pow(1 + monthlyRate, termMonths)) /
                (Math.pow(1 + monthlyRate, termMonths) - 1);

        return Math.round(monthlyPayment * 100.0) / 100.0; // Round to 2 decimal places
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public Loan getLoanById(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with id: " + loanId));
    }
}
