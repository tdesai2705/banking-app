package com.demo.banking.controller;

import com.demo.banking.model.Account;
import com.demo.banking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean AccountService service;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account("Test Owner", new BigDecimal("1000.00"));
        try {
            var f = Account.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(testAccount, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test void listReturnsAccounts() throws Exception {
        when(service.findAll()).thenReturn(List.of(new Account("Bob", BigDecimal.TEN)));
        mvc.perform(get("/api/accounts"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].ownerName").value("Bob"));
    }

    @Test void getReturns404WhenMissing() throws Exception {
        when(service.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(get("/api/accounts/99")).andExpect(status().isNotFound());
    }

    @Test void createAccount() throws Exception {
        Account a = new Account("Carol", new BigDecimal("250.00"));
        when(service.create(any())).thenReturn(a);
        mvc.perform(post("/api/accounts")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(a)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.ownerName").value("Carol"));
    }

    @Test void getAccountByIdReturnsAccount() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(testAccount));
        mvc.perform(get("/api/accounts/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.ownerName").value("Test Owner"))
           .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test void depositIncreasesBalance() throws Exception {
        Account updated = new Account("Test Owner", new BigDecimal("1500.00"));
        when(service.deposit(eq(1L), any(BigDecimal.class))).thenReturn(updated);

        Map<String, BigDecimal> depositRequest = new HashMap<>();
        depositRequest.put("amount", new BigDecimal("500.00"));

        mvc.perform(post("/api/accounts/1/deposit")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(depositRequest)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test void withdrawDecreasesBalance() throws Exception {
        Account updated = new Account("Test Owner", new BigDecimal("700.00"));
        when(service.withdraw(eq(1L), any(BigDecimal.class))).thenReturn(updated);

        Map<String, BigDecimal> withdrawRequest = new HashMap<>();
        withdrawRequest.put("amount", new BigDecimal("300.00"));

        mvc.perform(post("/api/accounts/1/withdraw")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(withdrawRequest)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.balance").value(700.00));
    }

    @Test void deleteAccountReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/accounts/1"))
           .andExpect(status().isNoContent());
    }

    @Test void depositWithNegativeAmountThrowsError() throws Exception {
        when(service.deposit(eq(1L), any(BigDecimal.class)))
            .thenThrow(new IllegalArgumentException("Deposit amount must be positive"));

        Map<String, BigDecimal> depositRequest = new HashMap<>();
        depositRequest.put("amount", new BigDecimal("-100.00"));

        mvc.perform(post("/api/accounts/1/deposit")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(depositRequest)))
           .andExpect(status().is5xxServerError());
    }

    @Test void withdrawWithInsufficientFundsThrowsError() throws Exception {
        when(service.withdraw(eq(1L), any(BigDecimal.class)))
            .thenThrow(new IllegalArgumentException("Insufficient funds"));

        Map<String, BigDecimal> withdrawRequest = new HashMap<>();
        withdrawRequest.put("amount", new BigDecimal("5000.00"));

        mvc.perform(post("/api/accounts/1/withdraw")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(withdrawRequest)))
           .andExpect(status().is5xxServerError());
    }
}
