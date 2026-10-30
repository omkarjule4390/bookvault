package com.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarymanagement.dto.BookRequest;
import com.librarymanagement.dto.BookResponse;
import com.librarymanagement.service.BookService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=testpass"})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("POST /api/v1/books - Should return 201 when book request is valid")
    void createBook_Valid_Returns201() throws Exception {
        BookRequest request = BookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .category("Software Engineering")
                .totalCopies(5)
                .publicationYear(2008)
                .build();

        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .category("Software Engineering")
                .totalCopies(5)
                .availableCopies(5)
                .publicationYear(2008)
                .build();

        when(bookService.createBook(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    @DisplayName("POST /api/v1/books - Should return 400 when validation fails")
    void createBook_Invalid_Returns400() throws Exception {
        BookRequest request = BookRequest.builder()
                .title("") // Blank title
                .author("Robert C. Martin")
                .isbn("invalid-isbn") // Bad ISBN
                .totalCopies(0) // Total copies must be >= 1
                .build();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return 200 with book details")
    void getBookById_Returns200() throws Exception {
        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .availableCopies(5)
                .build();

        when(bookService.getBookById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Should return 204 No Content")
    void deleteBook_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1").with(httpBasic("admin", "testpass")))
                .andExpect(status().isNoContent());
    }
}
