package com.librarymanagement.service;

import com.librarymanagement.dto.BookRequest;
import com.librarymanagement.dto.BookResponse;
import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.DuplicateResourceException;
import com.librarymanagement.exception.ResourceNotFoundException;
import com.librarymanagement.mapper.EntityMapper;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRepository;
import com.librarymanagement.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private BookRequest testBookRequest;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .category("Software Engineering")
                .totalCopies(5)
                .availableCopies(5)
                .publicationYear(2008)
                .build();

        testBookRequest = BookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .category("Software Engineering")
                .totalCopies(5)
                .publicationYear(2008)
                .build();
    }

    @Test
    @DisplayName("Should successfully create a book when ISBN is unique")
    void createBook_Success() {
        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookResponse response = bookService.createBook(testBookRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getIsbn()).isEqualTo("9780132350884");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when ISBN already exists")
    void createBook_DuplicateIsbn_ThrowsException() {
        when(bookRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(testBookRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists with ISBN");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should find book by ID")
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when book ID not found")
    void getBookById_NotFound_ThrowsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: '99'");
    }

    @Test
    @DisplayName("Should return paginated list of books")
    void getAllBooks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookResponse> responsePage = bookService.getAllBooks(pageable);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should successfully update an existing book")
    void updateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.existsByIsbnAndIdNot("9780132350884", 1L)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookResponse response = bookService.updateBook(1L, testBookRequest);

        assertThat(response).isNotNull();
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("Should delete book when it has no active borrow records")
    void deleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRepository.countByBookIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(0L);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(testBook);
    }

    @Test
    @DisplayName("Should throw BusinessException when deleting book with active borrows")
    void deleteBook_ActiveBorrows_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRepository.countByBookIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(2L);

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete book");

        verify(bookRepository, never()).delete(any(Book.class));
    }
}
