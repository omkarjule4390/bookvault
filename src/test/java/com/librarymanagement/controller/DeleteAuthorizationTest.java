package com.librarymanagement.controller;

import com.librarymanagement.config.SecurityConfig;
import com.librarymanagement.service.BookService;
import com.librarymanagement.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BookController.class, MemberController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=testpass"})
class DeleteAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("DELETE book without credentials returns 401 and deletes nothing")
    void deleteBook_NoCredentials_Returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1")).andExpect(status().isUnauthorized());
        verify(bookService, never()).deleteBook(anyLong());
    }

    @Test
    @DisplayName("DELETE book with a wrong password returns 401")
    void deleteBook_WrongPassword_Returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1").with(httpBasic("admin", "wrong")))
                .andExpect(status().isUnauthorized());
        verify(bookService, never()).deleteBook(anyLong());
    }

    @Test
    @DisplayName("DELETE book as admin returns 204")
    void deleteBook_Admin_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1").with(httpBasic("admin", "testpass")))
                .andExpect(status().isNoContent());
        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("DELETE member without credentials returns 401 and deletes nothing")
    void deleteMember_NoCredentials_Returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/members/1")).andExpect(status().isUnauthorized());
        verify(memberService, never()).deleteMember(anyLong());
    }

    @Test
    @DisplayName("DELETE member as admin returns 204")
    void deleteMember_Admin_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/members/1").with(httpBasic("admin", "testpass")))
                .andExpect(status().isNoContent());
        verify(memberService).deleteMember(1L);
    }
}
