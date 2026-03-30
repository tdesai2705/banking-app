package com.demo.banking.controller;

import com.demo.banking.model.Account;
import com.demo.banking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean AccountService service;

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
}
