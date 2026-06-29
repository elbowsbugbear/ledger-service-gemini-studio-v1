package com.budgetor.ledger;

import com.budgetor.ledger.domain.TemporalType;
import com.budgetor.ledger.domain.Transaction;
import com.budgetor.ledger.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class LedgerServiceApplicationTests {

    // Define the PostgreSQL Container with exact version matching production (16 Alpine)
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("budgetor_ledger_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Dynamically override Spring's datasource properties with the Testcontainers random port
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @AfterEach
    void cleanUp() {
        // Ensure clean state between test runs
        transactionRepository.deleteAll();
    }

    /**
     * Test case 1: Successful creation of a projected ledger transaction.
     * Verifies 201 Created and asserts that the transaction is committed to the MySQL database.
     */
    @Test
    void shouldCreateTransactionSuccessfully() throws Exception {
        Map<String, Object> requestDTO = new HashMap<>();
        requestDTO.put("description", "AWS Monthly Cloud Services Hosting");
        requestDTO.put("amount", 249.99);
        requestDTO.put("temporalType", "PROJECTED");
        requestDTO.put("transactionTimestamp", LocalDateTime.now().plusDays(5).toString());

        mockMvc.perform(post("/api/v1/ledger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is("AWS Monthly Cloud Services Hosting")))
                .andExpect(jsonPath("$.amount", is(249.99)))
                .andExpect(jsonPath("$.temporalType", is("PROJECTED")));

        // Physically verify in the Testcontainer database that record was written
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        Transaction persisted = transactions.get(0);
        assertThat(persisted.getDescription()).isEqualTo("AWS Monthly Cloud Services Hosting");
        assertThat(persisted.getAmount()).isEqualByComparingTo(new BigDecimal("249.99"));
        assertThat(persisted.getTemporalType()).isEqualTo(TemporalType.PROJECTED);
    }

    /**
     * Test case 2: Business Rule Exception check.
     * An ACTUAL temporal transaction scheduled in the future must fail with HTTP 400 Bad Request
     * and present structured validation errors handled by the GlobalExceptionHandler.
     */
    @Test
    void shouldFailWhenActualTransactionHasFutureTimestamp() throws Exception {
        Map<String, Object> requestDTO = new HashMap<>();
        requestDTO.put("description", "Office Lunch Reimbursement");
        requestDTO.put("amount", 85.50);
        requestDTO.put("temporalType", "ACTUAL");
        // An ACTUAL transaction scheduled in the future (violating our business rule)
        requestDTO.put("transactionTimestamp", LocalDateTime.now().plusDays(2).toString());

        mockMvc.perform(post("/api/v1/ledger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Business Rule Violation")))
                .andExpect(jsonPath("$.message", containsString("An ACTUAL transaction cannot be scheduled with a future timestamp")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        // Verify that nothing was written to the MySQL test container database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).isEmpty();
    }
}
