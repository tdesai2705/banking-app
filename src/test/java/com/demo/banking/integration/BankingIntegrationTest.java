package com.demo.banking.integration;

import com.demo.banking.model.Account;
import com.demo.banking.model.Transaction;
import com.demo.banking.model.User;
import com.demo.banking.repository.AccountRepository;
import com.demo.banking.repository.TransactionRepository;
import com.demo.banking.repository.UserRepository;
import com.demo.banking.service.AccountService;
import com.demo.banking.service.TransactionService;
import com.demo.banking.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankingIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Account testAccount1;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setEmail("integration@test.com");
        testUser.setPassword("password123");
        testUser.setFullName("Integration Test User");
        testUser = userRepository.save(testUser);

        // Create test accounts
        testAccount1 = new Account("Integration Owner 1", new BigDecimal("1000.00"));
        testAccount1.setAccountNumber("INT001");
        testAccount1 = accountRepository.save(testAccount1);

        testAccount2 = new Account("Integration Owner 2", new BigDecimal("500.00"));
        testAccount2.setAccountNumber("INT002");
        testAccount2 = accountRepository.save(testAccount2);
    }

    @Test
    void testCompleteTransferFlow() {
        // Execute transfer
        Transaction transaction = transactionService.transferMoney("INT001", "INT002", 200.0);

        // Verify transaction was created
        assertNotNull(transaction);
        assertEquals("TRANSFER", transaction.getType());
        assertEquals(200.0, transaction.getAmount());

        // Verify balances were updated
        Account updatedAccount1 = accountRepository.findByAccountNumber("INT001").orElseThrow();
        Account updatedAccount2 = accountRepository.findByAccountNumber("INT002").orElseThrow();

        assertEquals(800.0, updatedAccount1.getBalanceAsDouble());
        assertEquals(700.0, updatedAccount2.getBalanceAsDouble());

        // Verify transaction history
        List<Transaction> account1Transactions = transactionService.getAccountTransactions("INT001");
        assertFalse(account1Transactions.isEmpty());
        assertEquals("INT001", account1Transactions.get(0).getAccountNumber());
    }

    @Test
    void testMultipleDepositsAndWithdrawals() {
        // Perform multiple deposits
        accountService.deposit(testAccount1.getId(), new BigDecimal("100.00"));
        accountService.deposit(testAccount1.getId(), new BigDecimal("50.00"));

        // Perform withdrawals
        accountService.withdraw(testAccount1.getId(), new BigDecimal("200.00"));

        // Verify final balance
        Account updated = accountRepository.findById(testAccount1.getId()).orElseThrow();
        assertEquals(new BigDecimal("950.00"), updated.getBalance());
    }

    @Test
    void testCreateUserAndAccount() {
        // Create new user
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@test.com");
        newUser.setPassword("pass123");
        newUser.setFullName("New User");
        User createdUser = userService.createUser(
            newUser.getUsername(),
            newUser.getEmail(),
            newUser.getPassword(),
            newUser.getFullName()
        );

        assertNotNull(createdUser.getId());
        assertEquals("newuser", createdUser.getUsername());

        // Create account for user
        Account newAccount = accountService.createAccount("New User", new BigDecimal("1500.00"));
        assertNotNull(newAccount.getId());
        assertEquals("New User", newAccount.getOwnerName());
        assertEquals(new BigDecimal("1500.00"), newAccount.getBalance());
    }

    @Test
    void testTransferWithInsufficientFundsRollback() {
        BigDecimal initialBalance1 = testAccount1.getBalance();
        BigDecimal initialBalance2 = testAccount2.getBalance();

        // Attempt transfer with insufficient funds
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.transferMoney("INT001", "INT002", 5000.0);
        });

        // Verify no changes were made (transaction rolled back)
        Account account1 = accountRepository.findById(testAccount1.getId()).orElseThrow();
        Account account2 = accountRepository.findById(testAccount2.getId()).orElseThrow();

        assertEquals(initialBalance1, account1.getBalance());
        assertEquals(initialBalance2, account2.getBalance());

        // Verify no transaction record was created
        List<Transaction> transactions = transactionRepository.findByAccountNumberOrderByTimestampDesc("INT001");
        assertEquals(0, transactions.size());
    }

    @Test
    void testFindAccountsByOwnerName() {
        // Create multiple accounts for same owner
        accountService.createAccount("John Doe", new BigDecimal("100.00"));
        accountService.createAccount("John Doe", new BigDecimal("200.00"));
        accountService.createAccount("Jane Smith", new BigDecimal("300.00"));

        // Find accounts by owner
        List<Account> johnAccounts = accountService.findByOwnerName("John Doe");
        assertEquals(2, johnAccounts.size());
        assertTrue(johnAccounts.stream().allMatch(a -> a.getOwnerName().equals("John Doe")));

        List<Account> janeAccounts = accountService.findByOwnerName("Jane Smith");
        assertEquals(1, janeAccounts.size());
    }

    @Test
    void testUserUpdateAndRetrieve() {
        // Update user details
        testUser.setFullName("Updated Name");
        testUser.setEmail("updated@test.com");
        User updated = userService.updateUser(
            testUser.getId(),
            testUser.getEmail(),
            testUser.getFullName()
        );

        assertEquals("Updated Name", updated.getFullName());
        assertEquals("updated@test.com", updated.getEmail());

        // Retrieve and verify
        User retrieved = userService.getUserById(testUser.getId());
        assertEquals("Updated Name", retrieved.getFullName());
        assertEquals("updated@test.com", retrieved.getEmail());
    }

    @Test
    void testDepositAndWithdrawSequence() {
        Long accountId = testAccount1.getId();
        BigDecimal startBalance = testAccount1.getBalance();

        // Sequence of operations
        accountService.deposit(accountId, new BigDecimal("250.00"));
        accountService.withdraw(accountId, new BigDecimal("100.00"));
        accountService.deposit(accountId, new BigDecimal("50.00"));

        // Verify final balance
        Account finalAccount = accountRepository.findById(accountId).orElseThrow();
        BigDecimal expectedBalance = startBalance.add(new BigDecimal("250.00"))
                .subtract(new BigDecimal("100.00"))
                .add(new BigDecimal("50.00"));
        assertEquals(expectedBalance, finalAccount.getBalance());
    }

    @Test
    void testTransactionHistoryOrdering() {
        // Create multiple transactions
        transactionService.deposit("INT001", 100.0);
        transactionService.withdraw("INT001", 50.0);
        transactionService.deposit("INT001", 75.0);

        // Verify transaction history is ordered by timestamp descending
        List<Transaction> history = transactionService.getAccountTransactions("INT001");
        assertEquals(3, history.size());

        // Most recent should be first
        assertEquals("DEPOSIT", history.get(0).getType());
        assertEquals(75.0, history.get(0).getAmount());
    }

    @Test
    void testDeleteUserAndVerifyRemoval() {
        // Create and then delete user
        User newUser = new User();
        newUser.setUsername("tempuser");
        newUser.setEmail("temp@test.com");
        newUser.setPassword("temp123");
        newUser.setFullName("Temp User");
        User created = userService.createUser(
            newUser.getUsername(),
            newUser.getEmail(),
            newUser.getPassword(),
            newUser.getFullName()
        );

        Long userId = created.getId();
        userService.deleteUser(userId);

        // Verify user no longer exists
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(userId);
        });
    }

    @Test
    void testConcurrentAccountOperations() {
        Long accountId = testAccount1.getId();

        // Get initial balance from database (in case other tests modified it)
        Account initialAccount = accountRepository.findById(accountId).orElseThrow();
        BigDecimal initialBalance = initialAccount.getBalance();

        // Simulate multiple operations that should maintain consistency
        accountService.deposit(accountId, new BigDecimal("100.00"));
        Account intermediate = accountRepository.findById(accountId).orElseThrow();
        accountService.withdraw(accountId, new BigDecimal("50.00"));

        // Verify final state is consistent
        Account finalAccount = accountRepository.findById(accountId).orElseThrow();
        BigDecimal expected = initialBalance
                .add(new BigDecimal("100.00"))
                .subtract(new BigDecimal("50.00"));
        assertEquals(expected, finalAccount.getBalance());
    }
}
