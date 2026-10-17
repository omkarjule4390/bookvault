package com.librarymanagement.service;

import com.librarymanagement.dto.BorrowRequest;
import com.librarymanagement.dto.BorrowResponse;
import com.librarymanagement.entity.*;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.ResourceNotFoundException;
import com.librarymanagement.mapper.EntityMapper;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRepository;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.impl.BorrowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private Book testBook;
    private Member testMember;
    private BorrowRequest testBorrowRequest;
    private BorrowRecord testBorrowRecord;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .title("Clean Architecture")
                .author("Robert C. Martin")
                .isbn("9780134494166")
                .totalCopies(3)
                .availableCopies(3)
                .build();

        testMember = Member.builder()
                .id(1L)
                .name("Bob Smith")
                .email("bob.smith@example.com")
                .status(MemberStatus.ACTIVE)
                .build();

        testBorrowRequest = BorrowRequest.builder()
                .bookId(1L)
                .memberId(1L)
                .borrowDays(14)
                .remarks("Test borrow")
                .build();

        testBorrowRecord = BorrowRecord.builder()
                .id(1L)
                .book(testBook)
                .member(testMember)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(4))
                .status(BorrowStatus.BORROWED)
                .fineAmount(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Should successfully borrow book and decrement available copies")
    void borrowBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRepository.countByMemberIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(0L);
        when(borrowRepository.findByMemberIdAndBookIdAndStatus(1L, 1L, BorrowStatus.BORROWED)).thenReturn(Optional.empty());
        when(borrowRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);

        BorrowResponse response = borrowService.borrowBook(testBorrowRequest);

        assertThat(response).isNotNull();
        assertThat(testBook.getAvailableCopies()).isEqualTo(2);
        verify(bookRepository, times(1)).save(testBook);
        verify(borrowRepository, times(1)).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when member is inactive or suspended")
    void borrowBook_MemberInactive_ThrowsException() {
        testMember.setStatus(MemberStatus.SUSPENDED);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> borrowService.borrowBook(testBorrowRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("is not permitted to borrow books");

        verify(borrowRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when member reaches max borrowing limit")
    void borrowBook_MaxLimitReached_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRepository.countByMemberIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(5L);

        assertThatThrownBy(() -> borrowService.borrowBook(testBorrowRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reached the maximum active borrowing limit");

        verify(borrowRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when book has no copies available")
    void borrowBook_NoCopiesAvailable_ThrowsException() {
        testBook.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowRepository.countByMemberIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(1L);
        when(borrowRepository.findByMemberIdAndBookIdAndStatus(1L, 1L, BorrowStatus.BORROWED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(testBorrowRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No copies of");

        verify(borrowRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("Should return book on time with no fine and increment copies")
    void returnBook_Success_OnTime() {
        testBook.setAvailableCopies(2);
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(testBorrowRecord));
        when(borrowRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);

        BorrowResponse response = borrowService.returnBook(1L);

        assertThat(response).isNotNull();
        assertThat(testBorrowRecord.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(testBorrowRecord.getFineAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(testBook.getAvailableCopies()).isEqualTo(3);
        verify(bookRepository, times(1)).save(testBook);
        verify(borrowRepository, times(1)).save(testBorrowRecord);
    }

    @Test
    @DisplayName("Should calculate fine when book is returned past due date")
    void returnBook_Success_OverdueWithFine() {
        testBook.setAvailableCopies(2);
        testBorrowRecord.setDueDate(LocalDate.now().minusDays(5)); // 5 days overdue
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(testBorrowRecord));
        when(borrowRepository.save(any(BorrowRecord.class))).thenReturn(testBorrowRecord);

        BorrowResponse response = borrowService.returnBook(1L);

        assertThat(response).isNotNull();
        assertThat(testBorrowRecord.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(testBorrowRecord.getFineAmount()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        assertThat(testBorrowRecord.getRemarks()).contains("Returned 5 day(s) overdue");
    }

    @Test
    @DisplayName("Should throw BusinessException if book was already returned")
    void returnBook_AlreadyReturned_ThrowsException() {
        testBorrowRecord.setStatus(BorrowStatus.RETURNED);
        testBorrowRecord.setReturnDate(LocalDate.now().minusDays(2));
        when(borrowRepository.findById(1L)).thenReturn(Optional.of(testBorrowRecord));

        assertThatThrownBy(() -> borrowService.returnBook(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already returned");

        verify(bookRepository, never()).save(any(Book.class));
    }
}
