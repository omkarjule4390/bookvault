package com.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.librarymanagement.dto.MemberRequest;
import com.librarymanagement.dto.MemberResponse;
import com.librarymanagement.entity.MemberStatus;
import com.librarymanagement.service.MemberService;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=testpass"})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("POST /api/v1/members - Should return 201 when member request is valid")
    void registerMember_Valid_Returns201() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .phone("+1-555-0199")
                .status(MemberStatus.ACTIVE)
                .build();

        MemberResponse response = MemberResponse.builder()
                .id(1L)
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .phone("+1-555-0199")
                .membershipDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberService.registerMember(any(MemberRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice.johnson@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/members - Should return 400 when email format is invalid")
    void registerMember_InvalidEmail_Returns400() throws Exception {
        MemberRequest request = MemberRequest.builder()
                .name("Alice Johnson")
                .email("not-an-email")
                .phone("+1-555-0199")
                .build();

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("GET /api/v1/members/{id} - Should return 200 with member profile")
    void getMemberById_Returns200() throws Exception {
        MemberResponse response = MemberResponse.builder()
                .id(1L)
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .phone("+1-555-0199")
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberService.getMemberById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice Johnson"));
    }

    @Test
    @DisplayName("DELETE /api/v1/members/{id} - Should return 204 No Content")
    void deleteMember_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/members/1").with(httpBasic("admin", "testpass")))
                .andExpect(status().isNoContent());
    }
}
