package com.smartbudget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ============================================================
// TICKET-F064 to F067 (Day 6, Sprint 5) — MockMvc integration tests  [SOLVED]
// ============================================================
//
// WHAT: Integration tests verify the FULL stack end-to-end:
//         HTTP request → Controller → Service → Repository → Database → Response
//       Unlike unit tests (which mock everything), these tests start the REAL
//       Spring Boot application with an H2 database and send actual HTTP
//       requests through the whole filter chain.
//
// WHY:  A controller might be wired correctly but return the wrong JSON
//       format, or the service might silently swallow validation errors —
//       only an integration test catches those bugs before they ship.
//
// PREREQUISITES: TransactionController must be fully implemented
//                (TICKET-F056 to F059), plus TransactionService (F063)
//                and GlobalExceptionHandler (F065) so that error mapping works.
// ============================================================
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------------------
    // TICKET-F064 — smoke test: GET /api/transactions returns 200 + JSON
    // -------------------------------------------------------
    @Test
    void getAll_returns200AndJsonArray() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    // -------------------------------------------------------
    // TICKET-F066 — POST /api/transactions with valid body → 201 + persisted
    // -------------------------------------------------------
    @Test
    void createTransaction_validInput_returns201AndPersists() throws Exception {
        String body = """
                {
                  "user":        {"userId": 1},
                  "category":    {"categoryId": 1},
                  "amount":      100.00,
                  "txnDate":     "2026-05-01",
                  "description": "MockMvc happy path",
                  "type":        "INCOME"
                }
                """;

        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.txnId").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("MockMvc happy path"))
                .andReturn().getResponse().getContentAsString();

        // Round-trip: fetch the freshly-created id and verify it comes back.
        Long newId = new ObjectMapper().readTree(response).get("txnId").asLong();
        mockMvc.perform(get("/api/transactions/" + newId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("MockMvc happy path"));
    }

    // -------------------------------------------------------
    // TICKET-F067 — POST with a negative amount → 400 + explanatory message
    // -------------------------------------------------------
    @Test
    void createTransaction_negativeAmount_returns400() throws Exception {
        String body = """
                {
                  "user":        {"userId": 1},
                  "category":    {"categoryId": 1},
                  "amount":      -50.00,
                  "txnDate":     "2026-05-01",
                  "description": "should fail",
                  "type":        "EXPENSE"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("amount")));
    }

    // -------------------------------------------------------
    // TICKET-F067 — POST referencing a non-existent user → 404
    // -------------------------------------------------------
    @Test
    void createTransaction_missingUser_returns404() throws Exception {
        String body = """
                {
                  "user":        {"userId": 9999},
                  "category":    {"categoryId": 1},
                  "amount":      50.00,
                  "txnDate":     "2026-05-01",
                  "description": "missing user",
                  "type":        "EXPENSE"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User 9999")));
    }

    // -------------------------------------------------------
    // TICKET-F067 — POST with an unknown type value → 400
    // -------------------------------------------------------
    @Test
    void createTransaction_invalidType_returns400() throws Exception {
        String body = """
                {
                  "user":        {"userId": 1},
                  "category":    {"categoryId": 1},
                  "amount":      50.00,
                  "txnDate":     "2026-05-01",
                  "description": "oops",
                  "type":        "BOGUS"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("type")));
    }

    // -------------------------------------------------------
    // TICKET-F066 — GET /api/transactions/user/{userId} returns that user's rows
    // -------------------------------------------------------
    @Test
    void getTransactionsByUser_returnsNonEmptyArray() throws Exception {
        mockMvc.perform(get("/api/transactions/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }
}
