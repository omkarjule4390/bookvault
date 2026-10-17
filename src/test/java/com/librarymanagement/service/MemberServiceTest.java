package com.librarymanagement.service;

import com.librarymanagement.dto.MemberRequest;
import com.librarymanagement.dto.MemberResponse;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.entity.Member;
import com.librarymanagement.entity.MemberStatus;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.DuplicateResourceException;
import com.librarymanagement.exception.ResourceNotFoundException;
import com.librarymanagement.mapper.EntityMapper;
import com.librarymanagement.repository.BorrowRepository;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;
    private MemberRequest testMemberRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .phone("+1-555-0199")
                .membershipDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .build();

        testMemberRequest = MemberRequest.builder()
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .phone("+1-555-0199")
                .status(MemberStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a member with unique email")
    void registerMember_Success() {
        when(memberRepository.existsByEmail("alice.johnson@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        MemberResponse response = memberService.registerMember(testMemberRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Alice Johnson");
        assertThat(response.getEmail()).isEqualTo("alice.johnson@example.com");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already registered")
    void registerMember_DuplicateEmail_ThrowsException() {
        when(memberRepository.existsByEmail("alice.johnson@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.registerMember(testMemberRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists with email");

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should find member by ID")
    void getMemberById_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        MemberResponse response = memberService.getMemberById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Johnson");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when member ID not found")
    void getMemberById_NotFound_ThrowsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found with id: '99'");
    }

    @Test
    @DisplayName("Should update member details successfully")
    void updateMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByEmailAndIdNot("alice.johnson@example.com", 1L)).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        MemberResponse response = memberService.updateMember(1L, testMemberRequest);

        assertThat(response).isNotNull();
        verify(memberRepository, times(1)).save(testMember);
    }

    @Test
    @DisplayName("Should delete member when they have no active borrow records")
    void deleteMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRepository.countByMemberIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(0L);

        memberService.deleteMember(1L);

        verify(memberRepository, times(1)).delete(testMember);
    }

    @Test
    @DisplayName("Should throw BusinessException when deleting member with active borrow records")
    void deleteMember_ActiveBorrows_ThrowsException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRepository.countByMemberIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(1L);

        assertThatThrownBy(() -> memberService.deleteMember(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete member");

        verify(memberRepository, never()).delete(any(Member.class));
    }
}
