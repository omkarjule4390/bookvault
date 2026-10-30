package com.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarymanagement.dto.BorrowRequest;
import com.librarymanagement.dto.BorrowResponse;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.service.BorrowService;
import com.librarymanagement.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=testpass"})
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowService borrowService;

    @Test
    @DisplayName("POST /api/v1/borrow - Should return 201 when borrow request is valid")
    void borrowBook_Valid_Returns201() throws Exception {
        BorrowRequest request = BorrowRequest.builder()
                .bookId(1L)
                .memberId(1L)
                .borrowDays(14)
                .remarks("Research project")
                .build();

        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookId(1L)
                .bookTitle("Clean Code")
                .memberId(1L)
                .memberName("Alice Johnson")
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.BORROWED)
                .fineAmount(BigDecimal.ZERO)
                .remarks("Research project")
                .build();

        when(borrowService.borrowBook(any(BorrowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }

    @Test
    @DisplayName("POST /api/v1/borrow - Should return 400 when missing required IDs")
    void borrowBook_MissingIds_Returns400() throws Exception {
        BorrowRequest request = BorrowRequest.builder()
                .borrowDays(14)
                .build(); // Missing bookId and memberId

        mockMvc.perform(post("/api/v1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.bookId").exists())
                .andExpect(jsonPath("$.fieldErrors.memberId").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/borrow/{id}/return - Should return 200 on successful return")
    void returnBook_Returns200() throws Exception {
        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookId(1L)
                .bookTitle("Clean Code")
                .memberId(1L)
                .memberName("Alice Johnson")
                .borrowDate(LocalDate.now().minusDays(7))
                .dueDate(LocalDate.now().plusDays(7))
                .returnDate(LocalDate.now())
                .status(BorrowStatus.RETURNED)
                .fineAmount(BigDecimal.ZERO)
                .build();

        when(borrowService.returnBook(1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/borrow/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").value(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/borrow/{id} - Should return 200 with record details")
    void getBorrowRecordById_Returns200() throws Exception {
        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookId(1L)
                .bookTitle("Clean Code")
                .memberId(1L)
                .memberName("Alice Johnson")
                .status(BorrowStatus.BORROWED)
                .build();

        when(borrowService.getBorrowRecordById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/borrow/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }
}
